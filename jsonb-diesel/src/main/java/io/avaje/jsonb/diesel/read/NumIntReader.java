package io.avaje.jsonb.diesel.read;

import java.io.IOException;
import java.math.BigDecimal;

final class NumIntReader {


  public static short deserializeShort(final JReader reader) throws IOException {
    if (reader.last() == '"') {
      final int position = reader.getCurrentIndex();
      final char[] buf = reader.readSimpleQuote();
      try {
        return parseNumberGeneric(buf, reader.getCurrentIndex() - position - 1, reader, true).shortValueExact();
      } catch (ArithmeticException ignore) {
        throw reader.newParseErrorAt("Short overflow detected", reader.getCurrentIndex() - position);
      }
    }
    final int start = reader.scanNumber();
    final int end = reader.getCurrentIndex();
    final byte[] buf = reader.buffer;
    final byte ch = buf[start];
    final int value = ch == '-'
      ? parseNegativeInt(buf, reader, start, end)
      : parsePositiveInt(buf, reader, start, end, 0);
    if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
      throw reader.newParseErrorAt("Short overflow detected", reader.getCurrentIndex());
    }
    return (short)value;
  }

  public static int deserializeInt(final JReader reader) throws IOException {
    if (reader.last() == '"') {
      final int position = reader.getCurrentIndex();
      final char[] buf = reader.readSimpleQuote();
      try {
        return parseNumberGeneric(buf, reader.getCurrentIndex() - position - 1, reader, true).intValueExact();
      } catch (ArithmeticException ignore) {
        throw reader.newParseErrorAt("Integer overflow detected", reader.getCurrentIndex() - position);
      }
    }
    final int start = reader.scanNumber();
    final int end = reader.getCurrentIndex();
    final byte[] buf = reader.buffer;
    final byte ch = buf[start];
    if (ch == '-') {
      if (end > start + 2 && buf[start + 1] == '0' && buf[start + 2] >= '0' && buf[start + 2] <= '9') {
        numberException(reader, start, end, "Leading zero is not allowed");
      }
      return parseNegativeInt(buf, reader, start, end);
    } else {
      if (ch == '0' && end > start + 1 && buf[start + 1] >= '0' && buf[start + 1] <= '9') {
        numberException(reader, start, end, "Leading zero is not allowed");
      }
      return parsePositiveInt(buf, reader, start, end, 0);
    }
  }

  private static int parsePositiveInt(final byte[] buf, final JReader reader, final int start, final int end, final int offset) throws IOException {
    int value = 0;
    int i = start + offset;
    if (i == end) numberException(reader, start, end, "Digit not found");
    for (; i < end; i++) {
      final int ind = buf[i] - 48;
      if (ind < 0 || ind > 9) {
        if (i > start + offset && reader.allWhitespace(i, end)) return value;
        else if (i == end - 1 && buf[i] == '.') numberException(reader, start, end, "Number ends with a dot");
        final BigDecimal v = parseNumberGeneric(reader.prepareBuffer(start, end - start), end - start, reader, false);
        if (v.scale() > 0) numberException(reader, start, end, "Expecting int but found decimal value", v);
        return v.intValue();

      }
      value = (value << 3) + (value << 1) + ind;
      if (value < 0) {
        numberException(reader, start, end, "Integer overflow detected");
      }
    }
    return value;
  }

  private static int parseNegativeInt(final byte[] buf, final JReader reader, final int start, final int end) throws IOException {
    int value = 0;
    int i = start + 1;
    if (i == end) numberException(reader, start, end, "Digit not found");
    for (; i < end; i++) {
      final int ind = buf[i] - 48;
      if (ind < 0 || ind > 9) {
        if (i > start + 1 && reader.allWhitespace(i, end)) return value;
        else if (i == end - 1 && buf[i] == '.') numberException(reader, start, end, "Number ends with a dot");
        final BigDecimal v = parseNumberGeneric(reader.prepareBuffer(start, end - start), end - start, reader, false);
        if (v.scale() > 0) numberException(reader, start, end, "Expecting int but found decimal value", v);
        return v.intValue();
      }
      value = (value << 3) + (value << 1) - ind;
      if (value > 0) {
        numberException(reader, start, end, "Integer overflow detected");
      }
    }
    return value;
  }

  private static BigDecimal parseNumberGeneric(final char[] buf, final int len, final JReader reader, final boolean withQuotes) throws ParsingException {
    int end = len;
    while (end > 0 && Character.isWhitespace(buf[end - 1])) {
      end--;
    }
    if (end > reader.maxNumberDigits) {
      throw reader.newParseErrorWith("Too many digits detected in number", len, "", "Too many digits detected in number", end, "");
    }
    final int offset = buf[0] == '-' ? 1 : 0;
    if (buf[offset] == '0' && end > offset + 1 && buf[offset + 1] >= '0' && buf[offset + 1] <= '9') {
      throw reader.newParseErrorAt("Leading zero is not allowed. Error parsing number", len + (withQuotes ? 2 : 0));
    }
    try {
      return new BigDecimal(buf, 0, end);
    } catch (NumberFormatException nfe) {
      throw reader.newParseErrorAt("Error parsing number", len + (withQuotes ? 2 : 0), nfe);
    }
  }

  static void numberException(final JReader reader, final int start, final int end, String message) throws ParsingException {
    final int len = end - start;
    if (len > reader.maxNumberDigits) {
      throw reader.newParseErrorWith("Too many digits detected in number", len, "", "Too many digits detected in number", end, "");
    }
    throw reader.newParseErrorWith("Error parsing number", len, "", message, null, ". Error parsing number");
  }

  static void numberException(final JReader reader, final int start, final int end, String message, Object messageArgument) throws ParsingException {
    final int len = end - start;
    if (len > reader.maxNumberDigits) {
      throw reader.newParseErrorWith("Too many digits detected in number", len, "", "Too many digits detected in number", end, "");
    }
    throw reader.newParseErrorWith("Error parsing number", len, "", message, messageArgument, ". Error parsing number");
  }
}
