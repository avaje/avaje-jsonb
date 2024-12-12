package io.avaje.json.node;

import io.avaje.json.JsonAdapter;
import io.avaje.json.node.adapter.NodeAdapterBuilder;
import io.avaje.json.stream.JsonStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;

/**
 * Provide JsonAdapters for the JsonNode types.
 *
 * <pre>{@code
 *
 * static final JsonNodeMapper mapper = JsonNodeMapper.builder().build();
 *
 * JsonArray jsonArray = JsonArray.create()
 * .add(JsonInteger.of(42))
 * .add(JsonString.of("foo"));
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
   * Create a Builder for the JsonNodeAdapter.
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
  NodeMapper<JsonNode> nodeMapper();

  /**
   * Return a NodeMapper for json OBJECT content.
   * <p>
   * The NodeMapper provides support for all reading and writing options
   * such as InputStream, OutputStream, Reader, Writer etc.
   */
  NodeMapper<JsonObject> objectMapper();

  /**
   * Return a NodeMapper for json ARRAY content.
   * <p>
   * The NodeMapper provides support for all reading and writing options
   * such as InputStream, OutputStream, Reader, Writer etc.
   */
  NodeMapper<JsonArray> arrayMapper();

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
   * @see NodeMapper#toJson(JsonNode, OutputStream)
   * @see NodeMapper#toJson(JsonNode, Writer)
   */
  String toJson(JsonNode node);

  /**
   * Read any json content returning a JsonNode.
   * <p>
   * For options to read json content from InputStream, Reader etc
   * use the fromJson methods on {@link NodeMapper}.
   *
   * <pre>{@code
   * static final JsonNodeMapper mapper = JsonNodeMapper.builder().build();
   *
   * JsonNode nodeFromJson = mapper.fromJson(jsonContent);
   * }</pre>
   *
   * @see NodeMapper#fromJson(Reader)
   * @see NodeMapper#fromJson(InputStream)
   */
  JsonNode fromJson(String json);

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
   * Read a JsonArray from json string content.
   * <p>
   * Use this when we know that the json content is a JsonArray.
   *
   * @param json The json content.
   * @return The JsonArray parsed from the content.
   */
  JsonArray fromJsonArray(String json);

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
