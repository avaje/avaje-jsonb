package io.avaje.jsonb.jakarta;

import java.util.ArrayList;
import java.util.List;

final class TextBuffer {

  private static final int MIN_SEGMENT_LEN = 500;
  private static final int MAX_SEGMENT_LEN = 0x10000;

  private final BufferRecycler pool;
  private List<char[]> segments;
  private char[] currentSegment;

  /**
   * Total characters in segments.
   */
  private int segmentSize;

  /**
   * Total characters in currently active (last) segment.
   */
  private int currentSize;

  TextBuffer(BufferRecycler allocator) {
    this.pool = allocator;
    this.currentSegment = pool.take(0);
  }

  String getAndClear() {
    String result = contentsAsString();
    pool.recycle(0, currentSegment);
    return result;
  }

  private String contentsAsString() {
    final int segLen = segmentSize;
    final int currLen = currentSize;
    if (segLen == 0) {
      // just one buffer
      return (currLen == 0) ? "" : new String(currentSegment, 0, currLen);
    }
    // combine the segments
    StringBuilder sb = new StringBuilder(segLen + currLen);
    if (segments != null) {
      for (char[] seg : segments) {
        sb.append(seg, 0, seg.length);
      }
    }
    sb.append(currentSegment, 0, currentSize);
    return sb.toString();
  }

  public void append(char c) {
    // Room in current segment?
    char[] curr = currentSegment;
    if (currentSize >= curr.length) {
      expand();
      curr = currentSegment;
    }
    curr[currentSize++] = c;
  }

  public void append(char[] c, int start, int len) {
    // Room in current segment?
    char[] curr = currentSegment;
    int max = curr.length - currentSize;
    if (max >= len) {
      System.arraycopy(c, start, curr, currentSize, len);
      currentSize += len;
      return;
    }
    // No room for all, need to copy part(s):
    if (max > 0) {
      System.arraycopy(c, start, curr, currentSize, max);
      start += max;
      len -= max;
    }
    // And then allocate new segment; we are guaranteed to now
    // have enough room in segment.
    do {
      expand();
      int amount = Math.min(currentSegment.length, len);
      System.arraycopy(c, start, currentSegment, 0, amount);
      currentSize += amount;
      start += amount;
      len -= amount;
    } while (len > 0);
  }

  public void append(String str, int offset, int len) {
    // Room in current segment?
    char[] curr = currentSegment;
    int max = curr.length - currentSize;
    if (max >= len) {
      str.getChars(offset, offset + len, curr, currentSize);
      currentSize += len;
      return;
    }
    // No room for all, need to copy part(s):
    if (max > 0) {
      str.getChars(offset, offset + max, curr, currentSize);
      len -= max;
      offset += max;
    }
    // And then allocate new segment; we are guaranteed to now
    // have enough room in segment.
    do {
      expand();
      int amount = Math.min(currentSegment.length, len);
      str.getChars(offset, offset + amount, currentSegment, 0);
      currentSize += amount;
      offset += amount;
      len -= amount;
    } while (len > 0);
  }

  private void expand() {
    // First, let's move current segment to segment list:
    if (segments == null) {
      segments = new ArrayList<>();
    }
    char[] curr = currentSegment;
    segments.add(curr);
    segmentSize += curr.length;
    currentSize = 0;
    int oldLen = curr.length;

    // Let's grow segments by 50% minimum
    int newLen = oldLen + (oldLen >> 1);
    if (newLen < MIN_SEGMENT_LEN) {
      newLen = MIN_SEGMENT_LEN;
    } else if (newLen > MAX_SEGMENT_LEN) {
      newLen = MAX_SEGMENT_LEN;
    }
    currentSegment = carr(newLen);
  }

  private char[] carr(int len) {
    return new char[len];
  }
}
