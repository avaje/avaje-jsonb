package io.avaje.json.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * JSON Array type.
 */
public final class JsonArray implements JsonNode {

  private final List<JsonNode> children;

  /**
   * Create a new JsonArray that can be added to.
   */
  public static JsonArray create() {
    return new JsonArray(new ArrayList<>());
  }

  /**
   * Create an unmodifiable JsonArray with the given elements.
   */
  public static JsonArray of(List<JsonNode> children) {
    return new JsonArray(Collections.unmodifiableList(children));
  }

  JsonArray(List<JsonNode> children) {
    this.children = requireNonNull(children);
  }

  @Override
  public Type type() {
    return Type.ARRAY;
  }

  @Override
  public String text() {
    return children.toString();
  }

  /**
   * Return the child elements.
   */
  public List<JsonNode> elements() {
    return children;
  }

  /**
   * Return true if the json array is empty.
   */
  public boolean isEmpty() {
    return children.isEmpty();
  }

  /**
   * Add an element to the json array.
   */
  public JsonArray add(JsonNode element) {
    children.add(element);
    return this;
  }

}
