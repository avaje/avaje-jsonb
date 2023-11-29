package io.avaje.jsonb.stream;

import java.io.InputStream;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Different pool implementations use different strategies on retaining recyclers for reuse. For
 * example we have:
 *
 * <ul>
 *   <li>{@link NonRecyclingPool} which does not retain any recyclers and will always simply
 *       construct and return new instances when called
 *   <li>{@link ThreadLocalPool} which uses {@link ThreadLocal} to retain at most 1 recycler per
 *       {@link Thread}.
 *   <li>{@link BoundedPool} is "bounded pool" and retains at most N recyclers (default value being
 *       {@link BoundedPool#DEFAULT_CAPACITY}) at any given time.
 *   <li>{@link LockFreePool} is "unbounded" and retain any number of recyclers released: in
 *       practice it is at most the highest number of concurrently used instances.
 * </ul>
 */
class Recyclers {

  private Recyclers() {}

  private static final int GENERATOR_BUFFER_SIZE =
      Integer.getInteger("jsonb.generatorBufferSize", 4096);
  private static final int PARSER_BUFFER_SIZE = Integer.getInteger("jsonb.parserBufferSize", 4096);
  private static final int PARSER_CHAR_BUFFER_SIZE =
      Integer.getInteger("jsonb.parserCharBufferSize", 4096);

  private static JGenerator createGenerator() {
    return new JGenerator(GENERATOR_BUFFER_SIZE);
  }

  private static JParser createParser() {
    final char[] ch = new char[PARSER_CHAR_BUFFER_SIZE];
    final byte[] by = new byte[PARSER_BUFFER_SIZE];
    return new JParser(
        ch,
        by,
        0,
        JParser.ErrorInfo.MINIMAL,
        JParser.DoublePrecision.DEFAULT,
        JParser.UnknownNumberParsing.BIGDECIMAL,
        100,
        50_000);
  }

  /**
   * Default {@link BufferRecycler} implementation that uses {@link ThreadLocal} for recycling
   * instances. {@link BufferRecycler} instances are stored using {@link
   * java.lang.ref.SoftReference}s so that they may be Garbage Collected as needed by JVM.
   *
   * <p>Note that this implementation may not work well on platforms where {@link
   * java.lang.ref.SoftReference}s are not well supported (like Android), or on platforms where
   * {@link java.lang.Thread}s are not long-living or reused (like Project Loom).
   */
  static class ThreadLocalPool implements BufferRecycler {

    private final ThreadLocal<JParser> PARSER = ThreadLocal.withInitial(Recyclers::createParser);
    private final ThreadLocal<JGenerator> GENERATOR =
        ThreadLocal.withInitial(Recyclers::createGenerator);

    private static final BufferRecycler GLOBAL = new ThreadLocalPool();

    public static BufferRecycler shared() {
      return GLOBAL;
    }

    private ThreadLocalPool() {}

    @Override
    public JsonGenerator generator(JsonOutput target) {
      return GENERATOR.get().prepare(target);
    }

    @Override
    public JsonParser parser(byte[] bytes) {
      return PARSER.get().process(bytes, bytes.length);
    }

    @Override
    public JsonParser parser(InputStream in) {
      return PARSER.get().process(in);
    }

    @Override
    public void recycle(JsonGenerator recycler) {
      // nothing to do
    }

    @Override
    public void recycle(JsonParser recycler) {
      // nothing to do
    }
  }

  /**
   * {@link BufferRecycler} implementation that does not use any pool but simply creates new
   * instances when necessary.
   */
  static class NonRecyclingPool implements BufferRecycler {

    private static final BufferRecycler GLOBAL = new NonRecyclingPool();

    private NonRecyclingPool() {}

    public static BufferRecycler shared() {
      return GLOBAL;
    }

    @Override
    public JsonGenerator generator(JsonOutput target) {
      return createGenerator().prepare(target);
    }

    @Override
    public JsonParser parser(byte[] bytes) {
      return createParser().process(bytes, bytes.length);
    }

    @Override
    public JsonParser parser(InputStream in) {
      return createParser().process(in);
    }

    @Override
    public void recycle(JsonGenerator recycler) {
      // nothing to do
    }

    @Override
    public void recycle(JsonParser recycler) {
      // nothing to do
    }
  }

  /**
   * {@link BufferRecycler} implementation that uses a lock free linked list for recycling instances
   */
  static class LockFreePool implements BufferRecycler {

    private static final LockFreePool GLOBAL = new LockFreePool();

    private final AtomicReference<LockFreePool.GenNode> genhead = new AtomicReference<>();
    private final AtomicReference<LockFreePool.ParseNode> parsehead = new AtomicReference<>();

    private LockFreePool() {}

    public static LockFreePool shared() {
      return GLOBAL;
    }

    public static LockFreePool nonShared() {
      return new LockFreePool();
    }

    @Override
    public JsonGenerator generator(JsonOutput target) {

      return getGen(target);
    }

    @Override
    public JsonParser parser(byte[] bytes) {
      return getParser().process(bytes, bytes.length);
    }

    @Override
    public JsonParser parser(InputStream in) {

      return getParser().process(in);
    }

    private JsonGenerator getGen(JsonOutput target) {
      // This simple lock free algorithm uses an optimistic compareAndSet strategy to
      // populate the underlying linked list in a thread-safe way. However, under very
      // heavy contention, the compareAndSet could fail multiple times, so it seems a
      // reasonable heuristic to limit the number of retries in this situation.
      for (int i = 0; i < 3; i++) {
        var currentHead = genhead.get();
        if (currentHead == null) {
          return createGenerator().prepare(target);
        }
        if (genhead.compareAndSet(currentHead, currentHead.next)) {
          currentHead.next = null;
          return currentHead.value.prepare(target);
        }
      }
      return createGenerator().prepare(target);
    }

    private JsonParser getParser() {
      for (int i = 0; i < 3; i++) {
        var currentHead = parsehead.get();
        if (currentHead == null) {
          return createParser();
        }
        if (parsehead.compareAndSet(currentHead, currentHead.next)) {
          currentHead.next = null;
          return currentHead.value;
        }
      }
      return createParser();
    }

    @Override
    public void recycle(JsonGenerator bufferRecycler) {
      var newHead = new LockFreePool.GenNode(bufferRecycler);
      for (int i = 0; i < 3; i++) {
        newHead.next = genhead.get();
        if (genhead.compareAndSet(newHead.next, newHead)) {
          return;
        }
      }
    }

    @Override
    public void recycle(JsonParser bufferRecycler) {
      var newHead = new LockFreePool.ParseNode(bufferRecycler);
      for (int i = 0; i < 3; i++) {
        newHead.next = parsehead.get();
        if (parsehead.compareAndSet(newHead.next, newHead)) {
          return;
        }
      }
    }

    private static class GenNode {
      final JsonGenerator value;
      LockFreePool.GenNode next;

      GenNode(JsonGenerator value) {
        this.value = value;
      }
    }

    private static class ParseNode {
      final JsonParser value;
      LockFreePool.ParseNode next;

      ParseNode(JsonParser value) {
        this.value = value;
      }
    }
  }

  /**
   * {@link BufferRecycler} implementation that uses a bounded queue ({@link ArrayBlockingQueue} for
   * recycling instances. This is "bounded" pool since it will never hold on to more instances than
   * its size configuration: the default size is {@link BoundedPool#DEFAULT_CAPACITY}.
   */
  static class BoundedPool implements BufferRecycler {

    private static final int DEFAULT_CAPACITY = Integer.getInteger("jsonb.recycler.capacity", 100);

    private static final BoundedPool GLOBAL = new BoundedPool(DEFAULT_CAPACITY);

    private final Queue<JsonGenerator> genPool;
    private final Queue<JsonParser> parsePool;

    private BoundedPool(int capacity) {
      genPool = new ArrayBlockingQueue<>(capacity);
      parsePool = new ArrayBlockingQueue<>(capacity);
    }

    public static BoundedPool shared() {
      return GLOBAL;
    }

    public static BoundedPool nonShared() {

      return new BoundedPool(DEFAULT_CAPACITY);
    }

    @Override
    public JsonGenerator generator(JsonOutput target) {
      var bufferRecycler = genPool.poll();
      if (bufferRecycler == null) {
        bufferRecycler = createGenerator();
      }
      return bufferRecycler.prepare(target);
    }

    @Override
    public JsonParser parser(byte[] bytes) {

      var bufferRecycler = parsePool.poll();
      if (bufferRecycler == null) {
        bufferRecycler = createParser();
      }
      return bufferRecycler.process(bytes, bytes.length);
    }

    @Override
    public JsonParser parser(InputStream in) {

      var bufferRecycler = parsePool.poll();
      if (bufferRecycler == null) {
        bufferRecycler = createParser();
      }
      return bufferRecycler.process(in);
    }

    @Override
    public void recycle(JsonGenerator recycler) {
      genPool.offer(recycler);
    }

    @Override
    public void recycle(JsonParser recycler) {
      parsePool.offer(recycler);
    }
  }
}
