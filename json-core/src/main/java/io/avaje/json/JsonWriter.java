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
import java.io.Flushable;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Writes json content.
 */
public interface JsonWriter extends Closeable, Flushable {

  /**
   * Unwrap the underlying generator being used.
   * <p>
   * We do this to get access to the underlying generator. For the case when
   * using jackson-core we get access to Jackson JsonGenerator and can then
   * do Jackson specific things like set custom escaping.
   *
   * <pre>{@code
   *
   *     try (JsonWriter writer = jsonb.writer(new StringWriter())) {
   *
   *       // get access to the underlying Jackson JsonGenerator
   *       var generator = writer.unwrap(JsonGenerator.class)
   *
   *       // do Jackson specific things like ...
   *       generator.setCharacterEscapes(new HTMLCharacterEscapes());
   *
   *       jsonb.toJson(myBean, writer);
   *     }
   *
   * }</pre>
   */
  <T> T unwrap(Class<T> type);

  /**
   * Set to serialise null values or not.
   */
  void serializeNulls(boolean serializeNulls);

  /**
   * Return true if null values should be serialised.
   */
  boolean serializeNulls();

  /**
   * Set to serialise empty collections or not.
   */
  void serializeEmpty(boolean serializeEmpty);

  /**
   * Return true if empty collections should be serialised.
   */
  boolean serializeEmpty();

  /**
   * Set tp true to output json in pretty format.
   */
  void pretty(boolean pretty);

  /**
   * Return the current path.
   */
  String path();

  /**
   * Set the property names that will be used for all json generation.
   * <p>
   * These names should be used for all json generation for this generator and set once
   * rather than set per object via {@link #beginObject(PropertyNames)} (PropertyNames)}.
   * <p>
   * This is used by view json generation where all the names are known at the point
   * when the view is created (a sort of flattened nested tree).
   */
  void allNames(PropertyNames names);

  /**
   * Set the next property name to write by position. This uses the already encoded
   * name values of PropertyNames.
   */
  void name(int position);

  /**
   * Set the next property name to write.
   * <p>
   * This is generally less efficient than using {@link JsonWriter#beginObject(PropertyNames)}
   * and {@link JsonWriter#name(int)}.
   */
  void name(String name);

  /**
   * Write array begin.
   */
  void beginArray();

  /**
   * Write array end.
   */
  void endArray();

  /**
   * Write empty array.
   */
  void emptyArray();

  /**
   * Write object begin.
   */
  void beginObject();

  /**
   * Write object being and use the already encoded property names.
   */
  void beginObject(PropertyNames names);

  /**
   * Write object end.
   */
  void endObject();

  /**
   * Write null value.
   */
  void nullValue();

  /**
   * Write a string value.
   */
  void value(String value);

  /**
   * Write a boolean value.
   */
  void value(boolean value);

  /**
   * Write an int value.
   */
  void value(int value);

  /**
   * Write a long value.
   */
  void value(long value);

  /**
   * Write a double value.
   */
  void value(double value);

  /**
   * Write a Boolean value.
   */
  void value(Boolean value);

  /**
   * Write an Integer value.
   */
  void value(Integer value);

  /**
   * Write a Long value.
   */
  void value(Long value);

  /**
   * Write a Double value.
   */
  void value(Double value);

  /**
   * Write a BigDecimal value.
   */
  void value(BigDecimal value);

  /**
   * Write a BigInteger value.
   */
  void value(BigInteger value);

  /**
   * Write binary content as base64.
   */
  void value(byte[] value);

  /**
   * Write a value that could be any value.
   */
  void jsonValue(Object value);

  /**
   * Write raw JSON content.
   */
  void rawValue(String value);

  /**
   * Write new line characters typically for {@code x-json-stream} content.
   */
  void writeNewLine();

  /**
   * Flush the writer.
   */
  void flush();

  /**
   * Close the writer.
   */
  void close();

  /**
   * Mark the generated json as not completed due to an error.
   * <p>
   * This typically means not to flush or close an underlying OutputStream which
   * allows it to be reset to then write some error response content instead.
   */
  void markIncomplete();
}
