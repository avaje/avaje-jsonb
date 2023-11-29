package io.avaje.jsonb.stream;

import java.util.function.Supplier;

/**
 * Strategy for recycling buffers used in parsing and generation.
 */
public enum BufferRecycleStrategy {

  HYBRID_POOL(BufferRecycler::hybrid),
  NO_RECYCLING(BufferRecycler::nonRecyclingPool),
  LOCK_FREE(BufferRecycler::lockFreePool),
  THREAD_LOCAL(BufferRecycler::threadLocalPool);

  private final Supplier<BufferRecycler> supplier;

  BufferRecycleStrategy(Supplier<BufferRecycler> supplier) {
    this.supplier = supplier;
  }

  BufferRecycler recycler() {
    return supplier.get();
  }
}
