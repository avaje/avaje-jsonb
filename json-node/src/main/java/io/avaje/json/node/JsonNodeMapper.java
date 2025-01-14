package io.avaje.json.node;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.json.PropertyNames;
import io.avaje.json.node.adapter.NodeAdapterBuilder;
import io.avaje.json.simple.SimpleMapper;
import io.avaje.json.stream.JsonStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.function.Function;

/**
 * Provide JsonAdapters for the JsonNode types.
 *
 * <pre>{@code
 *
 * static final JsonNodeMapper mapper = JsonNodeMapper.builder().build();
 *
 * JsonArray jsonArray = JsonArray.create()
 * .add(42)
 * .add("foo");
 *
 * var asJson = mapper.toJson(jsonArray);
 *
 * JsonNode jsonNodeFromJson = mapper.fromJson(asJson);
 * assertThat(jsonNodeFromJson).isInstanceOf(JsonArray.class);
 *
 * JsonArray arrayFromJson = mapper.fromJson(JsonArray.class, asJson);
 * assertThat(arrayFromJson.elements()).hasSize(2);
 *
 * }</pre>
 */
public interface JsonNodeMapper {

  /**
   * Create a Builder for the JsonNodeMapper.
   */
  static Builder builder() {
    return new NodeAdapterBuilder();
  }

  /**
   * Return a NodeMapper for ANY json content.
   * <p>
   * The NodeMapper provides support for all reading and writing options
   * such as InputStream, OutputStream, Reader, Writer etc.
   */
  SimpleMapper.Type<JsonNode> nodeMapper();

  /**
   * Return a NodeMapper for json OBJECT content.
   * <p>
   * The NodeMapper provides support for all reading and writing options
   * such as InputStream, OutputStream, Reader, Writer etc.
   */
  SimpleMapper.Type<JsonObject> objectMapper();

  /**
   * Return a NodeMapper for json ARRAY content.
   * <p>
   * The NodeMapper provides support for all reading and writing options
   * such as InputStream, OutputStream, Reader, Writer etc.
   */
  SimpleMapper.Type<JsonArray> arrayMapper();

  /**
   * Write the node to JSON string.
   * <p>
   * For options to write json content to OutputStream, Writer etc
   * use {@link #nodeMapper()}.
   *
   * <pre>{@code
   * static final JsonNodeMapper mapper = JsonNodeMapper.builder().build();
   *
   * JsonArray jsonArray = JsonArray.create()
   * .add(JsonInteger.of(42))
   * .add(JsonString.of("foo"));
   *
   * var asJson = mapper.toJson(jsonArray);
   * }</pre>
   *
   * @see SimpleMapper.Type#toJson(Object, OutputStream)
   * @see SimpleMapper.Type#toJson(Object, Writer)
   */
  String toJson(JsonNode node);

  /**
   * Write the node to JSON.
   * <p>
   * For options to write json content to OutputStream, Writer etc
   * use {@link #nodeMapper()}.
   *
   * @see SimpleMapper.Type#toJson(Object, OutputStream)
   * @see SimpleMapper.Type#toJson(Object, Writer)
   */
  void toJson(JsonNode node, JsonWriter jsonWriter);

  /**
   * Read any json content returning a JsonNode.
   * <p>
   * For options to read json content from InputStream, Reader etc
   * use the fromJson methods on {@link SimpleMapper.Type}.
   *
   * <pre>{@code
   * static final JsonNodeMapper mapper = JsonNodeMapper.builder().build();
   *
   * JsonNode nodeFromJson = mapper.fromJson(jsonContent);
   * }</pre>
   *
   * @see SimpleMapper.Type#fromJson(Reader)
   * @see SimpleMapper.Type#fromJson(InputStream)
   */
  JsonNode fromJson(String json);

  /**
   * Read any json content returning a JsonNode.
   * <p>
   * For options to read json content from InputStream, Reader etc
   * use the fromJson methods on {@link SimpleMapper.Type}.
   *
   * @see SimpleMapper.Type#fromJson(Reader)
   * @see SimpleMapper.Type#fromJson(InputStream)
   */
  JsonNode fromJson(JsonReader jsonReader);

  /**
   * Read a JsonObject from json string content.
   * <p>
   * Use this when we know that the json content is a JsonObject.
   *
   * @param json The json content.
   * @return The JsonObject parsed from the content.
   */
  JsonObject fromJsonObject(String json);

  /**
   * Read a JsonObject from json string content.
   * <p>
   * Use this when we know that the json content is a JsonObject.
   */
  JsonObject fromJsonObject(JsonReader jsonReader);

  /**
   * Read a JsonArray from json string content.
   * <p>
   * Use this when we know that the json content is a JsonArray.
   *
   * @param json The json content.
   * @return The JsonArray parsed from the content.
   */
  JsonArray fromJsonArray(String json);

  /**
   * Read a JsonArray from json string content.
   * <p>
   * Use this when we know that the json content is a JsonArray.
   */
  JsonArray fromJsonArray(JsonReader jsonReader);

  /**
   * Helper method to read JSON with an expected JsonNode type.
   *
   * <pre>{@code
   * static final JsonNodeMapper mapper = JsonNodeMapper.builder().build();
   *
   * JsonArray arrayFromJson = mapper.fromJson(JsonArray.class, jsonContent);
   * }</pre>
   */
  <T extends JsonNode> T fromJson(Class<T> type, String json);

  /**
   * Return the JsonAdapter for the given JsonNode type.
   *
   * @param type The JsonNode type
   * @return The adapter for the given type
   */
  <T extends JsonNode> JsonAdapter<T> adapter(Class<T> type);

  /**
   * Create a JsonAdapter for the given generic type or null if the
   * type is not actually a JsonNode type.
   */
  JsonAdapter<?> adapter(Type type);

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
  <T> SimpleMapper.Type<T> type(JsonAdapter<T> customAdapter);

  /**
   * Return a Type specific mapper using a function that creates a JsonAdapter.
   * <p>
   * Often the adapterFunction is the constructor of the custom JsonAdapter where
   * the constructor takes JsonNodeMapper as the only argument.
   *
   * @param adapterFunction The function that creates a JsonAdapter.
   * @param <T>             The type of the class to map to/from json.
   * @return The Type specific mapper.
   */
  <T> SimpleMapper.Type<T> type(Function<JsonNodeMapper, JsonAdapter<T>> adapterFunction);

  /**
   * Build the JsonNodeMapper.
   */
  interface Builder {

    /**
     * Set the default JsonStream to use when using {@link JsonNodeMapper#toJson(JsonNode)}
     * {@link JsonNodeMapper#fromJson(String)}.
     * <p>
     * When not set this defaults to {@code JsonStream.builder().build()}.
     *
     * @see JsonStream#builder()
     */
    Builder jsonStream(JsonStream jsonStream);

    /**
     * Set the adapter to use when reading {@link JsonNode.Type#NUMBER}.
     * <p>
     * The default will read as a double and test for the value being an
     * integral returning a long if is.
     */
    Builder numberAdapter(JsonAdapter<JsonNumber> numberAdapter);

    /**
     * Build and return the JsonNodeMapper.
     */
    JsonNodeMapper build();
  }
}
