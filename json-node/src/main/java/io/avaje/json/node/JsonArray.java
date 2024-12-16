package io.avaje.json.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * JSON Array type.
 */
public final class JsonArray implements JsonNode {

  private static final long serialVersionUID = 1L;

  private static final JsonArray EMPTY = new JsonArray(Collections.emptyList());

  private final List<JsonNode> children;

  /**
   * Create an empty immutable JsonArray.
   */
  public static JsonArray empty() {
    return EMPTY;
  }

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

  private JsonArray(List<JsonNode> children) {
    this.children = requireNonNull(children);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (!(object instanceof JsonArray)) return false;
    JsonArray jsonArray = (JsonArray) object;
    return Objects.equals(children, jsonArray.children);
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
  public JsonArray unmodifiable() {
    final var newList = new ArrayList<JsonNode>(children.size());
    for (JsonNode child : children) {
      newList.add(child.unmodifiable());
    }
    return of(newList);
  }

  @Override
  public JsonArray copy() {
    final var newList = new ArrayList<JsonNode>(children.size());
    for (JsonNode child : children) {
      newList.add(child.copy());
    }
    return new JsonArray(newList);
  }

  @Override
  public List<Object> toPlain() {
    final var newList = new ArrayList<>(children.size());
    for (JsonNode child : children) {
      newList.add(child.toPlain());
    }
    return newList;
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
   * Return true if the json array is empty.
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
   * Return the child elements.
   */
  public List<JsonNode> elements() {
    return children;
  }

  /**
   * Return the stream of child elements.
   */
  public Stream<JsonNode> stream() {
    return children.stream();
  }

  /**
   * Add an element to the json array.
   */
  public JsonArray add(JsonNode element) {
    children.add(element);
    return this;
  }

  /**
   * Add a String value.
   */
  public JsonArray add(String value) {
    return add(JsonString.of(value));
  }

  /**
   * Add a boolean value.
   */
  public JsonArray add(boolean value) {
    return add(JsonBoolean.of(value));
  }

  /**
   * Add a int value.
   */
  public JsonArray add(int value) {
    return add(JsonInteger.of(value));
  }

  /**
   * Add a long value.
   */
  public JsonArray add(long value) {
    return add(JsonLong.of(value));
  }

}
