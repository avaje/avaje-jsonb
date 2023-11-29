package io.avaje.jsonb.stream;

import java.io.InputStream;

import io.avaje.jsonb.stream.HybridBufferRecycler.StripedLockFreePool;
import io.avaje.jsonb.stream.Recyclers.NonRecyclingPool;
import io.avaje.jsonb.stream.Recyclers.ThreadLocalPool;

/**
 * Interface for entity that controls creation and possible reuse of {@link JsonParsers} and {@link
 * JsonGenerators} instances.
 *
 * <p>Different pool implementations use different strategies on retaining recyclers for reuse. For
 * example we have:
 *
 * <ul>
 *   <li>{@link NonRecyclingPool} which does not retain any recyclers and will always simply
 *       construct and return new instances when called
 *   <li>{@link ThreadLocalPool} which uses {@link ThreadLocal} to retain at most 1 instance per
 *       {@link Thread}.
 *   <li>{@link BoundedPool} is "bounded pool" and retains at most N instance (default value being
 *       100) at any given time.
 *   <li>{@link LockFreePool} is "unbounded" and retains any number of instances released: in
 *       practice it is at most the highest number of concurrently used instances.
 * </ul>
 */
interface BufferRecycler {

  /** Return a recycled generator with the given target OutputStream. */
  JsonGenerator generator(JsonOutput target);

  /** Return a recycled generator with expected "to String" result. */
  default JsonGenerator generator() {
    return generator(null);
  }

  JsonParser parser(byte[] bytes);

  JsonParser parser(InputStream in);

  void recycle(JsonGenerator recycler);

  void recycle(JsonParser recycler);

  /**
   * @return Globally shared instance of {@link HybridBufferRecycler};
   */
  static BufferRecycler hybrid() {
    return HybridBufferRecycler.getInstance();
  }

  /**
   * @return Globally shared instance of {@link ThreadLocalPool}.
   */
  static BufferRecycler threadLocalPool() {
    return ThreadLocalPool.shared();
  }

  /**
   * @return Globally shared instance of {@link NonRecyclingPool}.
   */
  static BufferRecycler nonRecyclingPool() {
    return NonRecyclingPool.shared();
  }

  /**
   * @return Globally shared instance of {@link StripedLockFreePool}.
   */
  static BufferRecycler lockFreePool() {
    return StripedLockFreePool.getInstance();
  }

  /**
   * @return new instance of {@link StripedLockFreePool}
   */
  static BufferRecycler unsharedLockFreePool() {
    return StripedLockFreePool.nonShared();
  }
}
