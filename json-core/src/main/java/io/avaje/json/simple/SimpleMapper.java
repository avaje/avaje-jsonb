package io.avaje.json.simple;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.json.PropertyNames;
import io.avaje.json.stream.JsonStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A mapper for mapping to basic Java types.
 * <p>
 * This supports the basic Java types of String, Boolean, Integer, Long, Double and
 * Maps and List of these.
 * <p>
 * For full support with more types and binding to custom types use avaje-jsonb instead.
 *
 * <h3>Example</h3>
 * <pre>{@code
 *
 *   static final SimpleMapper simpleMapper = SimpleMapper.builder().build();
 *
 *   Map<String, Long> map = new LinkedHashMap<>();
 *   map.put("one", 45L);
 *   map.put("two", 93L);
 *
 *   String asJson = simpleMapper.toJson(map);
 *
 * }</pre>
 */
public interface SimpleMapper {

  /**
   * Create a new builder for SimpleMapper.
   */
  static Builder builder() {
    return new DSimpleMapperBuilder();
  }

  /**
   * Return a mapper for any json content.
   */
  Type<Object> object();

  /**
   * Return a mapper for json OBJECT content with more reading/writing options.
   */
  Type<Map<String, Object>> map();

  /**
   * Return a mapper for json ARRAY content with more reading/writing options.
   */
  Type<List<Object>> list();

  /**
   * Write the object to JSON string.
   * <p>
   * For options to write json content to OutputStream, Writer etc
   * use {@link Type}.
   *
   * <pre>{@code
   *
   * var list = List.of(42, "hello");
   *
   * var asJson = mapper.toJson(list);
   * }</pre>
   */
  String toJson(Object object);

  /**
   * Write the object to JsonWriter.
   * <p>
   * For options to write json content to OutputStream, Writer etc
   * use {@link Type}.
   */
  void toJson(Object object, JsonWriter jsonWriter);

  /**
   * Read the object from JSON string.
   */
  Object fromJson(String json);

  /**
   * Read the object from JSON.
   */
  Object fromJson(JsonReader jsonReader);

  /**
   * Read a Map from JSON OBJECT string.
   * <p>
   * Use {@link #map()} for more reading options.
   */
  Map<String, Object> fromJsonObject(String json);

  /**
   * Read a Map from JSON OBJECT.
   * <p>
   * Use {@link #map()} for more reading options.
   */
  Map<String, Object> fromJsonObject(JsonReader jsonReader);

  /**
   * Read a List from JSON ARRAY string.
   * <p>
   * Use {@link #list()} for more reading options.
   */
  List<Object> fromJsonArray(String json);

  /**
   * Read a List from JSON ARRAY.
   * <p>
   * Use {@link #list()} for more reading options.
   */
  List<Object> fromJsonArray(JsonReader jsonReader);

  /**
   * Return the property names as PropertyNames.
   * <p>
   * Provides the option of optimising the writing of json for property names
   * by having them already escaped and encoded rather than as plain strings.
   */
  PropertyNames properties(String... names);

  /**
   * Return a Type specific mapper for the given JsonAdapter.
   *
   * @param customAdapter The custom adapter to use.
   * @param <T>           The type of the class to map to/from json.
   * @return The Type specific mapper.
   */
  <T> Type<T> type(JsonAdapter<T> customAdapter);

  /**
   * Return a Type specific mapper using a function that creates a JsonAdapter.
   * <p>
   * Often the adapterFunction is the constructor of the custom JsonAdapter where
   * the constructor takes SimpleMapper as the only argument.
   *
   * @param adapterFunction The function that creates a JsonAdapter.
   * @param <T>             The type of the class to map to/from json.
   * @return The Type specific mapper.
   */
  <T> Type<T> type(Function<SimpleMapper, JsonAdapter<T>> adapterFunction);

  default JsonExtract extract(Map<String, Object> map) {
    return new DExtract(map);
  }

  /**
   * Build the JsonNodeMapper.
   */
  interface Builder {

    /**
     * Set the default JsonStream to use.
     * <p>
     * When not set this defaults to {@code JsonStream.builder().build()}.
     *
     * @see JsonStream#builder()
     */
    Builder jsonStream(JsonStream jsonStream);

    /**
     * Build and return the JsonNodeMapper.
     */
    SimpleMapper build();
  }

  /**
   * Reading and writing with all options such and InputStream, Reader etc.
   */
  interface Type<T> {

    /**
     * Create a list type for this type.
     */
    Type<List<T>> list();

    /**
     * Create a map type with string keys and this type as the value type.
     */
    Type<Map<String, T>> map();

    /**
     * Read the return the value from the json content.
     */
    T fromJson(String content);

    /**
     * Read the return the value from the reader.
     */
    T fromJson(JsonReader reader);

    /**
     * Read the return the value from the json content.
     */
    T fromJson(byte[] content);

    /**
     * Read the return the value from the reader.
     */
    T fromJson(Reader reader);

    /**
     * Read the return the value from the inputStream.
     */
    T fromJson(InputStream inputStream);

    /**
     * Return as json string.
     */
    String toJson(T value);

    /**
     * Return as json string in pretty format.
     */
    String toJsonPretty(T value);

    /**
     * Return the value as json content in bytes form.
     */
    byte[] toJsonBytes(T value);

    /**
     * Write to the given writer.
     */
    void toJson(T value, JsonWriter writer);

    /**
     * Write to the given writer.
     */
    void toJson(T value, Writer writer);

    /**
     * Write to the given outputStream.
     */
    void toJson(T value, OutputStream outputStream);

  }
}
