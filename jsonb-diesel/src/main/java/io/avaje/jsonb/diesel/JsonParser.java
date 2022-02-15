package io.avaje.jsonb.diesel;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 */
final class JsonParser {

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

  private InputStream stream;
  private int readLimit;
  //always leave some room for reading special stuff, so that buffer contains enough padding for such optimizations
  private int bufferLenWithExtraSpace;

  private final byte[] originalBuffer;
  private final int originalBufferLenWithExtraSpace;

  private ArrayStack<JsonNames> nameStack;
  private JsonNames currentNames;
  private boolean pushedNames;

  final ErrorInfo errorInfo;
  final DoublePrecision doublePrecision;
  final int doubleLengthLimit;
  final UnknownNumberParsing unknownNumbers;
  final int maxNumberDigits;
  private final int maxStringBuffer;

  JsonParser(
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
    this.bufferLenWithExtraSpace = buffer.length - 38; //currently maximum padding is for uuid
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
  void reset() {
    this.buffer = this.originalBuffer;
    this.bufferLenWithExtraSpace = this.originalBufferLenWithExtraSpace;
    currentIndex = 0;
    this.length = 0;
    this.readLimit = 0;
    this.stream = null;
  }

  /**
   * Bind input stream for processing.
   * Stream will be processed in byte[] chunks.
   * If stream is null, reference to stream will be released.
   *
   * @param stream set input stream
   * @return itself
   * @throws IOException unable to read from stream
   */
  JsonParser process(final InputStream stream) throws IOException {
    this.currentPosition = 0;
    this.currentIndex = 0;
    this.stream = stream;
    if (stream != null) {
      this.readLimit = Math.min(this.length, bufferLenWithExtraSpace);
      final int available = readFully(buffer, stream, 0);
      readLimit = Math.min(available, bufferLenWithExtraSpace);
      this.length = available;
    }
    return this;
  }

  /**
   * Bind byte[] buffer for processing.
   * If this method is used in combination with process(InputStream) this buffer will be used for processing chunks of stream.
   * If null is sent for byte[] buffer, new length for valid input will be set for existing buffer.
   *
   * @param newBuffer new buffer to use for processing
   * @param newLength length of buffer which can be used
   * @return itself
   */
  JsonParser process(final byte[] newBuffer, final int newLength) {
    if (newBuffer != null) {
      this.buffer = newBuffer;
      this.bufferLenWithExtraSpace = buffer.length - 38; // maximum padding is for uuid
    }
    if (newLength > buffer.length) {
      throw new IllegalArgumentException("length can't be longer than buffer.length");
    }
    currentIndex = 0;
    this.length = newLength;
    this.stream = null;
    this.readLimit = newLength;
    return this;
  }

  void names(JsonNames nextNames) {
    if (currentNames != nextNames) {
      if (currentNames != null) {
        pushCurrentNames();
      }
      currentNames = nextNames;
    }
  }

  private void pushCurrentNames() {
    if (nameStack == null) {
      nameStack = new ArrayStack<>();
    }
    pushedNames = true;
    nameStack.push(currentNames);
  }

  /**
   * Valid length of the input buffer.
   */
  int length() {
    return length;
  }

  @Override
  public String toString() {
    return new String(buffer, 0, length, utf8);
  }

  private static int readFully(final byte[] buffer, final InputStream stream, final int offset) throws IOException {
    int read;
    int position = offset;
    while (position < buffer.length
      && (read = stream.read(buffer, position, buffer.length - position)) != -1) {
      position += read;
    }
    return position;
  }

  private static class EmptyEOFException extends EOFException {
    @Override
    public synchronized Throwable fillInStackTrace() {
      return this;
    }
  }
  private static final EOFException eof = new EmptyEOFException();

  boolean withStackTrace() {
    return errorInfo == ErrorInfo.WITH_STACK_TRACE;
  }

  /**
   * Read next byte from the JSON input.
   * If buffer has been read in full IOException will be thrown
   *
   * @return next byte
   * @throws IOException when end of JSON input
   */
  private byte read() throws IOException {
    if (stream != null && currentIndex > readLimit) {
      prepareNextBlock();
    }
    if (currentIndex >= length) {
      throw ParsingException.create("Unexpected end of JSON input", eof, withStackTrace());
    }
    return last = buffer[currentIndex++];
  }

  private int prepareNextBlock() throws IOException {
    final int len = length - currentIndex;
    System.arraycopy(buffer, currentIndex, buffer, 0, len);
    final int available = readFully(buffer, stream, len);
    currentPosition += currentIndex;
    if (available == len) {
      readLimit = length - currentIndex;
      length = readLimit;
      currentIndex = 0;
    } else {
      readLimit = Math.min(available, bufferLenWithExtraSpace);
      this.length = available;
      currentIndex = 0;
    }
    return available;
  }

  boolean isEndOfStream() throws IOException {
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
  byte last() {
    return last;
  }

  String positionDescription(int offset) {
    final StringBuilder error = new StringBuilder(60);
    positionDescription(offset, error);
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

  private final StringBuilder error = new StringBuilder(0);
  private final Formatter errorFormatter = new Formatter(error);

  public ParsingException newParseError(final String description) {
    return newParseError(description, 0);
  }

  public ParsingException newParseError(final String description, final int positionOffset) {
    if (errorInfo == ErrorInfo.MINIMAL) return ParsingException.create(description, false);
    error.setLength(0);
    error.append(description);
    error.append(". Found ");
    error.append((char)last);
    if (errorInfo == ErrorInfo.DESCRIPTION_ONLY) return ParsingException.create(error.toString(), false);
    error.append(" ");
    positionDescription(positionOffset, error);
    return ParsingException.create(error.toString(), withStackTrace());
  }

  public ParsingException newParseErrorAt(final String description, final int positionOffset) {
    if (errorInfo == ErrorInfo.MINIMAL || errorInfo == ErrorInfo.DESCRIPTION_ONLY) {
      return ParsingException.create(description, false);
    }
    error.setLength(0);
    error.append(description);
    error.append(" ");
    positionDescription(positionOffset, error);
    return ParsingException.create(error.toString(), withStackTrace());
  }

  public ParsingException newParseErrorAt(final String description, final int positionOffset, final Exception cause) {
    if (cause == null) throw new IllegalArgumentException("cause can't be null");
    if (errorInfo == ErrorInfo.MINIMAL) return ParsingException.create(description, cause, false);
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
    if (errorInfo == ErrorInfo.DESCRIPTION_ONLY) return ParsingException.create(error.toString(), cause, false);
    error.append(" ");
    positionDescription(positionOffset, error);
    return ParsingException.create(error.toString(), withStackTrace());
  }

  public ParsingException newParseErrorFormat(final String shortDescription, final int positionOffset, final String longDescriptionFormat, Object... arguments) {
    if (errorInfo == ErrorInfo.MINIMAL) return ParsingException.create(shortDescription, false);
    error.setLength(0);
    errorFormatter.format(longDescriptionFormat, arguments);
    if (errorInfo == ErrorInfo.DESCRIPTION_ONLY) return ParsingException.create(error.toString(), false);
    error.append(" ");
    positionDescription(positionOffset, error);
    return ParsingException.create(error.toString(), withStackTrace());
  }

  public ParsingException newParseErrorWith(
    final String description, Object argument) {
    return newParseErrorWith(description, 0, "", description, argument, "");
  }

  public ParsingException newParseErrorWith(
    final String shortDescription,
    final int positionOffset,
    final String longDescriptionPrefix,
    final String longDescriptionMessage, Object argument,
    final String longDescriptionSuffix) {
    if (errorInfo == ErrorInfo.MINIMAL) return ParsingException.create(shortDescription, false);
    error.setLength(0);
    error.append(longDescriptionPrefix);
    error.append(longDescriptionMessage);
    if (argument != null) {
      error.append(": '");
      error.append(argument.toString());
      error.append("'");
    }
    error.append(longDescriptionSuffix);
    if (errorInfo == ErrorInfo.DESCRIPTION_ONLY) return ParsingException.create(error.toString(), false);
    error.append(" ");
    positionDescription(positionOffset, error);
    return ParsingException.create(error.toString(), withStackTrace());
  }

  int getTokenStart() {
    return tokenStart;
  }

  int getCurrentIndex() {
    return currentIndex;
  }

  int scanNumber() {
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

  char[] prepareBuffer(final int start, final int len) throws ParsingException {
    if (len > maxNumberDigits) {
      throw newParseErrorWith("Too many digits detected in number", len, "", "Too many digits detected in number", len, "");
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

  boolean allWhitespace(final int start, final int end) {
    final byte[] _buf = buffer;
    for (int i = start; i < end; i++) {
      if (!WHITESPACE[_buf[i] + 128]) return false;
    }
    return true;
  }

  int findNonWhitespace(final int end) {
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
   * @throws ParsingException unable to parse string
   */
  char[] readSimpleQuote() throws ParsingException {
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

  public int readInt() throws IOException {
    return NumberParser.deserializeInt(this);
  }

  public long readLong() throws IOException {
    return NumberParser.deserializeLong(this);
  }

  public short readShort() throws IOException {
    return NumberParser.deserializeShort(this);
  }

  public double readDouble() throws IOException {
    return NumberParser.deserializeDouble(this);
  }

  public BigDecimal readDecimal() throws IOException {
    return NumberParser.deserializeDecimal(this);
  }

  public BigInteger readBigInt() throws IOException {
    return NumberParser.deserializeBigInt(this);
  }

  public boolean readBool() throws IOException {
    if (wasTrue()) {
      return true;
    } else if (wasFalse()) {
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
   * @throws IOException error reading string input
   */
  public String readString() throws IOException {
    final int len = parseString();
    //return valuesCache == null ? new String(chars, 0, len) : valuesCache.get(chars, len);
    return new String(chars, 0, len);
  }

  int parseString() throws IOException {
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

  private int hexToInt(final byte value) throws ParsingException {
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
        if (currentIndex + 1 < length) {
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
        } else {
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

  /**
   * Read next token (byte) from input JSON.
   * Whitespace will be skipped and next non-whitespace byte will be returned.
   *
   * @return next non-whitespace byte in the JSON input
   * @throws IOException unable to get next byte (end of stream, ...)
   */
  public byte getNextToken() throws IOException {
    read();
    if (WHITESPACE[last + 128]) {
      while (wasWhiteSpace()) {
        read();
      }
    }
    return last;
  }

//  long positionInStream() {
//    return currentPosition + currentIndex;
//  }

  long positionInStream(final int offset) {
    return currentPosition + currentIndex - offset;
  }

  long calcHash() throws IOException {
    if (last != '"') throw newParseError("Expecting '\"' for attribute name start");
    tokenStart = currentIndex;
    int ci = currentIndex;
    long hash = 0x811c9dc5;
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

  private int calcHashAndCopyName(long hash, int ci) throws IOException {
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
        return (int) hash;
      }
      if (i == chars.length) {
        chars = Arrays.copyOf(chars, chars.length * 2);
      }
      chars[i++] = (char) b;
      hash ^= b;
      hash *= 0x1000193;
    } while (!isEndOfStream());
    //TODO: check offset
    throw newParseErrorAt("JSON string was not closed with a double quote", (int)startPosition);
  }

  private String lastFieldName() {
    if (stream != null && nameEnd == -1) {
      return new String(chars, 0, lastNameLen);
    }
    return new String(buffer, tokenStart, nameEnd - tokenStart - 1, StandardCharsets.UTF_8);
  }

  private byte skipString() throws IOException {
    byte c = read();
    boolean inEscape = false;
    while (c != '"' || inEscape) {
      inEscape = !inEscape && c == '\\';
      c = read();
    }
    return getNextToken();
  }

  /**
   * Skip to next non-whitespace token (byte)
   * Will not allocate memory while skipping over JSON input.
   *
   * @return next non-whitespace byte
   * @throws IOException unable to read next byte (end of stream, invalid JSON, ...)
   */
  public byte skip() throws IOException {
    if (last == '"') return skipString();
    if (last == '{') {
      return skipObject();
    }
    if (last == '[') {
      return skipArray();
    }
    if (last == 'n') {
      if (!wasNull()) throw newParseErrorAt("Expecting 'null' for null constant", 0);
      return getNextToken();
    }
    if (last == 't') {
      if (!wasTrue()) throw newParseErrorAt("Expecting 'true' for true constant", 0);
      return getNextToken();
    }
    if (last == 'f') {
      if (!wasFalse()) throw newParseErrorAt("Expecting 'false' for false constant", 0);
      return getNextToken();
    }
    while (last != ',' && last != '}' && last != ']') {
      read();
    }
    return last;
  }

  private byte skipArray() throws IOException {
    getNextToken();
    byte nextToken = skip();
    while (nextToken == ',') {
      getNextToken();
      nextToken = skip();
    }
    if (nextToken != ']') throw newParseError("Expecting ']' for array end");
    return getNextToken();
  }

  private byte skipObject() throws IOException {
    byte nextToken = getNextToken();
    if (nextToken == '}') return getNextToken();
    if (nextToken == '"') {
      nextToken = skipString();
    } else {
      throw newParseError("Expecting '\"' for attribute name");
    }
    if (nextToken != ':') throw newParseError("Expecting ':' after attribute name");
    getNextToken();
    nextToken = skip();
    while (nextToken == ',') {
      nextToken = getNextToken();
      if (nextToken == '"') {
        nextToken = skipString();
      } else {
        throw newParseError("Expecting '\"' for attribute name");
      }
      if (nextToken != ':') throw newParseError("Expecting ':' after attribute name");
      getNextToken();
      nextToken = skip();
    }
    if (nextToken != '}') throw newParseError("Expecting '}' for object end");
    return getNextToken();
  }

//  public final byte[] readBase64() throws IOException {
//    if (stream != null && Base64.findEnd(buffer, currentIndex) == buffer.length) {
//      final int len = parseString();
//      final byte[] input = new byte[len];
//      for (int i = 0; i < input.length; i++) {
//        input[i] = (byte) chars[i];
//      }
//      return Base64.decodeFast(input, 0, len);
//    }
//    if (last != '"') throw newParseError("Expecting '\"' for base64 start");
//    final int start = currentIndex;
//    currentIndex = Base64.findEnd(buffer, start);
//    last = buffer[currentIndex++];
//    if (last != '"') throw newParseError("Expecting '\"' for base64 end");
//    return Base64.decodeFast(buffer, start, currentIndex - 1);
//  }

  public String nextField() throws IOException {
    if (currentNames != null) {
      final long hash = calcHash();
      String key = currentNames.lookup(hash);
      if (key == null) {
        key = lastFieldName();
      }
      if (read() != ':') {
        if (!wasWhiteSpace() || getNextToken() != ':') {
          throw newParseError("Expecting ':' after attribute name");
        }
      }
      getNextToken(); // position to read the value/next
      return key;
    }
    return readKey();
  }

  /**
   * Read key value of JSON input.
   */
  private String readKey() throws IOException {
    final int len = parseString();
    final String key = new String(chars, 0, len);
    if (getNextToken() != ':') throw newParseError("Expecting ':' after attribute name");
    getNextToken();
    return key;
  }

  /**
   * Checks if 'null' value is at current position.
   */
  public boolean wasNull() throws ParsingException {
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
  private boolean wasTrue() throws ParsingException {
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
  private boolean wasFalse() throws ParsingException {
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

  /**
   * Ensure array start
   */
  public void startArray() throws IOException {
    if (last != '[' && getNextToken() != '[') {
      if (currentIndex >= length) throw newParseErrorAt("Unexpected end in JSON", 0, eof);
      throw newParseError("Expecting '[' as array start");
    }
  }

  /**
   * Ensure array end
   */
  public void endArray() throws IOException {
    if (last != ']' && getNextToken() != ']') {
      if (currentIndex >= length) throw newParseErrorAt("Unexpected end in JSON", 0, eof);
      throw newParseError("Expecting ']' as array end");
    }
  }

  /**
   * Ensure object start
   */
  public void startObject() throws IOException {
    if (last != '{' && getNextToken() != '{') {
      if (currentIndex >= length) throw newParseErrorAt("Unexpected end in JSON", 0, eof);
      throw newParseError("Expecting '{' as object start");
    }
  }

  /**
   * Ensure object end
   */
  public void endObject() throws IOException {
    if (pushedNames) {
      pushedNames = false;
      currentNames = nameStack != null ? nameStack.pop() : null;
    }
    if (last != '}' && getNextToken() != '}') {
      if (currentIndex >= length) throw newParseErrorAt("Unexpected end in JSON", 0, eof);
      throw newParseError("Expecting '}' as object end");
    }
  }

}

