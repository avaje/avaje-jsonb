package io.avaje.json.node;

import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * JSON Object type.
 */
public final class JsonObject implements JsonNode {

  private static final Pattern PATH_PATTERN = Pattern.compile("\\.");

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

  /**
   * Add a String value.
   */
  public JsonObject add(String key, String value) {
    return add(key, JsonString.of(value));
  }

  /**
   * Add a boolean value.
   */
  public JsonObject add(String key, boolean value) {
    return add(key, JsonBoolean.of(value));
  }

  /**
   * Add a int value.
   */
  public JsonObject add(String key, int value) {
    return add(key, JsonInteger.of(value));
  }

  /**
   * Add a long value.
   */
  public JsonObject add(String key, long value) {
    return add(key, JsonLong.of(value));
  }

  /**
   * Return the direct element at the given path throwing IllegalArgumentException
   * if it is missing.
   *
   * @param key The child key that must exist.
   * @return The child node.
   */
  public JsonNode get(String key) {
    final var node = children.get(key);
    if (node == null) {
      throw new IllegalArgumentException("Node not present for " + key);
    }
    return node;
  }

  @Nullable
  @Override
  public JsonNode find(String path) {
    final String[] paths = PATH_PATTERN.split(path, 2);
    final JsonNode child = children.get(paths[0]);
    if (child == null || paths.length == 1) {
      return child;
    }
    if (child instanceof JsonObject) {
      JsonObject co = (JsonObject) child;
      return co.find(paths[1]);
    }
    return null;
  }

  @Nullable
  @Override
  public String extract(String path) {
    final var node = find(path);
    return node == null ? null : node.text();
  }

  @Override
  public String extract(String path, String defaultValue) {
    final var name = find(path);
    return name == null ? defaultValue : name.text();
  }

  @Override
  public long extract(String path, long defaultValue) {
    final var node = find(path);
    return !(node instanceof JsonNumber)
      ? defaultValue
      : ((JsonNumber) node).longValue();
  }

  @Override
  public Number extract(String path, Number defaultValue) {
    final var node = find(path);
    return !(node instanceof JsonNumber)
      ? defaultValue
      : ((JsonNumber) node).numberValue();
  }

  @Override
  public boolean extract(String path, boolean defaultValue) {
    final var node = find(path);
    return !(node instanceof JsonBoolean)
      ? defaultValue
      : ((JsonBoolean) node).value();
  }
}
