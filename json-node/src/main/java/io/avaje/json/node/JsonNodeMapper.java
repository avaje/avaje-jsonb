package io.avaje.json.node;

import io.avaje.json.JsonAdapter;
import io.avaje.json.node.adapter.NodeAdapterBuilder;
import io.avaje.json.stream.JsonStream;

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
   * Helper method to write the node to JSON.
   *
   * <pre>{@code
   * static final JsonNodeMapper node = JsonNodeMapper.builder().build();
   *
   * JsonArray jsonArray = JsonArray.create()
   * .add(JsonInteger.of(42))
   * .add(JsonString.of("foo"));
   *
   * var asJson = node.toJson(jsonArray);
   * }</pre>
   */
  String toJson(JsonNode node);

  /**
   * Helper method to read JSON returning a JsonNode.
   *
   * <pre>{@code
   * static final JsonNodeMapper node = JsonNodeMapper.builder().build();
   *
   * JsonNode nodeFromJson = node.fromJson(jsonContent);
   * }</pre>
   */
  JsonNode fromJson(String json);

  /**
   * Helper method to read JSON with an expected JsonNode type.
   *
   * <pre>{@code
   * static final JsonNodeMapper node = JsonNodeMapper.builder().build();
   *
   * JsonArray arrayFromJson = node.fromJson(JsonArray.class, jsonContent);
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
