package io.avaje.json.jakarta;

import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.spi.PropertyNames;
import jakarta.json.stream.JsonGenerator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;

final class JakartaJsonWriter implements JsonWriter {

  private final JsonGenerator generator;
  private boolean serializeNulls;
  private boolean serializeEmpty;
  private DPropertyNames names;
  private String deferredName;
  private int namePos = -1;

  JakartaJsonWriter(JsonGenerator generator, boolean serializeNulls, boolean serializeEmpty) {
    this.generator = generator;
    this.serializeNulls = serializeNulls;
    this.serializeEmpty = serializeEmpty;
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
  public void serializeEmpty(boolean serializeEmpty) {
    this.serializeEmpty = serializeEmpty;
  }

  @Override
  public boolean serializeEmpty() {
    return serializeEmpty;
  }

  @Override
  public void pretty(boolean pretty) {
    // no support to turn on/off pretty output?
  }

  @Override
  public String path() {
    return null;
  }

  @Override
  public void names(PropertyNames names) {
    this.names = (DPropertyNames) names;
  }

  @Override
  public void name(int position) {
    namePos = position;
  }

  @Override
  public void name(String name) {
    deferredName = name;
  }

  @Override
  public void beginArray() {
    generator.writeStartArray();
  }

  @Override
  public void endArray() {
    generator.writeEnd();
  }

  @Override
  public void emptyArray() {
    if (serializeEmpty) {
      writeDeferredName();
      generator.writeStartArray();
      generator.writeEnd();
    } else if (namePos >= 0) {
      namePos = -1;
    } else if (deferredName != null) {
      deferredName = null;
    }
  }

  private void writeDeferredName() {
    if (namePos > -1) {
      generator.writeKey(names.key(namePos));
      namePos = -1;
    } else if (deferredName != null) {
      generator.writeKey(deferredName);
      deferredName = null;
    }
  }

  @Override
  public void beginObject() {
    generator.writeStartObject();
  }

  @Override
  public void endObject() {
    generator.writeEnd();
  }

  @Override
  public void nullValue() {
    generator.writeNull();
  }

  @Override
  public void value(boolean value) {
    writeDeferredName();
    generator.write(value);
  }

  @Override
  public void value(int value) {
    writeDeferredName();
    generator.write(value);
  }

  @Override
  public void value(long value) {
    writeDeferredName();
    generator.write(value);
  }

  @Override
  public void value(double value) {
    writeDeferredName();
    generator.write(value);
  }

  @Override
  public void value(String value) {
    if (value == null) {
      nullValue();
    } else {
      writeDeferredName();
      generator.write(value);
    }
  }

  @Override
  public void value(Boolean value) {
    if (value == null) {
      nullValue();
    } else {
      writeDeferredName();
      generator.write(value);
    }
  }

  @Override
  public void value(Integer value) {
    if (value == null) {
      nullValue();
    } else {
      writeDeferredName();
      generator.write(value);
    }
  }

  @Override
  public void value(Long value) {
    if (value == null) {
      nullValue();
    } else {
      writeDeferredName();
      generator.write(value);
    }
  }

  @Override
  public void value(Double value) {
    if (value == null) {
      nullValue();
    } else {
      writeDeferredName();
      generator.write(value);
    }
  }

  @Override
  public void value(BigDecimal value) {
    if (value == null) {
      nullValue();
    } else {
      writeDeferredName();
      generator.write(value);
    }
  }

  @Override
  public void value(BigInteger value) {
    if (value == null) {
      nullValue();
    } else {
      writeDeferredName();
      generator.write(value);
    }
  }

  @Override
  public void writeNewLine() {
    // TODO: No support to write new line characters for x-json-stream
  }

  @Override
  public void flush() {
    generator.flush();
  }

  @Override
  public void close() {
    generator.close();
  }

  @Override
  public void jsonValue(Object value) {
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

  private void writeList(List<?> value) {
    beginArray();
    for (Object element : value) {
      jsonValue(element);
    }
    endArray();
  }

  private void writeCollection(Collection<?> value) {
    beginArray();
    for (Object element : value) {
      jsonValue(element);
    }
    endArray();
  }

  private void writeMap(Map<?, ?> value) {
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
