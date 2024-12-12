package io.avaje.json.node;

import org.jspecify.annotations.Nullable;

/**
 * Represents the core JSON types.
 */
public /*sealed*/ interface JsonNode
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
   * Find a node given a path using dot notation.
   * @param path The path in dot notation
   * @return The found node or null
   */
  @Nullable
  default JsonNode find(String path) {
    throw new UnsupportedOperationException();
  }

  /**
   * Extract the text from the node at the given path.
   */
  @Nullable
  default String extract(String path) {
    throw new UnsupportedOperationException();
  }

  /**
   * Extract the text from the given path if present or the given default value.
   */
  default String extract(String path, String defaultValue) {
    throw new UnsupportedOperationException();
  }

  /**
   * Extract the long from the given path if present or the given default value.
   */
  default long extract(String path, long defaultValue) {
    throw new UnsupportedOperationException();
  }

  /**
   * Extract the int from the given path if present or the given default value.
   */
  default Number extract(String path, Number defaultValue) {
    throw new UnsupportedOperationException();
  }

  /**
   * Extract the boolean from the given path if present or the given default value.
   */
  default boolean extract(String path, boolean defaultValue) {
    throw new UnsupportedOperationException();
  }
}
