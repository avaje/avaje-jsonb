package io.avaje.json.stream;

/** Strategy for recycling buffers used in parsing and generation. */
public enum BufferRecycleStrategy {

  /** Do not perform any sort of recycling. */
  NO_RECYCLING,
  /** A lock free implementation designed for virtual thread use */
  LOCK_FREE,
  /** Use Thread Locals for recycling buffers */
  THREAD_LOCAL,
  /** Use 2 recyclers and switch between them depending on the nature of thread (virtual or not) */
  HYBRID_POOL;

}
