package io.avaje.mason;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.math.BigDecimal;

public interface JsonWriter extends Closeable, Flushable {

  void setIndent(String indent);

  String getIndent();

  void setLenient(boolean lenient);

  boolean isLenient();

  void setSerializeNulls(boolean serializeNulls);

  boolean getSerializeNulls();

  String path();

  JsonWriter beginArray() throws IOException;

  JsonWriter endArray() throws IOException;

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

  void promoteValueToName();

  void close() throws IOException;

  void flush() throws IOException;
}
