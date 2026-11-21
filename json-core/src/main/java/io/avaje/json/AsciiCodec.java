package io.avaje.json;

import java.nio.charset.StandardCharsets;

/**
 * Converts values to and from JSON strings whose content is plain ASCII with no characters that
 * need escaping, such as UUIDs and ISO-8601 dates.
 *
 * @param <T> The type of value
 */
public interface AsciiCodec<T> {

  /**
   * Return the value for the string content in {@code buffer[start, end)}.
   *
   * @param buffer The buffer holding the content
   * @param start The index of the first byte of content
   * @param end The index after the last byte of content
   * @return The value
   */
  default T decode(byte[] buffer, int start, int end) {
    return decode(new String(buffer, start, end - start, StandardCharsets.ISO_8859_1));
  }

  /**
   * The maximum number of bytes {@link #encode(Object, byte[], int)} writes for the given value, or
   * -1 to have {@code value.toString()} written instead.
   *
   * @param value The value to write
   * @return The maximum number of bytes, or -1
   */
  default int maxLength(T value) {
    return -1;
  }

  /**
   * Write the ASCII string content for the given value to the buffer.
   *
   * @param value The value to write
   * @param buffer The buffer to write to
   * @param pos The index to write the first byte at
   * @return The index after the last byte written
   */
  default int encode(T value, byte[] buffer, int pos) {
    final String text = value.toString();
    final int len = text.length();
    for (int i = 0; i < len; i++) {
      buffer[pos++] = (byte) text.charAt(i);
    }
    return pos;
  }

  /**
   * Return the value for the given string content.
   *
   * <p>Used by readers without direct byte access, and by the stream reader when the content has
   * escape sequences or non-ASCII characters, or does not fit in its current buffer.
   *
   * @param text The string content without the quotes, escape sequences resolved
   * @return The value
   */
  T decode(String text);
}
