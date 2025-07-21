package io.avaje.json.stream.core;

import io.avaje.json.JsonDataException;
import io.avaje.json.JsonEofException;
import io.avaje.json.JsonIoException;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Formatter;

/**
 * The json parsing implementation.
 */
class JParser implements JsonParser {

  enum ErrorInfo {
    WITH_STACK_TRACE,
    DESCRIPTION_AND_POSITION,
    DESCRIPTION_ONLY,
    MINIMAL
  }

  enum DoublePrecision {
    EXACT(0),
    HIGH(1),
    DEFAULT(3),
    LOW(4);

    final int level;

    DoublePrecision(int level) {
      this.level = level;
    }
  }

  enum UnknownNumberParsing {
    LONG_AND_BIGDECIMAL,
    LONG_AND_DOUBLE,
    BIGDECIMAL,
    DOUBLE
  }

  private static final boolean[] WHITESPACE = new boolean[256];
  private static final Charset utf8 = StandardCharsets.UTF_8;

  static {
    WHITESPACE[9 + 128] = true;
    WHITESPACE[10 + 128] = true;
    WHITESPACE[11 + 128] = true;
    WHITESPACE[12 + 128] = true;
    WHITESPACE[13 + 128] = true;
    WHITESPACE[32 + 128] = true;
    WHITESPACE[-96 + 128] = true;
    WHITESPACE[-31 + 128] = true;
    WHITESPACE[-30 + 128] = true;
    WHITESPACE[-29 + 128] = true;
  }

  private int tokenStart;
  private int nameEnd;
  private int currentIndex = 0;
  private long currentPosition = 0;
  private byte last = ' ';

  private int length;
  private final char[] tmp;

  byte[] buffer;
  char[] chars;
  private ByteArrayOutputStream readRawBuffer;
  private int readRawStartPosition;

  private InputStream stream;
  private int readLimit;
  // always leave some room for reading special stuff, so that buffer contains enough padding for such optimizations
  private int bufferLenWithExtraSpace;

  private final byte[] originalBuffer;
  private final int originalBufferLenWithExtraSpace;

  private final ArrayDeque<JsonNames> nameStack = new ArrayDeque<>();
  private JsonNames currentNames;

  final ErrorInfo errorInfo;
  final DoublePrecision doublePrecision;
  final int doubleLengthLimit;
  final UnknownNumberParsing unknownNumbers;
  final int maxNumberDigits;
  private final int maxStringBuffer;

  public JParser(
    final char[] tmp,
    final byte[] buffer,
    final int length,
    final ErrorInfo errorInfo,
    final DoublePrecision doublePrecision,
    final UnknownNumberParsing unknownNumbers,
    final int maxNumberDigits,
    final int maxStringBuffer) {
    this.tmp = tmp;
    this.buffer = buffer;
    this.length = length;
    this.bufferLenWithExtraSpace = buffer.length - 38; // maximum padding is for uuid
    this.chars = tmp;
    this.errorInfo = errorInfo;
    this.doublePrecision = doublePrecision;
    this.unknownNumbers = unknownNumbers;
    this.maxNumberDigits = maxNumberDigits;
    this.maxStringBuffer = maxStringBuffer;
    this.doubleLengthLimit = 15 + doublePrecision.level;
    this.originalBuffer = buffer;
    this.originalBufferLenWithExtraSpace = bufferLenWithExtraSpace;
  }

  /**
   * Reset reader after processing input
   * It will release reference to provided byte[] or InputStream input
   */
  @Override
  public final void close() {
    buffer = originalBuffer;
    bufferLenWithExtraSpace = originalBufferLenWithExtraSpace;
    last = ' ';
    currentIndex = 0;
    length = 0;
    readLimit = 0;
    nameStack.clear();
    stream = null;
  }

  @Override
  public final JParser process(final InputStream newStream) {
    nameStack.clear();
    currentPosition = 0;
    currentIndex = 0;
    stream = newStream;
    if (newStream != null) {
      readLimit = Math.min(length, bufferLenWithExtraSpace);
      final int available = readFully(buffer, newStream, 0);
      readLimit = Math.min(available, bufferLenWithExtraSpace);
      length = available;
    }
    return this;
  }

  @Override
  public final JParser process(final byte[] newBuffer, final int newLength) {
    if (newBuffer != null) {
      buffer = newBuffer;
      bufferLenWithExtraSpace = buffer.length - 38; // maximum padding is for uuid
    }
    if (newLength > buffer.length) {
      throw new IllegalArgumentException("length can't be longer than buffer.length");
    }
    nameStack.clear();
    currentIndex = 0;
    length = newLength;
    stream = null;
    readLimit = newLength;
    return this;
  }

  /**
   * Valid length of the input buffer.
   */
  final int length() {
    return length;
  }

  @Override
  public final String toString() {
    return new String(buffer, 0, length, utf8);
  }

  private static int readFully(final byte[] buffer, final InputStream stream, final int offset) {
    try {
      int read;
      int position = offset;
      while (position < buffer.length && (read = stream.read(buffer, position, buffer.length - position)) != -1) {
        position += read;
      }
      return position;
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  private static class EmptyEOFException extends EOFException {

    private static final long serialVersionUID = 1L;

    @Override
    public Throwable fillInStackTrace() {
      return this;
    }
  }

  private static final EOFException eof = new EmptyEOFException();

  /**
   * Read next byte from the JSON input.
   * If buffer has been read in full IOException will be thrown
   *
   * @return next byte
   */
  private byte read() {
    if (stream != null && currentIndex > readLimit) {
      prepareNextBlock();
    }
    if (currentIndex >= length) {
      throw new JsonEofException("Unexpected end of JSON input");
    }
    return last = buffer[currentIndex++];
  }

  private int prepareNextBlock() {
    final int len = length - currentIndex;
    if (readRawBuffer != null) {
      // performing readRaw()
      readRawBuffer.write(buffer, readRawStartPosition, currentIndex - readRawStartPosition);
      readRawStartPosition = 0;
      if (readRawBuffer.size() > maxStringBuffer) {
        throw newParseErrorWith("Maximum buffer limit exceeded for raw content", maxStringBuffer);
      }
    }
    System.arraycopy(buffer, currentIndex, buffer, 0, len);
    final int available = readFully(buffer, stream, len);
    currentPosition += currentIndex;
    if (available == len) {
      readLimit = length - currentIndex;
      length = readLimit;
    } else {
      readLimit = Math.min(available, bufferLenWithExtraSpace);
      this.length = available;
    }
    currentIndex = 0;
    return available;
  }

  final boolean isEndOfStream() {
    if (stream == null) {
      return length == currentIndex;
    }
    if (length != currentIndex) {
      return false;
    }
    return prepareNextBlock() == 0;
  }

  /**
   * Which was last byte read from the JSON input.
   * JsonReader doesn't allow to go back, but it remembers previously read byte
   *
   * @return which was the last byte read
   */
  @Override
  public final byte currentToken() {
    return currentIndex == 0 ? nextToken() : last;
  }

  @Override
  public final String location() {
    final StringBuilder error = new StringBuilder(60);
    positionDescription(0, error);
    return error.toString();
  }

  private void positionDescription(int offset, StringBuilder error) {
    error.append("at position: ").append(positionInStream(offset));
    if (currentIndex > offset) {
      try {
        int maxLen = Math.min(currentIndex - offset, 20);
        String prefix = new String(buffer, currentIndex - offset - maxLen, maxLen, utf8);
        error.append(", following: `");
        error.append(prefix);
        error.append('`');
      } catch (Exception ignore) {
      }
    }
    if (currentIndex - offset < readLimit) {
      try {
        int maxLen = Math.min(readLimit - currentIndex + offset, 20);
        String suffix = new String(buffer, currentIndex - offset, maxLen, utf8);
        error.append(", before: `");
        error.append(suffix);
        error.append('`');
      } catch (Exception ignore) {
      }
    }
  }

  final int getCurrentIndex() {
    return currentIndex;
  }

  final int scanNumber() {
    tokenStart = currentIndex - 1;
    int i = 1;
    int ci = currentIndex;
    byte bb = last;
    while (ci < length) {
      bb = buffer[ci++];
      if (bb == ',' || bb == '}' || bb == ']') break;
      i++;
    }
    currentIndex += i - 1;
    last = bb;
    return tokenStart;
  }

  final char[] prepareBuffer(final int start, final int len) {
    if (len > maxNumberDigits) {
      throw newParseErrorWith("Too many digits detected in number", len, "Too many digits detected in number", len, "");
    }
    while (chars.length < len) {
      chars = Arrays.copyOf(chars, chars.length * 2);
    }
    final char[] _tmp = chars;
    final byte[] _buf = buffer;
    for (int i = 0; i < len; i++) {
      _tmp[i] = (char) _buf[start + i];
    }
    return _tmp;
  }

  final boolean allWhitespace(final int start, final int end) {
    final byte[] _buf = buffer;
    for (int i = start; i < end; i++) {
      if (!WHITESPACE[_buf[i] + 128]) return false;
    }
    return true;
  }

  final int findNonWhitespace(final int end) {
    final byte[] _buf = buffer;
    for (int i = end - 1; i > 0; i--) {
      if (!WHITESPACE[_buf[i] + 128]) return i + 1;
    }
    return 0;
  }

  /**
   * Read simple "ascii string" into temporary buffer.
   * String length must be obtained through getTokenStart and getCurrentToken
   *
   * @return temporary buffer
   */
  final char[] readSimpleQuote() {
    if (last != '"') throw newParseError("Expecting '\"' for string start");
    int ci = tokenStart = currentIndex;
    try {
      for (int i = 0; i < tmp.length; i++) {
        final byte bb = buffer[ci++];
        if (bb == '"') break;
        tmp[i] = (char) bb;
      }
    } catch (ArrayIndexOutOfBoundsException ignore) {
      throw newParseErrorAt("JSON string was not closed with a double quote", 0);
    }
    if (ci > length) throw newParseErrorAt("JSON string was not closed with a double quote", 0);
    currentIndex = ci;
    return tmp;
  }

  @Override
  public final int readInt() {
    if (isNullValue()) return 0;
    return NumberParser.deserializeInt(this);
  }

  @Override
  public final long readLong() {
    if (isNullValue()) return 0L;
    return NumberParser.deserializeLong(this);
  }

  @Override
  public final short readShort() {
    return NumberParser.deserializeShort(this);
  }

  @Override
  public final double readDouble() {
    if (isNullValue()) return 0D;
    return NumberParser.deserializeDouble(this);
  }

  @Override
  public final BigDecimal readDecimal() {
    return NumberParser.deserializeDecimal(this);
  }

  @Override
  public final BigInteger readBigInteger() {
    return NumberParser.deserializeBigInt(this);
  }

  @Override
  public final boolean readBoolean() {
    if (wasTrue()) {
      return true;
    } else if (wasFalse() || isNullValue()) {
      return false;
    }
    throw newParseErrorAt("Found invalid boolean value", 0);
  }

  /**
   * Read string from JSON input.
   * If values cache is used, string will be looked up from the cache.
   * <p>
   * String value must start and end with a double quote (").
   *
   * @return parsed string
   */
  @Override
  public final String readString() {
    final int len = parseString();
    //return valuesCache == null ? new String(chars, 0, len) : valuesCache.get(chars, len);
    return new String(chars, 0, len);
  }

  final int parseString() {
    final int startIndex = currentIndex;
    if (last != '"') throw newParseError("Expecting '\"' for string start");
    else if (currentIndex == length) throw newParseErrorAt("Premature end of JSON string", 0);

    byte bb;
    int ci = currentIndex;
    char[] _tmp = chars;
    final int remaining = length - currentIndex;
    int _tmpLen = Math.min(_tmp.length, remaining);
    int i = 0;
    while (i < _tmpLen) {
      bb = buffer[ci++];
      if (bb == '"') {
        currentIndex = ci;
        return i;
      }
      // If we encounter a backslash, which is a beginning of an escape sequence
      // or a high bit was set - indicating an UTF-8 encoded multibyte character,
      // there is no chance that we can decode the string without instantiating
      // a temporary buffer, so quit this loop
      if ((bb ^ '\\') < 1) break;
      _tmp[i++] = (char) bb;
    }
    if (i == _tmp.length) {
      final int newSize = chars.length * 2;
      if (newSize > maxStringBuffer) {
        throw newParseErrorWith("Maximum string buffer limit exceeded", maxStringBuffer);
      }
      _tmp = chars = Arrays.copyOf(chars, newSize);
    }
    _tmpLen = _tmp.length;
    currentIndex = ci;
    int soFar = --currentIndex - startIndex;

    while (!isEndOfStream()) {
      int bc = read();
      if (bc == '"') {
        return soFar;
      }

      if (bc == '\\') {
        if (soFar >= _tmpLen - 6) {
          final int newSize = chars.length * 2;
          if (newSize > maxStringBuffer) {
            throw newParseErrorWith("Maximum string buffer limit exceeded", maxStringBuffer);
          }
          _tmp = chars = Arrays.copyOf(chars, newSize);
          _tmpLen = _tmp.length;
        }
        bc = buffer[currentIndex++];

        switch (bc) {
          case 'b':
            bc = '\b';
            break;
          case 't':
            bc = '\t';
            break;
          case 'n':
            bc = '\n';
            break;
          case 'f':
            bc = '\f';
            break;
          case 'r':
            bc = '\r';
            break;
          case '"':
          case '/':
          case '\\':
            break;
          case 'u':
            bc = (hexToInt(buffer[currentIndex++]) << 12) +
              (hexToInt(buffer[currentIndex++]) << 8) +
              (hexToInt(buffer[currentIndex++]) << 4) +
              hexToInt(buffer[currentIndex++]);
            break;

          default:
            throw newParseErrorWith("Invalid escape combination detected", bc);
        }
      } else if ((bc & 0x80) != 0) {
        if (soFar >= _tmpLen - 4) {
          final int newSize = chars.length * 2;
          if (newSize > maxStringBuffer) {
            throw newParseErrorWith("Maximum string buffer limit exceeded", maxStringBuffer);
          }
          _tmp = chars = Arrays.copyOf(chars, newSize);
          _tmpLen = _tmp.length;
        }
        final int u2 = buffer[currentIndex++];
        if ((bc & 0xE0) == 0xC0) {
          bc = ((bc & 0x1F) << 6) + (u2 & 0x3F);
        } else {
          final int u3 = buffer[currentIndex++];
          if ((bc & 0xF0) == 0xE0) {
            bc = ((bc & 0x0F) << 12) + ((u2 & 0x3F) << 6) + (u3 & 0x3F);
          } else {
            final int u4 = buffer[currentIndex++];
            if ((bc & 0xF8) == 0xF0) {
              bc = ((bc & 0x07) << 18) + ((u2 & 0x3F) << 12) + ((u3 & 0x3F) << 6) + (u4 & 0x3F);
            } else {
              // there are legal 5 & 6 byte combinations, but none are _valid_
              throw newParseErrorAt("Invalid unicode character detected", 0);
            }

            if (bc >= 0x10000) {
              // check if valid unicode
              if (bc >= 0x110000) {
                throw newParseErrorAt("Invalid unicode character detected", 0);
              }

              // split surrogates
              final int sup = bc - 0x10000;
              _tmp[soFar++] = (char) ((sup >>> 10) + 0xd800);
              _tmp[soFar++] = (char) ((sup & 0x3ff) + 0xdc00);
              continue;
            }
          }
        }
      } else if (soFar >= _tmpLen) {
        final int newSize = chars.length * 2;
        if (newSize > maxStringBuffer) {
          throw newParseErrorWith("Maximum string buffer limit exceeded", maxStringBuffer);
        }
        _tmp = chars = Arrays.copyOf(chars, newSize);
        _tmpLen = _tmp.length;
      }

      _tmp[soFar++] = (char) bc;
    }
    throw newParseErrorAt("JSON string was not closed with a double quote", 0);
  }

  private int hexToInt(final byte value) {
    if (value >= '0' && value <= '9') return value - 0x30;
    if (value >= 'A' && value <= 'F') return value - 0x37;
    if (value >= 'a' && value <= 'f') return value - 0x57;
    throw newParseErrorWith("Could not parse unicode escape, expected a hexadecimal digit", value);
  }

  private boolean wasWhiteSpace() {
    switch (last) {
      case 9:
      case 10:
      case 11:
      case 12:
      case 13:
      case 32:
      case -96:
        return true;
      case -31:
        if (currentIndex + 1 < length && buffer[currentIndex] == -102 && buffer[currentIndex + 1] == -128) {
          currentIndex += 2;
          last = ' ';
          return true;
        }
        return false;
      case -30:
        if (currentIndex + 1 >= length) {
          return false;
        }
        final byte b1 = buffer[currentIndex];
        final byte b2 = buffer[currentIndex + 1];
        if (b1 == -127 && b2 == -97) {
          currentIndex += 2;
          last = ' ';
          return true;
        }
        if (b1 != -128) return false;
        switch (b2) {
          case -128:
          case -127:
          case -126:
          case -125:
          case -124:
          case -123:
          case -122:
          case -121:
          case -120:
          case -119:
          case -118:
          case -88:
          case -87:
          case -81:
            currentIndex += 2;
            last = ' ';
            return true;
          default:
            return false;
        }
      case -29:
        if (currentIndex + 1 < length && buffer[currentIndex] == -128 && buffer[currentIndex + 1] == -128) {
          currentIndex += 2;
          last = ' ';
          return true;
        }
        return false;
      default:
        return false;
    }
  }

  @Override
  public final boolean hasNextStreamElement() {
    if (currentIndex >= length) {
      return false;
    }
    try {
      byte nextToken = nextToken();
      if (nextToken == ',') {
        nextToken();
        return true;
      }
      return (nextToken != ']');
    } catch (JsonEofException e) {
      // expected when streaming new line delimited content with trailing whitespace
      return false;
    }
  }

  @Override
  public final boolean hasNextElement() {
    if (currentIndex >= length) {
      return false;
    }
    final boolean firstElement = last == '[';
    byte nextToken = nextToken();
    if (nextToken == ']') {
      return false;
    }
    if (nextToken == ',') {
      nextToken();
      return true;
    }
    if (firstElement) {
      return true;
    }
    throw newParseError("Expecting ']' or ',' for end of array element");
  }

  /**
   * Read next token (byte) from input JSON.
   * Whitespace will be skipped and next non-whitespace byte will be returned.
   *
   * @return next non-whitespace byte in the JSON input
   */
  @Override
  public final byte nextToken() {
    read();
    if (WHITESPACE[last + 128]) {
      while (wasWhiteSpace()) {
        read();
      }
    }
    return last;
  }

  final long positionInStream(final int offset) {
    return currentPosition + currentIndex - offset;
  }

  final int calcHash() {
    if (last != '"') throw newParseError("Expecting '\"' for attribute name start");
    tokenStart = currentIndex;
    int ci = currentIndex;
    int hash = 0x811c9dc5;
    if (stream != null) {
      while (ci < readLimit) {
        byte b = buffer[ci];
        if (b == '\\') {
          if (ci == readLimit - 1) {
            return calcHashAndCopyName(hash, ci);
          }
          b = buffer[++ci];
        } else if (b == '"') {
          break;
        }
        ci++;
        hash ^= b;
        hash *= 0x1000193;
      }
      if (ci >= readLimit) {
        return calcHashAndCopyName(hash, ci);
      }
      nameEnd = currentIndex = ci + 1;
    } else {
      //TODO: use length instead!? this will read data after used buffer size
      while (ci < buffer.length) {
        byte b = buffer[ci++];
        if (b == '\\') {
          if (ci == buffer.length) throw newParseError("Expecting '\"' for attribute name end");
          b = buffer[ci++];
        } else if (b == '"') {
          break;
        }
        hash ^= b;
        hash *= 0x1000193;
      }
      nameEnd = currentIndex = ci;
    }
    return hash;
  }

  private int lastNameLen;

  private int calcHashAndCopyName(int hash, int ci) {
    int soFar = ci - tokenStart;
    long startPosition = currentPosition - soFar;
    while (chars.length < soFar) {
      chars = Arrays.copyOf(chars, chars.length * 2);
    }
    int i = 0;
    for (; i < soFar; i++) {
      chars[i] = (char) buffer[i + tokenStart];
    }
    currentIndex = ci;
    do {
      byte b = read();
      if (b == '\\') {
        b = read();
      } else if (b == '"') {
        nameEnd = -1;
        lastNameLen = i;
        return hash;
      }
      if (i == chars.length) {
        chars = Arrays.copyOf(chars, chars.length * 2);
      }
      chars[i++] = (char) b;
      hash ^= b;
      hash *= 0x1000193;
    } while (!isEndOfStream());
    //TODO: check offset
    throw newParseErrorAt("JSON string was not closed with a double quote", (int) startPosition);
  }

  private String lastFieldName() {
    if (stream != null && nameEnd == -1) {
      return new String(chars, 0, lastNameLen);
    }
    return new String(buffer, tokenStart, nameEnd - tokenStart - 1, StandardCharsets.UTF_8);
  }

  private byte skipString() {
    byte c = read();
    boolean inEscape = false;
    while (c != '"' || inEscape) {
      inEscape = !inEscape && c == '\\';
      c = read();
    }
    return nextToken();
  }

  @Override
  public final String readRaw() {
    readRawStartPosition = currentIndex - 1;
    if (stream != null) {
      readRawBuffer = new ByteArrayOutputStream();
    }
    skipValue();
    if (stream != null) {
      try {
        if (readRawBuffer.size() > 0) {
          // streaming and exceeded a single buffer, append the remaining
          readRawBuffer.write(buffer, 0, currentIndex);
          return new String(readRawBuffer.toByteArray(), utf8);
        }
      } finally {
        readRawBuffer = null;
      }
    }
    return new String(buffer, readRawStartPosition, currentIndex - readRawStartPosition, utf8);
  }

  @Override
  public final void skipValue() {
    skipNextValue();
    // go back one as nextToken() is called next
    last = buffer[--currentIndex];
  }

  /**
   * Skip to next non-whitespace token (byte)
   * Will not allocate memory while skipping over JSON input.
   *
   * @return next non-whitespace byte
   */
  private byte skipNextValue() {
    switch (last) {
      case '"':
        return skipString();
      case '{':
        return skipObject();
      case '[':
        return skipArray();
      case 'n':
        if (!isNullValue()) throw newParseErrorAt("Expecting 'null' for null constant", 0);
        return nextToken();
      case 't':
        if (!wasTrue()) throw newParseErrorAt("Expecting 'true' for true constant", 0);
        return nextToken();
      case 'f':
        if (!wasFalse()) throw newParseErrorAt("Expecting 'false' for false constant", 0);
        return nextToken();
      default:
        break;
    }
    while (last != ',' && last != '}' && last != ']') {
      read();
    }
    return last;
  }

  private byte skipArray() {
    nextToken();
    byte nextToken = skipNextValue();
    while (nextToken == ',') {
      nextToken();
      nextToken = skipNextValue();
    }
    if (nextToken != ']') throw newParseError("Expecting ']' for array end");
    return nextToken();
  }

  private byte skipObject() {
    byte nextToken = nextToken();
    if (nextToken == '}') return nextToken();
    if (nextToken == '"') {
      nextToken = skipString();
    } else {
      throw newParseError("Expecting '\"' for attribute name");
    }
    if (nextToken != ':') throw newParseError("Expecting ':' after attribute name");
    nextToken();
    nextToken = skipNextValue();
    while (nextToken == ',') {
      nextToken = nextToken();
      if (nextToken == '"') {
        nextToken = skipString();
      } else {
        throw newParseError("Expecting '\"' for attribute name");
      }
      if (nextToken != ':') throw newParseError("Expecting ':' after attribute name");
      nextToken();
      nextToken = skipNextValue();
    }
    if (nextToken != '}') throw newParseError("Expecting '}' for object end");
    return nextToken();
  }

  @Override
  public final byte[] readBinary() {
    if (stream != null && Base64.findEnd(buffer, currentIndex) == buffer.length) {
      final int len = parseString();
      final byte[] input = new byte[len];
      for (int i = 0; i < input.length; i++) {
        input[i] = (byte) chars[i];
      }
      return Base64.decodeFast(input, 0, len);
    }
    if (last != '"') throw newParseError("Expecting '\"' for base64 start");
    final int start = currentIndex;
    currentIndex = Base64.findEnd(buffer, start);
    last = buffer[currentIndex++];
    if (last != '"') throw newParseError("Expecting '\"' for base64 end");
    return Base64.decodeFast(buffer, start, currentIndex - 1);
  }

  @Override
  public final String nextField() {
    if (currentNames != null) {
      final int hash = calcHash();
      String key = currentNames.lookup(hash);
      if (key == null) {
        key = lastFieldName();
      }
      if ((read() != ':') && (!wasWhiteSpace() || nextToken() != ':')) {
        throw newParseError("Expecting ':' after attribute name");
      }
      nextToken(); // position to read the value/next
      return key;
    }
    return readKey();
  }

  /**
   * Read key value of JSON input.
   */
  private String readKey() {
    final int len = parseString();
    final String key = new String(chars, 0, len);
    if (nextToken() != ':') throw newParseError("Expecting ':' after attribute name");
    nextToken();
    return key;
  }

  @Override
  public final boolean isNullValue() {
    if (last == 'n') {
      if (currentIndex + 2 < length
        && buffer[currentIndex] == 'u'
        && buffer[currentIndex + 1] == 'l'
        && buffer[currentIndex + 2] == 'l') {
        currentIndex += 3;
        last = 'l';
        return true;
      }
      throw newParseErrorAt("Invalid null constant found", 0);
    }
    return false;
  }

  /**
   * Checks if 'true' value is at current position.
   */
  private boolean wasTrue() {
    if (last == 't') {
      if (currentIndex + 2 < length
        && buffer[currentIndex] == 'r'
        && buffer[currentIndex + 1] == 'u'
        && buffer[currentIndex + 2] == 'e') {
        currentIndex += 3;
        last = 'e';
        return true;
      }
      throw newParseErrorAt("Invalid true constant found", 0);
    }
    return false;
  }

  /**
   * Checks if 'false' value is at current position.
   */
  private boolean wasFalse() {
    if (last == 'f') {
      if (currentIndex + 3 < length
        && buffer[currentIndex] == 'a'
        && buffer[currentIndex + 1] == 'l'
        && buffer[currentIndex + 2] == 's'
        && buffer[currentIndex + 3] == 'e') {
        currentIndex += 4;
        last = 'e';
        return true;
      }
      throw newParseErrorAt("Invalid false constant found", 0);
    }
    return false;
  }

  @Override
  public final void startStream() {
    if (last == '[') return;
    if (last == '{') {
      // go back one
      last = buffer[--currentIndex];
      return;
    }
    nextToken();
    if (last == '[') return;
    if (last == '{') {
      // go back one
      last = buffer[--currentIndex];
      return;
    }
    if (currentIndex >= length) throw newParseErrorAt("Unexpected end in JSON", 0, eof);
    throw newParseError("Expecting start of stream");
  }

  @Override
  public final void endStream() {
    // do nothing
  }

  /**
   * Parse array start
   */
  @Override
  public final void startArray() {
    if (last != '[' && nextToken() != '[') {
      if (currentIndex >= length) throw newParseErrorAt("Unexpected end in JSON", 0, eof);
      throw newParseError("Expecting '[' as array start");
    }
  }

  /**
   * Parse array end
   */
  @Override
  public final void endArray() {
    if (last != ']' && nextToken() != ']') {
      if (currentIndex >= length) throw newParseErrorAt("Unexpected end in JSON", 0, eof);
      throw newParseError("Expecting ']' as array end");
    }
  }

  /**
   * Ensure object start
   */
  private void readStartObject() {
    if (last != '{' && nextToken() != '{') {
      if (currentIndex >= length) throw newParseErrorAt("Unexpected end in JSON", 0, eof);
      throw newParseError("Expecting '{' as object start");
    }
  }

  @Override
  public final void startObject() {
    readStartObject();
    if (currentNames != null) {
      nameStack.addFirst(currentNames);
      currentNames = JsonNames.EMPTY;
    }
  }

  @Override
  public final void startObject(final JsonNames names) {
    readStartObject();
    if (currentNames != null) {
      nameStack.addFirst(currentNames);
    }
    currentNames = names;
  }

  /**
   * Ensure object end
   */
  @Override
  public final void endObject() {
    currentNames = nameStack.pollFirst();
    if (last != '}' && nextToken() != '}') {
      if (currentIndex >= length) throw newParseErrorAt("Unexpected end in JSON", 0, eof);
      throw newParseError("Expecting '}' as object end");
    }
  }

  private final StringBuilder error = new StringBuilder(0);
  private final Formatter errorFormatter = new Formatter(error);

  final JsonDataException newParseError(final String description) {
    error.setLength(0);
    error.append(description);
    error.append(", instead found '");
    error.append((char) last).append("'");
    //if (errorInfo == ErrorInfo.DESCRIPTION_ONLY) return ParsingException.create(formatErrorMessage(), false);
    error.append(" ");
    positionDescription(0, error);
    return new JsonDataException(errorMessage());
  }

  private String errorMessage() {
    return error.toString().replace("\n", "\\n");
  }

  final JsonDataException newParseErrorAt(final String description, final int offset) {
    if (errorInfo == ErrorInfo.MINIMAL || errorInfo == ErrorInfo.DESCRIPTION_ONLY) {
      return new JsonDataException(description);
    }
    error.setLength(0);
    error.append(description);
    error.append(" ");
    positionDescription(offset, error);
    return new JsonDataException(errorMessage());
  }

  final JsonDataException newParseErrorAt(final String description, final int offset, final Exception cause) {
    if (cause == null) throw new IllegalArgumentException("cause can't be null");
    if (errorInfo == ErrorInfo.MINIMAL) return new JsonDataException(description, cause);
    error.setLength(0);
    final String msg = cause.getMessage();
    if (msg != null && msg.length() > 0) {
      error.append(msg);
      if (!msg.endsWith(".")) {
        error.append(".");
      }
      error.append(" ");
    }
    error.append(description);
    //if (errorInfo == ErrorInfo.DESCRIPTION_ONLY) return new JsonDataException(formatErrorMessage(), cause);
    error.append(" ");
    positionDescription(offset, error);
    return new JsonDataException(errorMessage());
  }

  final JsonDataException newParseErrorFormat(final String description, final int offset, final String extraFormat, Object... args) {
    if (errorInfo == ErrorInfo.MINIMAL) return new JsonDataException(description);
    error.setLength(0);
    errorFormatter.format(extraFormat, args);
    //if (errorInfo == ErrorInfo.DESCRIPTION_ONLY) return ParsingException.create(formatErrorMessage(), false);
    error.append(" ");
    positionDescription(offset, error);
    return new JsonDataException(errorMessage());
  }

  final JsonDataException newParseErrorWith(String description, Object argument) {
    return newParseErrorWith(description, 0, description, argument, "");
  }

  final JsonDataException newParseErrorWith(String description, int offset, String extra, Object extraArgument, String extraSuffix) {
    if (errorInfo == ErrorInfo.MINIMAL) return new JsonDataException(description);
    error.setLength(0);
    error.append(extra);
    if (extraArgument != null) {
      error.append(": '").append(extraArgument).append("'");
    }
    error.append(extraSuffix);
    //if (errorInfo == ErrorInfo.DESCRIPTION_ONLY) return ParsingException.create(formatErrorMessage(), false);
    error.append(" ");
    positionDescription(offset, error);
    return new JsonDataException(errorMessage());
  }
}

