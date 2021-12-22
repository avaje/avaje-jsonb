package io.avaje.jsonb.jakarta;

import java.lang.ref.SoftReference;

/**
 */
final class BufferRecycleProvider {

  private final static BufferRecycleTracker tracker;
  static {
    boolean tracking  = "true".equals(System.getProperty("io.avaje.jsonb.trackReusableBuffers"));
    tracker = tracking ? BufferRecycleTracker.instance() : null;
  }

  /**
   * This <code>ThreadLocal</code> contains a {@link SoftReference}
   * to a {@link BufferRecycler} used to provide a low-cost
   * buffer recycling between reader and writer instances.
   */
  private static final ThreadLocal<SoftReference<BufferRecycler>> recyclerRef = new ThreadLocal<>();

  /**
   * Main accessor to call for accessing possibly recycled {@link BufferRecycler} instance.
   *
   * @return {@link BufferRecycler} to use
   */
  private static BufferRecycler get() {
    SoftReference<BufferRecycler> ref = recyclerRef.get();
    BufferRecycler br = (ref == null) ? null : ref.get();
    if (br == null) {
      br = new Local();
      if (tracker != null) {
        ref = tracker.wrapAndTrack(br);
      } else {
        ref = new SoftReference<>(br);
      }
      recyclerRef.set(ref);
    }
    return br;
  }

  /**
   *
   * @return
   */
  static BufferRecycler provide() {
    return new ThreadLocalBased();
  }

  /**
   */
  static int releaseBuffers() {
    if (tracker != null) {
      return tracker.releaseBuffers();
    }
    return -1;
  }

  static final class ThreadLocalBased implements BufferRecycler {

    @Override
    public void recycle(int slot, char[] buffer) {
      BufferRecycleProvider.get().recycle(slot, buffer);
    }

    @Override
    public char[] take(int slot) {
      return BufferRecycleProvider.get().take(slot);
    }

    @Override
    public int release() {
      return BufferRecycleProvider.releaseBuffers();
    }
  }

  static final class Local implements BufferRecycler {

    private char[] buffer = new char[4000];

    @Override
    public void recycle(int slot, char[] buffer) {
      this.buffer = buffer;
    }

    @Override
    public char[] take(int slot) {
      final char[] buf = this.buffer;
      return buf == null ? new char[4000] : buffer;
    }

    @Override
    public int release() {
      buffer = null;
      return 1;
    }
  }
}
