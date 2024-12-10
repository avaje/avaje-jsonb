package io.avaje.json.node;

public interface JsonNode {

  /**
   * Return the type of the node.
   */
  Type type();

  String text();

  /**
   * The types for JsonNode.
   */
  enum Type {
    NULL,
    ARRAY,
    OBJECT,
    BOOLEAN,
    STRING,
    NUMBER,
    // BINARY,
    // MISSING,
    // POJO
  }
}
