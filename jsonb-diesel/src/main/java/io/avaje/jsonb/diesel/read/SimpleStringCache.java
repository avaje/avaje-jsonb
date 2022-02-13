package io.avaje.jsonb.diesel.read;


/**
 * Simplistic string cache implementation.
 * It uses a fixed String[] structure in which it caches string value based on it's hash.
 * Eg, hash &amp; mask provide index into the structure. Different string with same hash will overwrite the previous one.
 */
public final class SimpleStringCache implements StringCache {

  private final int mask;
  private final String[] cache;

  /**
   * Will use String[] with 1024 elements.
   */
  public SimpleStringCache() {
    this(10);
  }

  public SimpleStringCache(int log2Size) {
    int size = 2;
    for (int i = 1; i < log2Size; i++) {
      size *= 2;
    }
    mask = size - 1;
    cache = new String[size];
  }

  /**
   * Calculates hash of the provided "string" and looks it up from the String[]
   * It it doesn't exists of a different string is already there a new String instance is created
   * and saved into the String[]
   *
   * @param chars buffer into which string was parsed
   * @param len the string length inside the buffer
   * @return String instance matching the char[]/int pair
   */
  @Override
  public String get(char[] chars, int len) {
    long hash = 0x811c9dc5;
    for (int i = 0; i < len; i++) {
      hash ^= (byte) chars[i];
      hash *= 0x1000193;
    }
    final int index = (int) hash & mask;
    final String value = cache[index];
    if (value == null) return createAndPut(index, chars, len);
    if (value.length() != len) return createAndPut(index, chars, len);
    for (int i = 0; i < value.length(); i++) {
      if (value.charAt(i) != chars[i]) return createAndPut(index, chars, len);
    }
    return value;
  }

  private String createAndPut(int index, char[] chars, int len) {
    final String value = new String(chars, 0, len);
    cache[index] = value;
    return value;
  }
}
