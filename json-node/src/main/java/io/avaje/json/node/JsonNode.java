package io.avaje.json.node;

import org.jspecify.annotations.Nullable;

/**
 * Represents the code JSON types.
 */
public interface JsonNode {

  /**
   * The types for JsonNode.
   */
  enum Type {
    ARRAY(true, false),
    OBJECT(false, true),
    NULL(),
    BOOLEAN(),
    STRING(),
    NUMBER(true),
    ;
    // BINARY,
    // MISSING,
    // POJO
    private final boolean value;
    private final boolean numberType;
    private final boolean arrayType;
    private final boolean objectType;

    Type() {
      this(true, false, false, false);
    }

    Type(boolean numberType) {
      this(true, numberType, false, false);
    }

    Type(boolean arrayType, boolean objectType) {
      this(false, false, arrayType, objectType);
    }

    Type(boolean value, boolean numberType, boolean arrayType, boolean objectType) {
      this.value = value;
      this.numberType = numberType;
      this.arrayType = arrayType;
      this.objectType = objectType;
    }

    public boolean isValue() {
      return value;
    }

    public boolean isNumber() {
      return numberType;
    }

    public boolean isArray() {
      return arrayType;
    }

    public boolean isObject() {
      return objectType;
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
