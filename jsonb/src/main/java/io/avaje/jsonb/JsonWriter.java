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
package io.avaje.jsonb;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.math.BigDecimal;

public interface JsonWriter extends Closeable, Flushable {

  void indent(String indent);

  String indent();

  void lenient(boolean lenient);

  boolean lenient();

  void serializeNulls(boolean serializeNulls);

  boolean serializeNulls();

  boolean serializeEmpty();

  JsonWriter serializeEmpty(boolean serializeEmpty);

  String path();

  JsonWriter beginArray() throws IOException;

  JsonWriter endArray() throws IOException;

  JsonWriter emptyArray() throws IOException;

  JsonWriter beginObject() throws IOException;

  JsonWriter endObject() throws IOException;

  JsonWriter name(String name) throws IOException;

  JsonWriter nullValue() throws IOException;

  JsonWriter value(String value) throws IOException;

  JsonWriter value(boolean value) throws IOException;

  JsonWriter value(int value) throws IOException;

  JsonWriter value(long value) throws IOException;

  JsonWriter value(double value) throws IOException;

  JsonWriter value(Boolean value) throws IOException;

  JsonWriter value(Integer value) throws IOException;

  JsonWriter value(Long value) throws IOException;

  JsonWriter value(Double value) throws IOException;

  JsonWriter value(BigDecimal value) throws IOException;

  JsonWriter jsonValue(Object value) throws IOException;

  void close() throws IOException;

  void flush() throws IOException;

}
