package io.avaje.json.node;

import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.util.Optional;

/**
 * Represents the core JSON types.
 */
public /*sealed*/ interface JsonNode extends Serializable
  /*permits JsonArray, JsonObject, JsonBoolean, JsonString, JsonNumber*/ {

  /**
   * The types for JsonNode.
   */
  enum Type {
    ARRAY(false),
    OBJECT(false),
    NULL(),
    BOOLEAN(),
    STRING(),
    NUMBER(),
    ;
    // BINARY,
    // POJO
    private final boolean value;

    Type() {
      this(true);
    }

    Type(boolean value) {
      this.value = value;
    }

    /**
     * True for JsonBoolean, JsonString and JsonNumber's and NULL.
     */
    public boolean isValue() {
      return value;
    }

    /**
     * True for the JsonNumber implementations only.
     */
    public boolean isNumber() {
      return this == Type.NUMBER;
    }

    /**
     * True for JsonArray only.
     */
    public boolean isArray() {
      return this == Type.ARRAY;
    }

    /**
     * True for JsonObject only.
     */
    public boolean isObject() {
      return this == Type.OBJECT;
    }
  }

  /**
   * Return the type of the node.
   */
  Type type();

  /**
   * Return a text representation of the node.
   */
  String text();

  /**
   * Return an unmodifiable deep copy of the JsonNode.
   */
  JsonNode unmodifiable();

  /**
   * Return a mutable deep copy of the JsonNode.
   */
  JsonNode copy();

  /**
   * Return the JsonNode as its plain Java value ({@code String, Integer, List, Map etc}).
   */
  Object toPlain();

  /**
   * Find a node given a path using dot notation.
   *
   * @param path The path in dot notation
   * @return The found node or null
   */
  @Nullable
  default JsonNode find(String path) {
    throw new UnsupportedOperationException();
  }

  /**
   * Extract the text from the node at the given path.
   *
   * @throws IllegalArgumentException When the given path is missing.
   */
  default String extract(String path) {
    throw new UnsupportedOperationException();
  }

  /**
   * Extract the text from the given path if present else empty.
   */
  default Optional<String> extractOrEmpty(String path) {
    throw new UnsupportedOperationException();
  }

  /**
   * Extract the text from the given path if present or the given default value.
   *
   * @param missingValue The value to use when the path is missing.
   */
  default String extract(String path, String missingValue) {
    throw new UnsupportedOperationException();
  }

  /**
   * Extract the int from the given path if present or the given default value.
   *
   * @param missingValue The value to use when the path is missing.
   */
  default int extract(String path, int missingValue) {
    throw new UnsupportedOperationException();
  }

  /**
   * Extract the long from the given path if present or the given default value.
   *
   * @param missingValue The value to use when the path is missing.
   */
  default long extract(String path, long missingValue) {
    throw new UnsupportedOperationException();
  }

  /**
   * Extract the long from the given path if present or the given default value.
   *
   * @param missingValue The value to use when the path is missing.
   */
  default double extract(String path, double missingValue) {
    throw new UnsupportedOperationException();
  }

  /**
   * Extract the int from the given path if present or the given default value.
   *
   * @param missingValue The value to use when the path is missing.
   */
  default Number extract(String path, Number missingValue) {
    throw new UnsupportedOperationException();
  }

  /**
   * Extract the boolean from the given path if present or the given default value.
   *
   * @param missingValue The value to use when the path is missing.
   */
  default boolean extract(String path, boolean missingValue) {
    throw new UnsupportedOperationException();
  }

  /**
   * Extract the node from the given path if present or throw IllegalArgumentException
   * if it is missing.
   *
   * @throws IllegalArgumentException When the given path is missing.
   */
  default JsonNode extractNode(String path) {
    throw new UnsupportedOperationException();
  }

  /**
   * Extract the node from the given path if present or the given default value.
   *
   * @param missingValue The node to use when the path is missing.
   * @return The node for the given path.
   */
  default JsonNode extractNode(String path, JsonNode missingValue) {
    throw new UnsupportedOperationException();
  }

  /**
   * Extract the node from the given path if present else empty.
   *
   * @return The node for the given path.
   */
  default Optional<JsonNode> extractNodeOrEmpty(String path) {
    throw new UnsupportedOperationException();
  }

}
