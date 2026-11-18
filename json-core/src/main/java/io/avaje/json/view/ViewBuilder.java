package io.avaje.json.view;

import io.avaje.json.JsonAdapter;

import java.util.function.UnaryOperator;

/** Builds a JsonView. */
public interface ViewBuilder {

  /**
   * Begin a json object.
   *
   * @param name The name of the json property (null for the top level).
   * @param accessor The "getter/accessor" for the nested object (null for the top level).
   */
  void beginObject(String name, UnaryOperator<Object> accessor);

  /**
   * Add a json property entry.
   *
   * @param name The name of the json property.
   * @param adapter The adapter used for the property.
   * @param accessor The "getter/accessor" for the property.
   */
  void add(String name, JsonAdapter<?> adapter, UnaryOperator<Object> accessor);

  /** Add a nested json array. */
  void addArray(String name, JsonAdapter<?> adapter, UnaryOperator<Object> accessor);

  /** End a json object. */
  void endObject();
}
