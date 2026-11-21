package io.avaje.jsonb.core;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

import io.avaje.json.AsciiCodec;
import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;

/** Reads and writes UUIDs in canonical 8-4-4-4-12 hex form directly from and to bytes. */
final class UuidAdapter implements JsonAdapter<UUID>, AsciiCodec<UUID> {

  private static final int LENGTH = 36;

  private static final byte[] HEX_DIGITS = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
  };

  /** Value of each hex digit byte, -1 for any other byte. */
  private static final long[] HEX_VALUES = new long[256];

  static {
    Arrays.fill(HEX_VALUES, -1);
    for (var i = 0; i < 10; i++) {
      HEX_VALUES['0' + i] = i;
    }
    for (var i = 0; i < 6; i++) {
      HEX_VALUES['a' + i] = 10 + i;
      HEX_VALUES['A' + i] = 10 + i;
    }
  }

  @Override
  public UUID fromJson(JsonReader reader) {
    return reader.readString(this);
  }

  @Override
  public void toJson(JsonWriter writer, UUID value) {
    writer.value(this, value);
  }

  @Override
  public UUID decode(String text) {
    return UUID.fromString(text);
  }

  @Override
  public UUID decode(byte[] buffer, int start, int end) {
    if (end - start == LENGTH
        && buffer[start + 8] == '-'
        && buffer[start + 13] == '-'
        && buffer[start + 18] == '-'
        && buffer[start + 23] == '-') {
      final var g0 = hex4(buffer, start);
      final var g1 = hex4(buffer, start + 4);
      final var g2 = hex4(buffer, start + 9);
      final var g3 = hex4(buffer, start + 14);
      final var g4 = hex4(buffer, start + 19);
      final var g5 = hex4(buffer, start + 24);
      final var g6 = hex4(buffer, start + 28);
      final var g7 = hex4(buffer, start + 32);
      if ((g0 | g1 | g2 | g3 | g4 | g5 | g6 | g7) >= 0) {
        return new UUID(g0 << 48 | g1 << 32 | g2 << 16 | g3, g4 << 48 | g5 << 32 | g6 << 16 | g7);
      }
    }
    // non standard or malformed
    return UUID.fromString(new String(buffer, start, end - start, StandardCharsets.ISO_8859_1));
  }

  /** Return the value of 4 hex digits, or a negative value if any byte is not a hex digit. */
  private static long hex4(byte[] buffer, int pos) {
    return HEX_VALUES[buffer[pos] & 0xFF] << 12
        | HEX_VALUES[buffer[pos + 1] & 0xFF] << 8
        | HEX_VALUES[buffer[pos + 2] & 0xFF] << 4
        | HEX_VALUES[buffer[pos + 3] & 0xFF];
  }

  @Override
  public int maxLength(UUID value) {
    return LENGTH;
  }

  @Override
  public int encode(UUID value, byte[] buffer, int pos) {
    final var msb = value.getMostSignificantBits();
    final var lsb = value.getLeastSignificantBits();
    writeHex(buffer, pos, msb >>> 32, 8);
    buffer[pos + 8] = '-';
    writeHex(buffer, pos + 9, msb >>> 16, 4);
    buffer[pos + 13] = '-';
    writeHex(buffer, pos + 14, msb, 4);
    buffer[pos + 18] = '-';
    writeHex(buffer, pos + 19, lsb >>> 48, 4);
    buffer[pos + 23] = '-';
    writeHex(buffer, pos + 24, lsb, 12);
    return pos + LENGTH;
  }

  private static void writeHex(byte[] buffer, int pos, long value, int digits) {
    for (var i = pos + digits - 1; i >= pos; i--) {
      buffer[i] = HEX_DIGITS[(int) value & 0xF];
      value >>>= 4;
    }
  }

  @Override
  public String toString() {
    return "JsonAdapter(UUID)";
  }
}
