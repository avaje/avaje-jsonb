package io.avaje.jsonb.stream;

import java.util.function.Supplier;

/** Strategy for recycling buffers used in parsing and generation. */
public enum BufferRecycleStrategy {

  /** Do not perform any sort of recycling. */
  NO_RECYCLING(BufferRecycler::nonRecyclingPool),
  /** A lock free implementation designed for virtual thread use */
  LOCK_FREE(BufferRecycler::lockFreePool),
  /** Use Thread Locals for recycling buffers */
  THREAD_LOCAL(BufferRecycler::threadLocalPool),
  /** Use 2 recyclers and switch between them depending on the nature of thread (virtual or not) */
  HYBRID_POOL(BufferRecycler::hybrid);

  private final Supplier<BufferRecycler> supplier;

  BufferRecycleStrategy(Supplier<BufferRecycler> supplier) {
    this.supplier = supplier;
  }

  BufferRecycler recycler() {
    return supplier.get();
  }
}
