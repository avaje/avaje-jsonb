package io.avaje.json.stream.core;

import io.avaje.json.stream.JsonOutput;
import io.avaje.json.stream.core.HybridBufferRecycler.StripedLockFreePool;
import io.avaje.json.stream.core.Recyclers.NonRecyclingPool;
import io.avaje.json.stream.core.Recyclers.ThreadLocalPool;

import java.io.InputStream;

/**
 * Interface for controlling reuse of buffers used in parsing and generation.
 */
interface BufferRecycler {

  /**
   * Return a recycled generator with the given target OutputStream.
   */
  JsonGenerator generator(JsonOutput target);

  /**
   * Return a recycled generator with expected "to String" result.
   */
  default JsonGenerator generator() {
    return generator(null);
  }

  /**
   * Return the JsonParser given the content in bytes.
   */
  JsonParser parser(byte[] bytes);

  /**
   * Return the JsonParser given the content inputStream.
   */
  JsonParser parser(InputStream in);

  /**
   * Recycle the generator.
   */
  void recycle(JsonGenerator recycler);

  /**
   * Recycle the parser.
   */
  void recycle(JsonParser recycler);

  /**
   * @return Globally shared instance of {@link HybridBufferRecycler};
   */
  static BufferRecycler hybrid() {
    return HybridBufferRecycler.shared();
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
    return StripedLockFreePool.shared();
  }

}
