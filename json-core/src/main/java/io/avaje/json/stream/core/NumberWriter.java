package io.avaje.json.stream.core;

final class NumberWriter {

  private static final byte MINUS = '-';
  private static final byte[] MIN_INT = "-2147483648".getBytes();
  private static final byte[] MIN_LONG = "-9223372036854775808".getBytes();
  private static final int[] DIGITS = new int[1000];

  static {
    for (int i = 0; i < DIGITS.length; i++) {
      DIGITS[i] = (i < 10 ? (2 << 24) : i < 100 ? (1 << 24) : 0)
        + (((i / 100) + '0') << 16)
        + ((((i / 10) % 10) + '0') << 8)
        + i % 10 + '0';
    }
  }

  private NumberWriter() {}

  static void writeInt(final int value, final JGenerator sw) {
    final byte[] buf = sw.ensureCapacity(11);
    final int position = sw.position();
    int current = serializeInt(buf, position, value);
    sw.advance(current - position);
  }

  private static int serializeInt(final byte[] buf, int pos, final int value) {
    int i;
    if (value < 0) {
      if (value == Integer.MIN_VALUE) {
        System.arraycopy(MIN_INT, 0, buf, pos, MIN_INT.length);
        return pos + MIN_INT.length;
      }
      i = -value;
      buf[pos++] = MINUS;
    } else {
      i = value;
    }
    final int q1 = i / 1000;
    if (q1 == 0) {
      pos += writeFirstBuf(buf, DIGITS[i], pos);
      return pos;
    }
    final int r1 = i - q1 * 1000;
    final int q2 = q1 / 1000;
    if (q2 == 0) {
      final int v1 = DIGITS[r1];
      final int v2 = DIGITS[q1];
      int off = writeFirstBuf(buf, v2, pos);
      writeBuf(buf, v1, pos + off);
      return pos + 3 + off;
    }
    final int r2 = q1 - q2 * 1000;
    final int q3 = q2 / 1000;
    final int v1 = DIGITS[r1];
    final int v2 = DIGITS[r2];
    if (q3 == 0) {
      pos += writeFirstBuf(buf, DIGITS[q2], pos);
    } else {
      final int r3 = q2 - q3 * 1000;
      buf[pos++] = (byte) (q3 + '0');
      writeBuf(buf, DIGITS[r3], pos);
      pos += 3;
    }
    writeBuf(buf, v2, pos);
    writeBuf(buf, v1, pos + 3);
    return pos + 6;
  }

  private static int writeFirstBuf(final byte[] buf, final int v, int pos) {
    final int start = v >> 24;
    if (start == 0) {
      buf[pos++] = (byte) (v >> 16);
      buf[pos++] = (byte) (v >> 8);
    } else if (start == 1) {
      buf[pos++] = (byte) (v >> 8);
    }
    buf[pos] = (byte) v;
    return 3 - start;
  }

  private static void writeBuf(final byte[] buf, final int v, int pos) {
    buf[pos] = (byte) (v >> 16);
    buf[pos + 1] = (byte) (v >> 8);
    buf[pos + 2] = (byte) v;
  }


  static void writeLong(final long value, final JGenerator sw) {
    final byte[] buf = sw.ensureCapacity(21);
    final int position = sw.position();
    int current = serializeLong(buf, position, value);
    sw.advance(current - position);
  }

  private static int serializeLong(final byte[] buf, int pos, final long value) {
    long i;
    if (value < 0) {
      if (value == Long.MIN_VALUE) {
        System.arraycopy(MIN_LONG, 0, buf, pos, MIN_LONG.length);
        return pos + MIN_LONG.length;
      }
      i = -value;
      buf[pos++] = MINUS;
    } else {
      i = value;
    }
    final long q1 = i / 1000;
    if (q1 == 0) {
      pos += writeFirstBuf(buf, DIGITS[(int) i], pos);
      return pos;
    }
    final int r1 = (int) (i - q1 * 1000);
    final long q2 = q1 / 1000;
    if (q2 == 0) {
      final int v1 = DIGITS[r1];
      final int v2 = DIGITS[(int) q1];
      int off = writeFirstBuf(buf, v2, pos);
      writeBuf(buf, v1, pos + off);
      return pos + 3 + off;
    }
    final int r2 = (int) (q1 - q2 * 1000);
    final long q3 = q2 / 1000;
    if (q3 == 0) {
      final int v1 = DIGITS[r1];
      final int v2 = DIGITS[r2];
      final int v3 = DIGITS[(int) q2];
      pos += writeFirstBuf(buf, v3, pos);
      writeBuf(buf, v2, pos);
      writeBuf(buf, v1, pos + 3);
      return pos + 6;
    }
    final int r3 = (int) (q2 - q3 * 1000);
    final int q4 = (int) (q3 / 1000);
    if (q4 == 0) {
      final int v1 = DIGITS[r1];
      final int v2 = DIGITS[r2];
      final int v3 = DIGITS[r3];
      final int v4 = DIGITS[(int) q3];
      pos += writeFirstBuf(buf, v4, pos);
      writeBuf(buf, v3, pos);
      writeBuf(buf, v2, pos + 3);
      writeBuf(buf, v1, pos + 6);
      return pos + 9;
    }
    final int r4 = (int) (q3 - q4 * 1000);
    final int q5 = q4 / 1000;
    if (q5 == 0) {
      final int v1 = DIGITS[r1];
      final int v2 = DIGITS[r2];
      final int v3 = DIGITS[r3];
      final int v4 = DIGITS[r4];
      final int v5 = DIGITS[q4];
      pos += writeFirstBuf(buf, v5, pos);
      writeBuf(buf, v4, pos);
      writeBuf(buf, v3, pos + 3);
      writeBuf(buf, v2, pos + 6);
      writeBuf(buf, v1, pos + 9);
      return pos + 12;
    }
    final int r5 = q4 - q5 * 1000;
    final int q6 = q5 / 1000;
    final int v1 = DIGITS[r1];
    final int v2 = DIGITS[r2];
    final int v3 = DIGITS[r3];
    final int v4 = DIGITS[r4];
    final int v5 = DIGITS[r5];
    if (q6 == 0) {
      pos += writeFirstBuf(buf, DIGITS[q5], pos);
    } else {
      final int r6 = q5 - q6 * 1000;
      buf[pos++] = (byte) (q6 + '0');
      writeBuf(buf, DIGITS[r6], pos);
      pos += 3;
    }
    writeBuf(buf, v5, pos);
    writeBuf(buf, v4, pos + 3);
    writeBuf(buf, v3, pos + 6);
    writeBuf(buf, v2, pos + 9);
    writeBuf(buf, v1, pos + 12);
    return pos + 15;
  }
}
