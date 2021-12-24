package io.avaje.jsonb.diesel;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * DslJson writes JSON into JsonWriter which has two primary modes of operation:
 * <p>
 * * targeting specific output stream
 * * buffering the entire response in memory
 * <p>
 * In both cases JsonWriter writes into an byte[] buffer.
 * If stream is used as target, it will copy buffer into the stream whenever there is no more room in buffer for new data.
 * If stream is not used as target, it will grow the buffer to hold the encoded result.
 * To use stream as target reset(OutputStream) must be called before processing.
 * This class provides low level methods for JSON serialization.
 * <p>
 * After the processing is done,
 * in case then stream was used as target, flush() must be called to copy the remaining of the buffer into stream.
 * When entire response was buffered in memory, buffer can be copied to stream or resulting byte[] can be used directly.
 * <p>
 * For maximum performance JsonWriter instances should be reused (to avoid allocation of new byte[] buffer instances).
 * They should not be shared across threads (concurrently) so for Thread reuse it's best to use patterns such as ThreadLocal.
 */
final class JGenerator {

  private static final Charset UTF_8 = StandardCharsets.UTF_8;

  static final byte[] NULL = "null".getBytes(StandardCharsets.UTF_8);
  static final byte[] TRUE = "true".getBytes(StandardCharsets.UTF_8);
  static final byte[] FALSE = "false".getBytes(StandardCharsets.UTF_8);

  static final byte OBJECT_START = '{';
  static final byte OBJECT_END = '}';
  static final byte ARRAY_START = '[';
  static final byte ARRAY_END = ']';
  static final byte NEWLINE = '\n';
  static final byte COMMA = ',';
  static final byte SEMI = ':';
  static final byte QUOTE = '"';
  static final byte ESCAPE = '\\';

  private static final int OP_START = 1;
  private static final int OP_FIELD = 3;
  private static final int OP_END = 4;

  private int lastOp;
  private int position;
  private long flushed;
  private OutputStream target;
  private byte[] buffer;

  private final Grisu3.FastDtoaBuilder doubleBuilder = new Grisu3.FastDtoaBuilder();

  JGenerator() {
    this(512);
  }

  JGenerator(final int size) {
    this(new byte[size]);
  }

  JGenerator(final byte[] buffer) {
    this.buffer = buffer;
  }

  /**
   * Resets the writer - specifies the target stream and sets the position in buffer to 0.
   * If stream is set to null, JsonWriter will work in growing byte[] buffer mode (entire response will be buffered in memory).
   *
   * @param stream sets/clears the target stream
   */
  public void reset(OutputStream stream) {
    position = 0;
    target = stream;
    flushed = 0;
  }

  byte[] ensureCapacity(final int free) {
    if (position + free >= buffer.length) {
      enlargeOrFlush(position, free);
    }
    return buffer;
  }

  void advance(int size) {
    position += size;
  }

  private void enlargeOrFlush(final int size, final int padding) {
    if (target != null) {
      try {
        target.write(buffer, 0, size);
      } catch (IOException ex) {
        throw new SerializationException("Unable to write to target stream.", ex);
      }
      position = 0;
      flushed += size;
      if (padding > buffer.length) {
        buffer = Arrays.copyOf(buffer, buffer.length + buffer.length / 2 + padding);
      }
    } else {
      buffer = Arrays.copyOf(buffer, buffer.length + buffer.length / 2 + padding);
    }
  }

  private void writeByte(final byte value) {
    if (position == buffer.length) {
      enlargeOrFlush(position, 0);
    }
    buffer[position++] = value;
  }

  private void write(final String value) {
    final int len = value.length();
    if (position + (len << 2) + (len << 1) + 2 >= buffer.length) {
      enlargeOrFlush(position, (len << 2) + (len << 1) + 2);
    }
    final byte[] _result = buffer;
    _result[position] = QUOTE;
    int cur = position + 1;
    for (int i = 0; i < len; i++) {
      final char c = value.charAt(i);
      if (c > 31 && c != '"' && c != '\\' && c < 126) {
        _result[cur++] = (byte) c;
      } else {
        writeQuotedString(value, i, cur, len);
        return;
      }
    }
    _result[cur] = QUOTE;
    position = cur + 1;
  }

//  private void writeString(final CharSequence value) {
//    final int len = value.length();
//    if (position + (len << 2) + (len << 1) + 2 >= buffer.length) {
//      enlargeOrFlush(position, (len << 2) + (len << 1) + 2);
//    }
//    final byte[] _result = buffer;
//    _result[position] = QUOTE;
//    int cur = position + 1;
//    for (int i = 0; i < len; i++) {
//      final char c = value.charAt(i);
//      if (c > 31 && c != '"' && c != '\\' && c < 126) {
//        _result[cur++] = (byte) c;
//      } else {
//        writeQuotedString(value, i, cur, len);
//        return;
//      }
//    }
//    _result[cur] = QUOTE;
//    position = cur + 1;
//  }

  private void writeQuotedString(final CharSequence str, int i, int cur, final int len) {
    final byte[] _result = this.buffer;
    for (; i < len; i++) {
      final char c = str.charAt(i);
      if (c == '"') {
        _result[cur++] = ESCAPE;
        _result[cur++] = QUOTE;
      } else if (c == '\\') {
        _result[cur++] = ESCAPE;
        _result[cur++] = ESCAPE;
      } else if (c < 32) {
        if (c == 8) {
          _result[cur++] = ESCAPE;
          _result[cur++] = 'b';
        } else if (c == 9) {
          _result[cur++] = ESCAPE;
          _result[cur++] = 't';
        } else if (c == 10) {
          _result[cur++] = ESCAPE;
          _result[cur++] = 'n';
        } else if (c == 12) {
          _result[cur++] = ESCAPE;
          _result[cur++] = 'f';
        } else if (c == 13) {
          _result[cur++] = ESCAPE;
          _result[cur++] = 'r';
        } else {
          _result[cur] = ESCAPE;
          _result[cur + 1] = 'u';
          _result[cur + 2] = '0';
          _result[cur + 3] = '0';
          switch (c) {
            case 0:
              _result[cur + 4] = '0';
              _result[cur + 5] = '0';
              break;
            case 1:
              _result[cur + 4] = '0';
              _result[cur + 5] = '1';
              break;
            case 2:
              _result[cur + 4] = '0';
              _result[cur + 5] = '2';
              break;
            case 3:
              _result[cur + 4] = '0';
              _result[cur + 5] = '3';
              break;
            case 4:
              _result[cur + 4] = '0';
              _result[cur + 5] = '4';
              break;
            case 5:
              _result[cur + 4] = '0';
              _result[cur + 5] = '5';
              break;
            case 6:
              _result[cur + 4] = '0';
              _result[cur + 5] = '6';
              break;
            case 7:
              _result[cur + 4] = '0';
              _result[cur + 5] = '7';
              break;
            case 11:
              _result[cur + 4] = '0';
              _result[cur + 5] = 'B';
              break;
            case 14:
              _result[cur + 4] = '0';
              _result[cur + 5] = 'E';
              break;
            case 15:
              _result[cur + 4] = '0';
              _result[cur + 5] = 'F';
              break;
            case 16:
              _result[cur + 4] = '1';
              _result[cur + 5] = '0';
              break;
            case 17:
              _result[cur + 4] = '1';
              _result[cur + 5] = '1';
              break;
            case 18:
              _result[cur + 4] = '1';
              _result[cur + 5] = '2';
              break;
            case 19:
              _result[cur + 4] = '1';
              _result[cur + 5] = '3';
              break;
            case 20:
              _result[cur + 4] = '1';
              _result[cur + 5] = '4';
              break;
            case 21:
              _result[cur + 4] = '1';
              _result[cur + 5] = '5';
              break;
            case 22:
              _result[cur + 4] = '1';
              _result[cur + 5] = '6';
              break;
            case 23:
              _result[cur + 4] = '1';
              _result[cur + 5] = '7';
              break;
            case 24:
              _result[cur + 4] = '1';
              _result[cur + 5] = '8';
              break;
            case 25:
              _result[cur + 4] = '1';
              _result[cur + 5] = '9';
              break;
            case 26:
              _result[cur + 4] = '1';
              _result[cur + 5] = 'A';
              break;
            case 27:
              _result[cur + 4] = '1';
              _result[cur + 5] = 'B';
              break;
            case 28:
              _result[cur + 4] = '1';
              _result[cur + 5] = 'C';
              break;
            case 29:
              _result[cur + 4] = '1';
              _result[cur + 5] = 'D';
              break;
            case 30:
              _result[cur + 4] = '1';
              _result[cur + 5] = 'E';
              break;
            default:
              _result[cur + 4] = '1';
              _result[cur + 5] = 'F';
              break;
          }
          cur += 6;
        }
      } else if (c < 0x007F) {
        _result[cur++] = (byte) c;
      } else {
        final int cp = Character.codePointAt(str, i);
        if (Character.isSupplementaryCodePoint(cp)) {
          i++;
        }
        if (cp == 0x007F) {
          _result[cur++] = (byte) cp;
        } else if (cp <= 0x7FF) {
          _result[cur++] = (byte) (0xC0 | ((cp >> 6) & 0x1F));
          _result[cur++] = (byte) (0x80 | (cp & 0x3F));
        } else if ((cp < 0xD800) || (cp > 0xDFFF && cp <= 0xFFFF)) {
          _result[cur++] = (byte) (0xE0 | ((cp >> 12) & 0x0F));
          _result[cur++] = (byte) (0x80 | ((cp >> 6) & 0x3F));
          _result[cur++] = (byte) (0x80 | (cp & 0x3F));
        } else if (cp >= 0x10000 && cp <= 0x10FFFF) {
          _result[cur++] = (byte) (0xF0 | ((cp >> 18) & 0x07));
          _result[cur++] = (byte) (0x80 | ((cp >> 12) & 0x3F));
          _result[cur++] = (byte) (0x80 | ((cp >> 6) & 0x3F));
          _result[cur++] = (byte) (0x80 | (cp & 0x3F));
        } else {
          throw new SerializationException("Unknown unicode codepoint in string! " + Integer.toHexString(cp));
        }
      }
    }
    _result[cur] = QUOTE;
    position = cur + 1;
  }

  @SuppressWarnings("deprecation")
  private void writeAscii(final String value) {
    final int len = value.length();
    if (position + len >= buffer.length) {
      enlargeOrFlush(position, len);
    }
    value.getBytes(0, len, buffer, position);
    position += len;
  }

  private void writeAscii(final byte[] buf) {
    final int len = buf.length;
    if (position + len >= buffer.length) {
      enlargeOrFlush(position, len);
    }
    System.arraycopy(buf, 0, buffer, position, buf.length);
    position += len;
  }

  private void writeBase64(final byte[] value) {
    if (position + (value.length << 1) + 2 >= buffer.length) {
      enlargeOrFlush(position, (value.length << 1) + 2);
    }
    buffer[position++] = '"';
    position += Base64.encodeToBytes(value, buffer, position);
    buffer[position++] = '"';
  }

  void writeDouble(final double value) {
    if (value == Double.POSITIVE_INFINITY) {
      writeAscii("\"Infinity\"");
    } else if (value == Double.NEGATIVE_INFINITY) {
      writeAscii("\"-Infinity\"");
    } else if (value != value) {
      writeAscii("\"NaN\"");
    } else if (value == 0.0) {
      writeAscii("0.0");
    } else {
      if (Grisu3.tryConvert(value, doubleBuilder)) {
        if (position + 24 >= buffer.length) {
          enlargeOrFlush(position, 24);
        }
        final int len = doubleBuilder.copyTo(buffer, position);
        position += len;
      } else {
        writeAscii(Double.toString(value));
      }
    }
  }

  @Override
  public String toString() {
    return new String(buffer, 0, position, UTF_8);
  }

//  /**
//   * Current position in the buffer. When stream is not used, this is also equivalent
//   * to the size of the resulting JSON in bytes
//   *
//   * @return position in the populated buffer
//   */
//  public final int size() {
//    return position;
//  }
//
//  /**
//   * Total bytes currently flushed to stream
//   *
//   * @return bytes flushed
//   */
//  public final long flushed() {
//    return flushed;
//  }
//
//  /**
//   * Resets the writer - same as calling reset(OutputStream = null)
//   */
//  public final void reset() {
//    reset(null);
//  }


  public void flush() {
    if (target != null && position != 0) {
      try {
        target.write(buffer, 0, position);
      } catch (IOException ex) {
        throw new SerializationException("Unable to write to target stream.", ex);
      }
      flushed += position;
      position = 0;
    }
  }

  public void close() throws IOException {
    if (target != null && position != 0) {
      target.write(buffer, 0, position);
      position = 0;
      flushed = 0;
    }
  }

  private void prefixName() {
    if (lastOp == OP_END) {
      writeByte(JGenerator.COMMA);
    }
    lastOp = OP_FIELD;
  }

  private void prefixValue() {
    if (lastOp == OP_END) {
      writeByte(JGenerator.COMMA);
    }
    lastOp = OP_END;
  }

  public void writeStartObject() {
    if (lastOp == OP_END) {
      writeByte(JGenerator.COMMA);
    }
    writeByte(OBJECT_START);
    lastOp = OP_START;
  }

  public void writeEndObject() {
    writeByte(OBJECT_END);
    lastOp = OP_END;
  }

  public void writeStartArray() {
    writeByte(ARRAY_START);
    lastOp = OP_START;
  }

  public void writeEndArray() {
    writeByte(ARRAY_END);
    lastOp = OP_END;
  }

  public void writeFieldName(String name) {
    prefixName();
    write(name);
    writeByte(JGenerator.SEMI);
  }

  public void writeFieldName(byte[] escapedName) {
    prefixName();
    writeAscii(escapedName);
    writeByte(JGenerator.SEMI);
  }

  public void writeBinary(final byte[] value) {
    prefixValue();
    writeBase64(value);
  }

  public void writeNull() {
    prefixValue();
    writeAscii(NULL);
//    if ((position + 4) >= buffer.length) {
//      enlargeOrFlush(position, 0);
//    }
//    final int s = position;
//    final byte[] _result = buffer;
//    _result[s] = 'n';
//    _result[s + 1] = 'u';
//    _result[s + 2] = 'l';
//    _result[s + 3] = 'l';
//    position += 4;
  }

  public void writeBoolean(boolean value) {
    prefixValue();
    if (value) {
      writeAscii(TRUE);
    } else {
      writeAscii(FALSE);
    }
  }

  public void writeNumber(int value) {
    prefixValue();
    NumberConverter.writeInt(value, this);
  }

  public void writeNumber(long value) {
    prefixValue();
    NumberConverter.writeLong(value, this);
  }

  public void writeValue(String value) {
    prefixValue();
    write(value);
  }

  public void writeNewLine() {
    writeByte(NEWLINE);
  }
}
