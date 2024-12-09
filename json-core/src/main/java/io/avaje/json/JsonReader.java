/*
 * Copyright (C) 2014 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.avaje.json;

import java.io.Closeable;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Reads json content as a stream of JSON tokens and content.
 */
public interface JsonReader extends Closeable {

  /**
   * Unwrap and return the underlying JsonParser.
   * <p>
   * When using avaje-jsonb-jackson this will return the underlying Jackson JsonParser.
   *
   * <pre>{@code
   *
   * // when using avaje-jsonb-jackson
   * var jacksonParser = jsonReader.unwrap(JsonParser.class);
   *
   * }</pre>
   */
  <T> T unwrap(Class<T> type);

  /**
   * Read the beginning of an ARRAY or x-json-stream (new line delimited json content).
   */
  default void beginStream() {
    beginArray();
  }

  /**
   * Read the end of an ARRAY or x-json-stream (new line delimited json content).
   */
  default void endStream() {
    endArray();
  }

  /**
   * Return true if there is a next element in an ARRAY or x-json-stream (new line delimited json content).
   * <p>
   * Effectively this allows for new line delimited rather than commas between array elements.
   */
  default boolean hasNextStreamElement() {
    return hasNextElement();
  }

  /**
   * Read array begin.
   */
  void beginArray();

  /**
   * Read array end.
   */
  void endArray();

  /**
   * Return true if there is a next element in an ARRAY.
   */
  boolean hasNextElement();

  /**
   * Set the current property names.
   * <p>
   * Can be used by the reader to optimize the reading of known names.
   */
  void beginObject(PropertyNames names);

  /**
   * Read begin object.
   */
  void beginObject();

  /**
   * Read end object.
   */
  void endObject();

  /**
   * Return true if there is a next field to be read in an OBJECT.
   */
  boolean hasNextField();

  /**
   * Return the next field.
   */
  String nextField();

  /**
   * Return true if the value to be read is a null.
   */
  boolean isNullValue();

  /**
   * Read and return the next value as a boolean.
   */
  boolean readBoolean();

  /**
   * Read and return the next value as an int.
   */
  int readInt();

  /**
   * Read and return the next value as a long.
   */
  long readLong();

  /**
   * Read and return the next value as a double.
   */
  double readDouble();

  /**
   * Read and return the next value as a BigDecimal.
   */
  BigDecimal readDecimal();

  /**
   * Read and return the next value as a BigInteger.
   */
  BigInteger readBigInteger();

  /**
   * Read and return the next value as String.
   */
  String readString();

  /**
   * Read and return the binary value from base64.
   */
  byte[] readBinary();

  /**
   * Read and return raw json content as a String.
   */
  String readRaw();

  /**
   * Return the current location. This is typically used when reporting errors.
   */
  String location();

  /**
   * Return the current Token.
   */
  Token currentToken();

  /**
   * Close the resources of the reader.
   */
  void close();

  /**
   * Skip the next value.
   */
  void skipValue();

  /**
   * Reading json with an unmapped field, throw an Exception if failOnUnmapped is true.
   */
  void unmappedField(String fieldName);

  /**
   * Explicitly state if the streaming content contains ARRAY '[' and ']' tokens.
   * <p>
   * The builtin avaje-jsonb parser detects this automatically. Effectively we only need
   * to set this when we are using the Jackson core parser.
   *
   * <pre>{@code
   *
   *  try (JsonReader reader = jsonb.reader(arrayJson)) {
   *    // content contains ARRAY '[' and ']' tokens, use streamArray(true)
   *    Stream<MyBasic> asStream = type.stream(reader.streamArray(true));
   *    asStream.forEach(...);
   *  }
   *
   * }</pre>
   *
   * @param streamArray When true the content is expected to contain ARRAY '[' and ']' tokens.
   */
  default JsonReader streamArray(boolean streamArray) {
    // do nothing by default, jackson specifically needs this option
    return this;
  }

  /**
   * A structure, name, or value type in a JSON-encoded string.
   */
  enum Token {

    /**
     * The opening of a JSON array. Written using {@link JsonWriter#beginArray} and read using
     * {@link JsonReader#beginArray}.
     */
    BEGIN_ARRAY,

    /**
     * The opening of a JSON object. Written using {@link JsonWriter#beginObject} and read using
     * {@link JsonReader#beginObject}.
     */
    BEGIN_OBJECT,

    /**
     * A JSON string.
     */
    STRING,

    /**
     * A JSON number represented in this API by a Java {@code double}, {@code long}, or {@code int}.
     */
    NUMBER,

    /**
     * A JSON {@code true} or {@code false}.
     */
    BOOLEAN,

    /**
     * A JSON {@code null}.
     */
    NULL,

  }
}
