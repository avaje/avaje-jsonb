package io.avaje.jsonb.stream;

import java.util.function.Supplier;

public enum BufferRecycleStrategy {

  BOUNDED_POOL(BufferRecycler::boundedPool),
  BOUNDED_POOL_UNSHARED(BufferRecycler::boundedPool),
  NO_RECYCLING(BufferRecycler::nonRecyclingPool),
  LOCK_FREE(BufferRecycler::lockFreePool),
  LOCK_FREE_UNSHARED(BufferRecycler::unsharedLockFreePool),
  THREAD_LOCAL(BufferRecycler::threadLocalPool);

  private final Supplier<BufferRecycler> supplier;

  BufferRecycleStrategy(Supplier<BufferRecycler> supplier) {
    this.supplier = supplier;
  }

  BufferRecycler recycler() {
    return supplier.get();
  }
}
