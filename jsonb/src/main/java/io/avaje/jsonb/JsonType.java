package io.avaje.jsonb;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides API to serialise a type to and from JSON.
 * <p>
 * JsonType provides the main API that is used to convert to and from json.
 * <p>
 * Moshi note: JsonType does not exist in Moshi and has been added to provide a
 * slightly nicer API to use than JsonAdapter.
 */
public interface JsonType<T> {

  /**
   * Build and return the view given the DSL that specifies the properties to include.
   * <p>
   * Examples of json view DSL:
   * <pre>{@code
   *
   *   // only include the id and name properties
   *   (id, name)
   *
   *   // include billAddress which is a nested type
   *   (id, name, billingAddress(street, suburb))
   *
   *   // include billAddress with all it's properties
   *   (id, name, billingAddress(*))
   *
   *   (id, name, billingAddress(street, suburb), shippingAddress(*), contacts(email,lastName, firstName))
   *
   * }</pre>
   */
  JsonView<T> view(String dsl);

  /**
   * Return the list type for this JsonType.
   */
  JsonType<List<T>> list();

  /**
   * Return the set type for this JsonType.
   */
  JsonType<Set<T>> set();

  /**
   * Return the map with this type as the value type and string keys.
   */
  JsonType<Map<String, T>> map();

  /**
   * Return the value as json content.
   */
  String toJson(T value);

  /**
   * Write the value as json content to the given JsonWriter.
   */
  void toJson(JsonWriter writer, T value);

  /**
   * Write the value as json content to the given writer.
   */
  void toJson(Writer writer, T value);

  /**
   * Write the value as json content to the given outputStream.
   */
  void toJson(OutputStream outputStream, T value);

  /**
   * Read the return the value from the reader.
   */
  T fromJson(JsonReader reader);

  /**
   * Read the return the value from the json content.
   */
  T fromJson(String content);

  /**
   * Read the return the value from the reader.
   */
  T fromJson(Reader reader);

  /**
   * Read the return the value from the inputStream.
   */
  T fromJson(InputStream inputStream);

  /**
   * Convert from 'object form' expecting {@code Map<String,Object>} for
   * 'json object' and expecting {@code Collection<?>} for 'json array'.
   *
   * @param value The json value that will be converted into T.
   * @return The value converted from 'object form'.
   */
  T fromObject(Object value);
}
