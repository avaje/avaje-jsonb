package io.avaje.json.node;

import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * JSON Object type.
 */
public final class JsonObject implements JsonNode {

  private static final long serialVersionUID = 1L;
  private static final JsonObject EMPTY = new JsonObject(Collections.emptyMap());
  private static final Pattern PATH_PATTERN = Pattern.compile("\\.");

  private final Map<String, JsonNode> children;

  /**
   * Create an empty immutable JsonObject.
   */
  public static JsonObject empty() {
    return EMPTY;
  }

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
  public boolean equals(Object object) {
    if (this == object) return true;
    if (!(object instanceof JsonObject)) return false;
    JsonObject that = (JsonObject) object;
    return Objects.equals(children, that.children);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(children);
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

  @Override
  public JsonObject unmodifiable() {
    final var mapCopy = new LinkedHashMap<String, JsonNode>();
    for (Map.Entry<String, JsonNode> entry : children.entrySet()) {
      mapCopy.put(entry.getKey(), entry.getValue().unmodifiable());
    }
    return JsonObject.of(mapCopy);
  }

  @Override
  public JsonObject copy() {
    final var mapCopy = new LinkedHashMap<String, JsonNode>();
    for (Map.Entry<String, JsonNode> entry : children.entrySet()) {
      mapCopy.put(entry.getKey(), entry.getValue().copy());
    }
    return new JsonObject(mapCopy);
  }

  @Override
  public Map<String, Object> toPlain() {
    final var mapCopy = new LinkedHashMap<String, Object>();
    for (Map.Entry<String, JsonNode> entry : children.entrySet()) {
      mapCopy.put(entry.getKey(), entry.getValue().toPlain());
    }
    return mapCopy;
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
   * Remove the node for the given key returning the removed value.
   *
   * @param key The key to remove.
   * @return The node that has been removed.
   */
  @Nullable
  public JsonNode remove(String key) {
    return children.remove(key);
  }

  /**
   * Return the direct element at the given path throwing IllegalArgumentException
   * if it is missing.
   *
   * @param key The child key that must exist.
   * @return The child node.
   * @throws IllegalArgumentException when the path is missing.
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

  @Override
  public String extract(String path) {
    final var node = find(path);
    if (node == null) {
      throw new IllegalArgumentException("Node not present for " + path);
    }
    return node.text();
  }

  @Override
  public Optional<String> extractOrEmpty(String path) {
    final var name = find(path);
    return name == null ? Optional.empty() : Optional.of(name.text());
  }

  @Override
  public String extract(String path, String missingValue) {
    final var name = find(path);
    return name == null ? missingValue : name.text();
  }

  @Override
  public int extract(String path, int missingValue) {
    final var node = find(path);
    return !(node instanceof JsonNumber)
      ? missingValue
      : ((JsonNumber) node).intValue();
  }

  @Override
  public long extract(String path, long missingValue) {
    final var node = find(path);
    return !(node instanceof JsonNumber)
      ? missingValue
      : ((JsonNumber) node).longValue();
  }

  @Override
  public double extract(String path, double missingValue) {
    final var node = find(path);
    return !(node instanceof JsonNumber)
      ? missingValue
      : ((JsonNumber) node).doubleValue();
  }

  @Override
  public Number extract(String path, Number missingValue) {
    final var node = find(path);
    return !(node instanceof JsonNumber)
      ? missingValue
      : ((JsonNumber) node).numberValue();
  }

  @Override
  public boolean extract(String path, boolean missingValue) {
    final var node = find(path);
    return !(node instanceof JsonBoolean)
      ? missingValue
      : ((JsonBoolean) node).value();
  }

  @Override
  public JsonNode extractNode(String path) {
    final var node = find(path);
    if (node == null) {
      throw new IllegalArgumentException("Node not present for " + path);
    }
    return node;
  }

  @Override
  public JsonNode extractNode(String path, JsonNode missingValue) {
    final var node = find(path);
    return node != null ? node : missingValue;
  }

  @Override
  public Optional<JsonNode> extractNodeOrEmpty(String path) {
    final var node = find(path);
    return Optional.ofNullable(node);
  }
}
