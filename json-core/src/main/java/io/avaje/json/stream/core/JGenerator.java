package io.avaje.json.stream.core;

import io.avaje.json.JsonIoException;
import io.avaje.json.stream.JsonOutput;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

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
class JGenerator implements JsonGenerator {

  private static final Charset UTF_8 = StandardCharsets.UTF_8;

  private static final byte[] NULL = "null".getBytes(StandardCharsets.UTF_8);
  private static final byte[] TRUE = "true".getBytes(StandardCharsets.UTF_8);
  private static final byte[] FALSE = "false".getBytes(StandardCharsets.UTF_8);
  private static final byte[] INDENT = "  ".getBytes(StandardCharsets.UTF_8);

  private static final byte OBJECT_START = '{';
  private static final byte OBJECT_END = '}';
  private static final byte ARRAY_START = '[';
  private static final byte ARRAY_END = ']';
  private static final byte NEWLINE = '\n';
  private static final byte COMMA = ',';
  private static final byte SEMI = ':';
  private static final byte QUOTE = '"';
  private static final byte ESCAPE = '\\';
  private static final byte SPACE = ' ';

  private static final int OP_START = 1;
  private static final int OP_FIELD = 3;
  private static final int OP_END = 4;

  private final Grisu3.FastDtoaBuilder doubleBuilder = new Grisu3.FastDtoaBuilder();
  private byte[] buffer;
  private JsonOutput target;
  private int lastOp;
  private int position;
  private boolean pretty;
  private int depth;
  private final Deque<JsonNames> nameStack = new ArrayDeque<>();
  private JsonNames currentNames;
  private boolean allNames;
  private boolean incomplete;

  JGenerator() {
    this(512);
  }

  JGenerator(final int size) {
    this(new byte[size]);
  }

  JGenerator(final byte[] buffer) {
    this.buffer = buffer;
  }

  @Override
  public JsonGenerator prepare(JsonOutput targetStream) {
    target = targetStream;
    lastOp = 0;
    position = 0;
    pretty = false;
    nameStack.clear();
    allNames = false;
    currentNames = null;
    incomplete = false;
    return this;
  }

  int position() {
    return position;
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
        throw new JsonIoException("Unable to write to target stream.", ex);
      }
      position = 0;
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

  private void writeString(final String value) {
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

  private void writeQuotedString(final String str, int i, int cur, final int len) {
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
        switch (c) {
          case 8:
            _result[cur++] = ESCAPE;
            _result[cur++] = 'b';
            break;
          case 9:
            _result[cur++] = ESCAPE;
            _result[cur++] = 't';
            break;
          case 10:
            _result[cur++] = ESCAPE;
            _result[cur++] = 'n';
            break;
          case 12:
            _result[cur++] = ESCAPE;
            _result[cur++] = 'f';
            break;
          case 13:
            _result[cur++] = ESCAPE;
            _result[cur++] = 'r';
            break;
          default:
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
            break;
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
          throw new JsonIoException("Unknown unicode codepoint in string! " + Integer.toHexString(cp));
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
    buffer[position++] = QUOTE;
    position += Base64.encodeToBytes(value, buffer, position);
    buffer[position++] = QUOTE;
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
    } else if (Grisu3.tryConvert(value, doubleBuilder)) {
      if (position + 24 >= buffer.length) {
        enlargeOrFlush(position, 24);
      }
      final int len = doubleBuilder.copyTo(buffer, position);
      position += len;
    } else {
      writeAscii(Double.toString(value));
    }
  }

  @Override
  public String toString() {
    return new String(buffer, 0, position, UTF_8);
  }

  @Override
  public byte[] toByteArray() {
    if (target != null) {
      throw new IllegalStateException("Method is not available when targeting stream");
    }
    return Arrays.copyOf(buffer, position);
  }

  @Override
  public void markIncomplete() {
    incomplete = true;
  }

  @Override
  public void flush() {
    if (target != null) {
      try {
        if (position != 0) {
          target.writeLast(buffer, 0, position);
          position = 0;
        }
        target.flush();
      } catch (IOException ex) {
        throw new JsonIoException("Unable to write to target stream.", ex);
      }
    }
  }

  @Override
  public void close() {
    if (incomplete) return;
    flush();
  }

  private void prefixName() {
    if (lastOp == OP_END) {
      writeByte(COMMA);
    }
    lastOp = OP_FIELD;
    if (pretty) {
      prettyIndent();
    }
  }

  private void prefixValue() {
    if (lastOp == OP_END) {
      writeByte(COMMA);
    }
    if (pretty && (depth > 1)) {
      prettyIndent();
    }
    lastOp = OP_END;
  }

  @Override
  public void pretty(boolean pretty) {
    this.pretty = pretty;
  }

  private void writeStartObject() {
    if (pretty) {
      depth++;
    }
    if (lastOp == OP_END) {
      writeByte(COMMA);
    }
    writeByte(OBJECT_START);
    lastOp = OP_START;
  }

  @Override
  public void startObject() {
    writeStartObject();
    if (currentNames != null && !allNames) {
      nameStack.push(currentNames);
      currentNames = JsonNames.EMPTY;
    }
  }

  @Override
  public void startObject(JsonNames nextNames) {
    writeStartObject();
    if (currentNames != null) {
      nameStack.push(currentNames);
    }
    currentNames = nextNames;
  }

  @Override
  public void endObject() {
    if (!allNames) {
      currentNames = nameStack.poll();
    }
    if (pretty) {
      depth--;
      prettyIndent();
    }
    writeByte(OBJECT_END);
    lastOp = OP_END;
  }

  @Override
  public void startArray() {
    writeByte(ARRAY_START);
    lastOp = OP_START;
    if (pretty) {
      depth++;
    }
  }

  @Override
  public void endArray() {
    if (pretty) {
      depth--;
      prettyIndent();
    }
    writeByte(ARRAY_END);
    lastOp = OP_END;
  }

  private void prettyIndent() {
    writeByte(NEWLINE);
    for (int i = 0; i < depth; i++) {
      writeAscii(INDENT);
    }
  }

  @Override
  public void allNames(JsonNames names) {
    allNames = true;
    currentNames = names;
  }

  @Override
  public void writeName(int namePos) {
    prefixName();
    writeAscii(currentNames.key(namePos));
    writeColon();
  }

  @Override
  public void writeName(String name) {
    prefixName();
    writeString(name);
    writeColon();
  }

  private void writeColon() {
    writeByte(SEMI);
    if (pretty) {
      writeByte(SPACE);
    }
  }

  @Override
  public void writeNull() {
    prefixValue();
    writeAscii(NULL);
  }

  @Override
  public void write(boolean value) {
    prefixValue();
    if (value) {
      writeAscii(TRUE);
    } else {
      writeAscii(FALSE);
    }
  }

  @Override
  public void write(int value) {
    prefixValue();
    NumberWriter.writeInt(value, this);
  }

  @Override
  public void write(long value) {
    prefixValue();
    NumberWriter.writeLong(value, this);
  }

  @Override
  public void write(double value) {
    prefixValue();
    writeDouble(value);
  }

  @Override
  public void write(BigInteger value) {
    prefixValue();
    writeAscii(value.toString());
  }

  @Override
  public void write(BigDecimal value) {
    prefixValue();
    writeAscii(value.toString());
  }

  @Override
  public void write(String value) {
    prefixValue();
    writeString(value);
  }

  @Override
  public void write(byte[] value) {
    prefixValue();
    writeBase64(value);
  }

  @Override
  public void writeRaw(String value) {
    prefixValue();
    writeAscii(value);
  }

  @Override
  public void writeNewLine() {
    writeByte(NEWLINE);
    lastOp = 0;
  }
}
