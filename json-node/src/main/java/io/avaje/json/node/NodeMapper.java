package io.avaje.json.node;

import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * Mappers for JsonNode, JsonArray and JsonObject.
 * <p>
 * This supports more options for reading and writing json content
 * such as InputStream, OutputStream, Reader, Writer etc.
 *
 * @see JsonNodeMapper#arrayMapper()
 * @see JsonNodeMapper#objectMapper()
 * @see JsonNodeMapper#nodeMapper()
 */
public interface NodeMapper<T extends JsonNode> {

  /**
   * Read the return the value from the json content.
   */
  T fromJson(String content);

  /**
   * Read the return the value from the reader.
   */
  T fromJson(JsonReader reader);

  /**
   * Read the return the value from the json content.
   */
  T fromJson(byte[] content);

  /**
   * Read the return the value from the reader.
   */
  T fromJson(Reader reader);

  /**
   * Read the return the value from the inputStream.
   */
  T fromJson(InputStream inputStream);

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

}
