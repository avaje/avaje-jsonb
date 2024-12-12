package io.avaje.json.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

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

  private JsonArray(List<JsonNode> children) {
    this.children = requireNonNull(children);
  }

  @Override
  public String toString() {
    return text();
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
