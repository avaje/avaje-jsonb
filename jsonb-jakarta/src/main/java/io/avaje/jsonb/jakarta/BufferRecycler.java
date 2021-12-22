package io.avaje.jsonb.jakarta;

interface BufferRecycler {

  char[] take(int slot);

  void recycle(int slot, char[] buffer);

  /**
   * On shutdown release the buffers.
   * Generally only necessary when service reloading occurs.
   */
  int release();
}
