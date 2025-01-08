package io.avaje.json.simple;

import java.util.Map;
import java.util.Optional;

/**
 * A helper to extract values from a Map.
 * <p>
 * The <em>path</em> can be simple like {@code "name"} or a nested path using
 * dot notation like {@code "address.city"}.
 * <p>
 * For extracting numbers there are methods for int, long and double that will
 * return the intValue(), longValue() and doubleValue() respectively.
 * <p>
 * <pre>{@code
 *
 *   String json = "{\"name\":\"Rob\",\"score\":4.5,\"whenActive\":\"2025-10-20\",\"address\":{\"street\":\"Pall Mall\"}}";
 *   Map<String, Object> mapFromJson = simpleMapper.fromJsonObject(json);
 *
 *   JsonExtract jsonExtract = simpleMapper.extract(mapFromJson);
 *
 *   String name = jsonExtract.extract("name");
 *   double score = jsonExtract.extract("score", -1D);
 *   String street = jsonExtract.extract("address.street");
 *
 *   LocalDate activeDate = jsonExtract.extractOrEmpty("whenActive")
 *     .map(LocalDate::parse)
 *     .orElseThrow();
 *
 * }</pre>
 *
 */
public interface JsonExtract {

  /**
   * Return a JsonExtract for the given Map of values.
   */
  static JsonExtract of(Map<String, Object> map) {
    return new DExtract(map);
  }

  /**
   * Extract the text from the node at the given path.
   *
   * @throws IllegalArgumentException When the given path is missing.
   */
  String extract(String path);

  /**
   * Extract the text value from the given path if present else empty.
   *
   * <pre>{@code
   *
   *   LocalDate activeDate = jsonExtract.extractOrEmpty("whenActive")
   *     .map(LocalDate::parse)
   *     .orElseThrow();
   *
   * }</pre>
   */
  Optional<String> extractOrEmpty(String path);

  /**
   * Extract the text value from the given path if present or the given default value.
   *
   * @param missingValue The value to use when the path is missing.
   */
  String extract(String path, String missingValue);

  /**
   * Extract the int from the given path if present or the given default value.
   *
   * @param missingValue The value to use when the path is missing.
   */
  int extract(String path, int missingValue);

  /**
   * Extract the long from the given path if present or the given default value.
   *
   * @param missingValue The value to use when the path is missing.
   */
  long extract(String path, long missingValue);

  /**
   * Extract the double from the given path if present or the given default value.
   *
   * @param missingValue The value to use when the path is missing.
   */
  double extract(String path, double missingValue);

  /**
   * Extract the boolean from the given path if present or the given default value.
   *
   * @param missingValue The value to use when the path is missing.
   */
  boolean extract(String path, boolean missingValue);
}
