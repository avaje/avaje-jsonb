package io.avaje.json.node;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * JSON Object type.
 */
public final class JsonObject implements JsonNode {

  private final Map<String, JsonNode> children;

  /**
   * Create a new mutable JsonObject to add elements to.
   */
  public static JsonObject create() {
    return new JsonObject(new LinkedHashMap<>());
  }

  /**
   * Create a unmodifiable JsonObject with the given elements.
   */
  public static JsonObject of(Map<String, JsonNode> elements) {
    return new JsonObject(Collections.unmodifiableMap(elements));
  }

  private JsonObject(Map<String, JsonNode> children) {
    this.children = requireNonNull(children);
  }

  @Override
  public String toString() {
    return text();
  }

  @Override
  public Type type() {
    return Type.OBJECT;
  }

  @Override
  public String text() {
    return children.toString();
  }

  /**
   * Return true if the json object contains no elements.
   */
  public boolean isEmpty() {
    return children.isEmpty();
  }

  /**
   * Return the number of elements.
   */
  public int size() {
    return children.size();
  }

  /**
   * Return true if the object contains the given key.
   */
  public boolean containsKey(String key) {
    return children.containsKey(key);
  }

  /**
   * Return the elements of the object.
   */
  public Map<String, JsonNode> elements() {
    return children;
  }

  /**
   * Add an element to the json object.
   *
   * @param key   The key for the element
   * @param value The value for the element.
   * @return This JsonObject for fluid use.
   */
  public JsonObject add(String key, JsonNode value) {
    children.put(key, value);
    return this;
  }

  public Optional<JsonNode> get(String key) {
    return Optional.ofNullable(children.get(key));
  }

}
