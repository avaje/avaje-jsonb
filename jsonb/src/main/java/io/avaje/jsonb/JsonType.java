package io.avaje.jsonb;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides API to serialise a type to and from JSON.
 * <p>
 * JsonType does not exist in Moshi and has been added to provide a
 * slightly easier API to use than JsonAdapter.
 */
public interface JsonType<T> {

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

  String toJson(T value) throws IOException;

  void toJson(JsonWriter writer, T value) throws IOException;

  void toJson(Writer writer, T value) throws IOException;

  void toJson(OutputStream outputStream, T value) throws IOException;

  T fromJson(JsonReader reader) throws IOException;

  T fromJson(String content) throws IOException;

  T fromJson(Reader reader) throws IOException;

  T fromJson(InputStream inputStream) throws IOException;

  /**
   * Convert from 'object form' expecting {@code Map<String,Object>} for
   * 'json object' and expecting {@code Collection<?>} for 'json array'.
   *
   * @param value The json value that will be converted into T.
   * @return The value converted from 'object form'.
   */
  T fromObject(Object value) throws IOException;
}
