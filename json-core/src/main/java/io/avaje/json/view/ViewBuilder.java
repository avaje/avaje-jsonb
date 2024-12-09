package io.avaje.json.view;

import io.avaje.json.JsonAdapter;

import java.lang.invoke.MethodHandle;

/**
 * Builds a JsonView.
 */
public interface ViewBuilder {

  /**
   * Begin a json object.
   */
  void beginObject(String name, MethodHandle methodHandle);

  /**
   * Add a json property entry.
   * @param name The name of the json property.
   * @param adapter The adapter used for the property.
   * @param methodHandle The MethodHandle of the "getter/accessor" for the property.
   */
  void add(String name, JsonAdapter<?> adapter, MethodHandle methodHandle);

  /**
   * Add a nested json array.
   */
  void addArray(String name, JsonAdapter<?> adapter, MethodHandle methodHandle);

  /**
   * End a json object.
   */
  void endObject();

  /**
   * Return a MethodHandle for public field access for the given class and field name.
   */
  MethodHandle field(Class<?> cls, String name);

  /**
   * Return a MethodHandle for the "getter/accessor" for the given class and field name.
   */
  MethodHandle method(Class<?> cls, String methodName, Class<?> returnType);

}
