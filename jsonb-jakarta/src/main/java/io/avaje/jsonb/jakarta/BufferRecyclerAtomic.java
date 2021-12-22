package io.avaje.jsonb.jakarta;

import java.util.concurrent.atomic.AtomicReferenceArray;

final class BufferRecyclerAtomic implements BufferRecycler {

  private final AtomicReferenceArray<char[]> buffers;

  BufferRecyclerAtomic() {
    buffers = new AtomicReferenceArray<>(1);
    buffers.set(0, new char[4000]);
  }

  @Override
  public char[] take(int slot) {
    final char[] sw = buffers.getAndSet(0, null);
    return sw == null ? new char[4000] : sw;
  }

  @Override
  public void recycle(int slot, char[] buffer) {
    buffers.set(0, buffer);
  }

  @Override
  public int release() {
    buffers.set(0, null);
    return 1;
  }

}
