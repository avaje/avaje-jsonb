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

  void serializeEmpty(boolean serializeEmpty);

  String path();

  void beginArray() throws IOException;

  void endArray() throws IOException;

  void emptyArray() throws IOException;

  void beginObject() throws IOException;

  void endObject() throws IOException;

  void name(String name) throws IOException;

  //void name(Object name) throws IOException;

  void nullValue() throws IOException;

  void value(String value) throws IOException;

  void value(boolean value) throws IOException;

  void value(int value) throws IOException;

  void value(long value) throws IOException;

  void value(double value) throws IOException;

  void value(Boolean value) throws IOException;

  void value(Integer value) throws IOException;

  void value(Long value) throws IOException;

  void value(Double value) throws IOException;

  void value(BigDecimal value) throws IOException;

  void jsonValue(Object value) throws IOException;

  void close() throws IOException;

  void flush() throws IOException;

}
