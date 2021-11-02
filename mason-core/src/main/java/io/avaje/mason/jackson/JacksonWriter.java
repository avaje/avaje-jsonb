package io.avaje.mason.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import io.avaje.mason.JsonWriter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class JacksonWriter implements JsonWriter {

  private final JsonGenerator generator;
  private boolean serializeEmpty;
  private boolean serializeNulls;
  private String deferredName;

  JacksonWriter(JsonGenerator generator) {
    this.generator = generator;
  }

  @Override
  public void close() throws IOException {
    generator.close();
  }

  @Override
  public void flush() throws IOException {
    generator.flush();
  }

  @Override
  public void indent(String indent) {

  }

  @Override
  public String indent() {
    return null;
  }

  @Override
  public void lenient(boolean lenient) {

  }

  @Override
  public boolean lenient() {
    return false;
  }

  @Override
  public void serializeNulls(boolean serializeNulls) {
    this.serializeNulls = serializeNulls;
  }

  @Override
  public boolean serializeNulls() {
    return serializeNulls;
  }

  @Override
  public boolean serializeEmpty() {
    return serializeEmpty;
  }

  @Override
  public JsonWriter serializeEmpty(boolean serializeEmpty) {
    this.serializeEmpty = serializeEmpty;
    return this;
  }

  @Override
  public String path() {
    return generator.toString();
  }

  @Override
  public JsonWriter beginArray() throws IOException {
    writeDeferredName();
    generator.writeStartArray();
    return this;
  }

  @Override
  public JsonWriter endArray() throws IOException {
    generator.writeEndArray();
    return this;
  }

  @Override
  public JsonWriter beginObject() throws IOException {
    writeDeferredName();
    generator.writeStartObject();
    return this;
  }

  @Override
  public JsonWriter endObject() throws IOException {
    generator.writeEndObject();
    return this;
  }

  @Override
  public JsonWriter name(String name) {
    deferredName = name;
    return this;
  }

  void writeDeferredName() throws IOException {
    if (deferredName != null) {
      generator.writeFieldName(deferredName);
      deferredName = null;
    }
  }

  @Override
  public JsonWriter emptyArray() throws IOException {
    if (serializeEmpty) {
      writeDeferredName();
      generator.writeStartArray();
      generator.writeEndArray();
    }
    return this;
  }

  @Override
  public JsonWriter nullValue() throws IOException {
    if (serializeNulls) {
      writeDeferredName();
      generator.writeNull();
    }
    return this;
  }

  @Override
  public JsonWriter value(String value) throws IOException {
    if (value == null) {
      return nullValue();
    }
    writeDeferredName();
    generator.writeString(value);
    return this;
  }

  @Override
  public JsonWriter value(boolean value) throws IOException {
    writeDeferredName();
    generator.writeBoolean(value);
    return this;
  }


  @Override
  public JsonWriter value(int value) throws IOException {
    writeDeferredName();
    generator.writeNumber(value);
    return this;
  }

  @Override
  public JsonWriter value(long value) throws IOException {
    writeDeferredName();
    generator.writeNumber(value);
    return this;
  }

  @Override
  public JsonWriter value(double value) throws IOException {
    writeDeferredName();
    generator.writeNumber(value);
    return this;
  }

  @Override
  public JsonWriter value(Boolean value) throws IOException {
    if (value == null) {
      return nullValue();
    }
    writeDeferredName();
    generator.writeBoolean(value);
    return this;
  }

  @Override
  public JsonWriter value(Integer value) throws IOException {
    if (value == null) {
      return nullValue();
    }
    writeDeferredName();
    generator.writeNumber(value);
    return this;
  }

  @Override
  public JsonWriter value(Long value) throws IOException {
    if (value == null) {
      return nullValue();
    }
    writeDeferredName();
    generator.writeNumber(value);
    return this;
  }

  @Override
  public JsonWriter value(Double value) throws IOException {
    if (value == null) {
      return nullValue();
    }
    writeDeferredName();
    generator.writeNumber(value);
    return this;
  }

  @Override
  public JsonWriter value(BigDecimal value) throws IOException {
    if (value == null) {
      return nullValue();
    }
    writeDeferredName();
    generator.writeNumber(value);
    return this;
  }

  @Override
  public JsonWriter jsonValue(Object value) throws IOException {
    if (value instanceof Map<?, ?>) {
      writeMap((Map<?, ?>) value);
    } else if (value instanceof List<?>) {
      writeList((List<?>) value);
    } else if (value instanceof Collection<?>) {
      writeCollection((Collection<?>) value);
    } else if (value instanceof String) {
      value(((String) value));
    } else if (value instanceof Boolean) {
      value(((Boolean) value).booleanValue());
    } else if (value instanceof Integer) {
      value(((Integer) value).intValue());
    } else if (value instanceof Long) {
      value(((Long) value).longValue());
    } else if (value instanceof Double) {
      value(((Double) value).doubleValue());
    } else if (value instanceof BigDecimal) {
      value(((BigDecimal) value));
    } else if (value == null) {
      nullValue();
    } else {
      throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName());
    }
    return this;
  }

  private void writeList(List<?> value) throws IOException {
    beginArray();
    for (Object element : value) {
      jsonValue(element);
    }
    endArray();
  }

  private void writeCollection(Collection<?> value) throws IOException {
    beginArray();
    for (Object element : value) {
      jsonValue(element);
    }
    endArray();
  }

  private void writeMap(Map<?, ?> value) throws IOException {
    beginObject();
    for (Map.Entry<?, ?> entry : value.entrySet()) {
      Object key = entry.getKey();
      if (!(key instanceof String)) {
        throw new IllegalArgumentException(
          key == null ? "Map keys must be non-null" : "Map keys must be of type String: " + key.getClass().getName());
      }
      name(((String) key));
      jsonValue(entry.getValue());
    }
    endObject();
  }
}
