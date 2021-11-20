package io.avaje.jsonb.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.spi.PropertyNames;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

final class JacksonWriter implements JsonWriter {

  private final JsonGenerator generator;
  private boolean serializeEmpty;
  private boolean serializeNulls;
  private String deferredName;
  private ArrayStack<JacksonNames> nameStack;
  private JacksonNames currentNames;
  private boolean pushedNames;
  private int namePos = -1;

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
  public void serializeEmpty(boolean serializeEmpty) {
    this.serializeEmpty = serializeEmpty;
  }

  @Override
  public String path() {
    return generator.toString();
  }

  @Override
  public void beginArray() throws IOException {
    writeDeferredName();
    generator.writeStartArray();
  }

  @Override
  public void endArray() throws IOException {
    generator.writeEndArray();
  }

  @Override
  public void beginObject() throws IOException {
    writeDeferredName();
    generator.writeStartObject();
  }

  @Override
  public void endObject() throws IOException {
    generator.writeEndObject();
    if (pushedNames) {
      pushedNames = false;
      currentNames = nameStack != null ? nameStack.pop() : null;
    }
  }

  @Override
  public void name(String name) {
    deferredName = name;
  }

  @Override
  public void names(PropertyNames nextNames) {
    if (currentNames != nextNames) {
      if (currentNames != null) {
        pushCurrentNames();
      }
      currentNames = (JacksonNames)nextNames;
    }
  }

  private void pushCurrentNames() {
    if (nameStack == null) {
      nameStack = new ArrayStack<>();
    }
    pushedNames = true;
    nameStack.push(currentNames);
  }

  @Override
  public void name(int position) {
    this.namePos = position;
  }

  void writeDeferredName() throws IOException {
    if (namePos > -1) {
      generator.writeFieldName(currentNames.key(namePos));
      namePos = -1;
    } else if (deferredName != null) {
      generator.writeFieldName(deferredName);
      deferredName = null;
    }
  }

  @Override
  public void emptyArray() throws IOException {
    if (serializeEmpty) {
      writeDeferredName();
      generator.writeStartArray();
      generator.writeEndArray();
    } else if (namePos >= 0) {
      namePos = -1;
    } else if (deferredName != null) {
      deferredName = null;
    }
  }

  @Override
  public void nullValue() throws IOException {
    if (serializeNulls) {
      writeDeferredName();
      generator.writeNull();
    }
  }

  @Override
  public void value(String value) throws IOException {
    if (value == null) {
      nullValue();
    } else {
      writeDeferredName();
      generator.writeString(value);
    }
  }

  @Override
  public void value(boolean value) throws IOException {
    writeDeferredName();
    generator.writeBoolean(value);
  }


  @Override
  public void value(int value) throws IOException {
    writeDeferredName();
    generator.writeNumber(value);
  }

  @Override
  public void value(long value) throws IOException {
    writeDeferredName();
    generator.writeNumber(value);
  }

  @Override
  public void value(double value) throws IOException {
    writeDeferredName();
    generator.writeNumber(value);
  }

  @Override
  public void value(Boolean value) throws IOException {
    if (value == null) {
      nullValue();
    } else {
      writeDeferredName();
      generator.writeBoolean(value);
    }
  }

  @Override
  public void value(Integer value) throws IOException {
    if (value == null) {
      nullValue();
    } else {
      writeDeferredName();
      generator.writeNumber(value);
    }
  }

  @Override
  public void value(Long value) throws IOException {
    if (value == null) {
      nullValue();
    } else {
      writeDeferredName();
      generator.writeNumber(value);
    }
  }

  @Override
  public void value(Double value) throws IOException {
    if (value == null) {
      nullValue();
    } else {
      writeDeferredName();
      generator.writeNumber(value);
    }
  }

  @Override
  public void value(BigDecimal value) throws IOException {
    if (value == null) {
      nullValue();
    } else {
      writeDeferredName();
      generator.writeNumber(value);
    }
  }

  @Override
  public void rawValue(String value) throws IOException {
    if (value == null) {
      nullValue();
    } else {
      writeDeferredName();
      generator.writeRaw(value);
    }
  }

  @Override
  public void jsonValue(Object value) throws IOException {
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
