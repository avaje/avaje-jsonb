package io.avaje.json.stream.core;

import io.avaje.json.stream.JsonOutput;
import io.avaje.json.stream.core.Recyclers.ThreadLocalPool;

import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Predicate;

/**
 * This is a custom implementation of the Jackson's RecyclerPool intended to work equally well with
 * both platform and virtual threads. This pool works regardless of the version of the JVM in use
 * and internally uses 2 distinct pools one for platform threads (which is exactly the same {@link
 * ThreadLocal} based one provided by Jackson out of the box) and the other designed for being
 * virtual threads friendly. It switches between the 2 only depending on the nature of thread
 * (virtual or not) requiring the acquisition of a pooled resource, obtained via {@link
 * MethodHandle} to guarantee compatibility also with old JVM versions. The pool also guarantees
 * that the pooled resource is always released to the same internal pool from where it has been
 * acquired, regardless if the releasing thread is different from the one that originally made the
 * acquisition.
 *
 * <p>The virtual thread friendly inner pool is implemented with N striped linked lists using a
 * simple lock free algorithm based on CAS. The striping is performed shuffling the id of the thread
 * requiring to acquire a pooled resource with a xorshift based computation. The resulting of this
 * computation is also stored in the pooled resource, bringing the twofold advantage of always
 * releasing the resource in the same bucket from where it has been taken regardless if the
 * releasing thread is different from the one that did the acquisition and avoiding the need of
 * recalculating the position of that bucket also during the release. The heads of the linked lists
 * are hold in an {@link AtomicReferenceArray} where each head has a distance of 16 positions from
 * the adjacent ones to prevent the false sharing problem.
 */
@SuppressWarnings("resource")
final class HybridBufferRecycler implements BufferRecycler {

  private static final HybridBufferRecycler INSTANCE = new HybridBufferRecycler();

  private static final Predicate<Thread> isVirtual = VirtualPredicate.findIsVirtualPredicate();

  private static final BufferRecycler NATIVE_RECYCLER = ThreadLocalPool.shared();
  private static final BufferRecycler VIRTUAL_RECYCLER = StripedLockFreePool.shared();

  private HybridBufferRecycler() {
  }

  static HybridBufferRecycler shared() {
    return INSTANCE;
  }

  @Override
  public JsonGenerator generator(JsonOutput target) {
    return isVirtual.test(Thread.currentThread())
      ? VIRTUAL_RECYCLER.generator(target)
      : NATIVE_RECYCLER.generator(target);
  }

  @Override
  public JsonParser parser(byte[] bytes) {
    return isVirtual.test(Thread.currentThread())
      ? VIRTUAL_RECYCLER.parser(bytes)
      : NATIVE_RECYCLER.parser(bytes);
  }

  @Override
  public JsonParser parser(InputStream in) {
    return isVirtual.test(Thread.currentThread())
      ? VIRTUAL_RECYCLER.parser(in)
      : NATIVE_RECYCLER.parser(in);
  }

  @Override
  public void recycle(JsonGenerator recycler) {
    if (recycler instanceof VThreadJGenerator) {
      VIRTUAL_RECYCLER.recycle(recycler);
    }
  }

  @Override
  public void recycle(JsonParser recycler) {
    if (recycler instanceof VThreadJParser) {
      VIRTUAL_RECYCLER.recycle(recycler);
    }
  }

  static final class StripedLockFreePool implements BufferRecycler {
    private static final StripedLockFreePool INSTANCE = new StripedLockFreePool(Runtime.getRuntime().availableProcessors());

    private static final int CACHE_LINE_SHIFT = 4;

    private static final int CACHE_LINE_PADDING = 1 << CACHE_LINE_SHIFT;

    private final XorShiftThreadProbe threadProbe;

    private final AtomicReferenceArray<JNode> generatorStacks;
    private final AtomicReferenceArray<PNode> parserStacks;

    private StripedLockFreePool(int stripesCount) {
      final int size = roundToPowerOfTwo(stripesCount);
      this.generatorStacks = new AtomicReferenceArray<>(size * CACHE_LINE_PADDING);
      this.parserStacks = new AtomicReferenceArray<>(size * CACHE_LINE_PADDING);

      int mask = (size - 1) << CACHE_LINE_SHIFT;
      this.threadProbe = new XorShiftThreadProbe(mask);
    }

    static StripedLockFreePool shared() {
      return INSTANCE;
    }

    @Override
    public JsonParser parser(byte[] bytes) {
      return parser().process(bytes, bytes.length);
    }

    @Override
    public JsonParser parser(InputStream in) {
      return parser().process(in);
    }

    @Override
    public JsonGenerator generator(JsonOutput target) {
      final int index = threadProbe.index();
      var currentHead = generatorStacks.get(index);
      while (true) {
        if (currentHead == null) {
          return new VThreadJGenerator(index).prepare(target);
        }

        if (generatorStacks.compareAndSet(index, currentHead, currentHead.next)) {
          currentHead.next = null;
          return currentHead.value.prepare(target);
        } else {
          currentHead = generatorStacks.get(index);
        }
      }
    }

    private JsonParser parser() {
      int index = threadProbe.index();

      var currentHead = parserStacks.get(index);
      while (true) {
        if (currentHead == null) {
          return new VThreadJParser(index);
        }

        if (parserStacks.compareAndSet(index, currentHead, currentHead.next)) {
          currentHead.next = null;
          return currentHead.value;
        } else {
          currentHead = parserStacks.get(index);
        }
      }
    }

    @Override
    public void recycle(JsonGenerator recycler) {
      var vThreadBufferRecycler = (VThreadJGenerator) recycler;
      var newHead = new JNode(vThreadBufferRecycler);

      var next = generatorStacks.get(vThreadBufferRecycler.slot);
      while (true) {
        newHead.level = next == null ? 1 : next.level + 1;
        if (generatorStacks.compareAndSet(vThreadBufferRecycler.slot, next, newHead)) {
          newHead.next = next;
          return;
        } else {
          next = generatorStacks.get(vThreadBufferRecycler.slot);
        }
      }
    }

    @Override
    public void recycle(JsonParser recycler) {
      var vThreadBufferRecycler = (VThreadJParser) recycler;
      var newHead = new PNode(vThreadBufferRecycler);

      var next = parserStacks.get(vThreadBufferRecycler.slot);
      while (true) {
        newHead.level = next == null ? 1 : next.level + 1;
        if (parserStacks.compareAndSet(vThreadBufferRecycler.slot, next, newHead)) {
          newHead.next = next;
          return;
        } else {
          next = parserStacks.get(vThreadBufferRecycler.slot);
        }
      }
    }

    private static final class JNode {
      final VThreadJGenerator value;
      JNode next;
      int level = 0;

      JNode(VThreadJGenerator value) {
        this.value = value;
      }
    }

    private static final class PNode {
      final VThreadJParser value;
      PNode next;
      int level = 0;

      PNode(VThreadJParser value) {
        this.value = value;
      }
    }
  }

  private static final class VThreadJGenerator extends JGenerator {
    private final int slot;

    private VThreadJGenerator(int slot) {
      super(Recyclers.GENERATOR_BUFFER_SIZE);
      this.slot = slot;
    }
  }

  private static final class VThreadJParser extends JParser {
    private final int slot;

    private VThreadJParser(int slot) {
      super(
        new char[Recyclers.PARSER_CHAR_BUFFER_SIZE],
        new byte[Recyclers.PARSER_BUFFER_SIZE],
        0,
        JParser.ErrorInfo.MINIMAL,
        JParser.DoublePrecision.DEFAULT,
        JParser.UnknownNumberParsing.BIGDECIMAL,
        100,
        50_000);
      this.slot = slot;
    }
  }

  private static final class VirtualPredicate {
    private static final MethodHandle virtualMh = findVirtualMH();

    private static MethodHandle findVirtualMH() {
      try {
        return MethodHandles.publicLookup()
          .findVirtual(Thread.class, "isVirtual", MethodType.methodType(boolean.class));
      } catch (Exception e) {
        return null;
      }
    }

    private static Predicate<Thread> findIsVirtualPredicate() {
      return virtualMh == null ? VirtualPredicate::notVirtual : VirtualPredicate::isVirtual;
    }

    private static boolean isVirtual(Thread thread) {
      try {
        return (boolean) virtualMh.invokeExact(thread);
      } catch (Throwable e) {
        throw new RuntimeException(e);
      }
    }

    private static boolean notVirtual(Thread thread) {
      return false;
    }
  }

  private static final class XorShiftThreadProbe {

    private final int mask;

    XorShiftThreadProbe(int mask) {
      this.mask = mask;
    }

    public int index() {
      return probe() & mask;
    }

    private int probe() {
      // Multiplicative Fibonacci hashing implementation
      // 0x9e3779b9 is the integral part of the Golden Ratio's fractional part 0.61803398875…
      // (sqrt(5)-1)/2
      // multiplied by 2^32, which has the best possible scattering properties.
      int probe = (int) ((Thread.currentThread().getId() * 0x9e3779b9) & Integer.MAX_VALUE);
      // xorshift
      probe ^= probe << 13;
      probe ^= probe >>> 17;
      probe ^= probe << 5;
      return probe;
    }
  }

  private static final int MAX_POW2 = 1 << 30;

  private static int roundToPowerOfTwo(final int value) {
    if (value > MAX_POW2) {
      throw new IllegalArgumentException("Exceeded max power of 2 (2^31), got " + value);
    }
    if (value < 0) {
      throw new IllegalArgumentException("Expecting value >= 0, got " + value);
    }
    return 1 << (32 - Integer.numberOfLeadingZeros(value - 1));
  }
}
