package io.avaje.json.stream.core;

import io.avaje.json.JsonWriter;
import io.avaje.json.PropertyNames;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;

final class JsonWriteAdapter implements JsonWriter {

  private final JsonGenerator generator;
  private final BufferRecycler recycler;
  private boolean serializeEmpty;
  private boolean serializeNulls;
  private String deferredName;
  private int namePos = -1;

  JsonWriteAdapter(JsonGenerator generator, BufferRecycler recycler, boolean serializeNulls, boolean serializeEmpty) {
    this.generator = generator;
    this.serializeNulls = serializeNulls;
    this.serializeEmpty = serializeEmpty;
    this.recycler = recycler;
  }

  @Override
  public <T> T unwrap(Class<T> type) {
    return type.cast(generator);
  }

  @Override
  public void markIncomplete() {
    generator.markIncomplete();
  }

  @Override
  public void close() {
    generator.close();
    recycler.recycle(generator);
  }

  @Override
  public void flush() {
    generator.flush();
  }

  @Override
  public void pretty(boolean pretty) {
    generator.pretty(pretty);
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
  public void beginArray() {
    writeDeferredName();
    generator.startArray();
  }

  @Override
  public void endArray() {
    generator.endArray();
  }

  @Override
  public void beginObject() {
    writeDeferredName();
    generator.startObject();
  }

  @Override
  public void beginObject(PropertyNames names) {
    writeDeferredName();
    generator.startObject((JsonNames) names);
  }

  @Override
  public void endObject() {
    generator.endObject();
  }

  @Override
  public void name(String name) {
    deferredName = name;
  }

  @Override
  public void allNames(PropertyNames names) {
    generator.allNames((JsonNames) names);
  }

  @Override
  public void name(int position) {
    this.namePos = position;
  }

  void writeDeferredName() {
    if (namePos > -1) {
      generator.writeName(namePos);
      namePos = -1;
    } else if (deferredName != null) {
      generator.writeName(deferredName);
      deferredName = null;
    }
  }

  @Override
  public void emptyArray() {
    if (serializeEmpty) {
      writeDeferredName();
      generator.startArray();
      generator.endArray();
    } else if (namePos >= 0) {
      namePos = -1;
    } else if (deferredName != null) {
      deferredName = null;
    }
  }

  @Override
  public void nullValue() {
    if (serializeNulls) {
      writeDeferredName();
      generator.writeNull();
    } else if (namePos >= 0) {
      namePos = -1;
    } else if (deferredName != null) {
      deferredName = null;
    }
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
  public void value(byte[] value) {
    if (value == null) {
      nullValue();
    } else {
      writeDeferredName();
      generator.write(value);
    }
  }

  @Override
  public void rawValue(String value) {
    if (value == null) {
      nullValue();
    } else {
      writeDeferredName();
      generator.writeRaw(value);
    }
  }

  @Override
  public void rawChunkStart() {
    writeDeferredName();
    generator.startRawChunk();
  }

  @Override
  public void rawChunk(char value) {
    generator.writeRawChunk(value);
  }

  @Override
  public void rawChunk(String value) {
    generator.writeRawChunk(value);
  }

  @Override
  public void rawChunkEncode(String value) {
    generator.writeRawChunkEncode(value);
  }

  @Override
  public void rawChunkEnd() {
    // do nothing
  }

  @Override
  public void writeNewLine() {
    generator.writeNewLine();
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
      value((String) value);
    } else if (value instanceof Boolean) {
      value(((Boolean) value).booleanValue());
    } else if (value instanceof Integer) {
      value(((Integer) value).intValue());
    } else if (value instanceof Long) {
      value(((Long) value).longValue());
    } else if (value instanceof Double) {
      value(((Double) value).doubleValue());
    } else if (value instanceof BigDecimal) {
      value((BigDecimal) value);
    } else if (value instanceof byte[]) {
      value((byte[]) value);
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
