package io.avaje.jsonb;

import io.avaje.json.stream.JsonOutput;
import io.avaje.json.JsonWriter;

import java.io.OutputStream;
import java.io.Writer;

/**
 * Represents a subset of properties that can be written as json.
 * <p>
 * We can use a "view DSL" to dynamically define which properties to include in the
 * json view.
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
 *
 * @see JsonType#view(String)
 */
public interface JsonView<T> {

  /**
   * Return as json string.
   */
  String toJson(T value);

  /**
   * Return as json string in pretty format.
   */
  String toJsonPretty(T value);

  /**
   * Return the value as json content in bytes form.
   */
  byte[] toJsonBytes(T value);

  /**
   * Write to the given writer.
   */
  void toJson(T value, JsonWriter writer);

  /**
   * Write to the given writer.
   */
  void toJson(T value, Writer writer);

  /**
   * Write to the given outputStream.
   */
  void toJson(T value, OutputStream outputStream);

  /**
   * Write to the given output.
   */
  void toJson(T value, JsonOutput output);
}
