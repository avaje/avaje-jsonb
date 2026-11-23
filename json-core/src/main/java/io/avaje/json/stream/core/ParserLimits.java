package io.avaje.json.stream.core;

/**
 * Maximum parser limits for numbers, decoded strings, and streamed raw JSON content.
 */
final class ParserLimits {

  static final int MAX_STRING_BUFFER_LIMIT = Integer.MAX_VALUE - 8;

  private final int maxNumberDigits;
  private final int maxStringBuffer;
  private final int maxRawBuffer;

  /**
   * Create parser limits.
   *
   * @param maxNumberDigits maximum number of digits in a numeric value
   * @param maxStringBuffer maximum decoded string buffer size
   * @param maxRawBuffer maximum buffer size used when reading raw JSON content
   */
  ParserLimits(int maxNumberDigits, int maxStringBuffer, int maxRawBuffer) {
    if (maxNumberDigits < 0) {
      throw new IllegalArgumentException("maxNumberDigits must not be negative");
    }
    if (maxStringBuffer < 0) {
      throw new IllegalArgumentException("maxStringBuffer must not be negative");
    }
    if (maxRawBuffer < 0) {
      throw new IllegalArgumentException("maxRawBuffer must not be negative");
    }
    this.maxNumberDigits = maxNumberDigits;
    this.maxStringBuffer = capMaxStringBuffer(maxStringBuffer);
    this.maxRawBuffer = maxRawBuffer;
  }

  static int capMaxStringBuffer(int maxStringBuffer) {
    return Math.min(maxStringBuffer, MAX_STRING_BUFFER_LIMIT);
  }

  /** Maximum number of digits in a numeric value. */
  int maxNumberDigits() {
    return maxNumberDigits;
  }

  /** Maximum decoded string buffer size. */
  int maxStringBuffer() {
    return maxStringBuffer;
  }

  /** Maximum buffer size used when reading raw JSON content. */
  int maxRawBuffer() {
    return maxRawBuffer;
  }
}
