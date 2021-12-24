package io.avaje.jsonb.diesel;

import io.avaje.jsonb.JsonIoException;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.spi.PropertyNames;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class FooWriter implements JsonWriter {

  final JGenerator generator;
  private boolean serializeEmpty;
  private boolean serializeNulls;
  private String deferredName;
  private ArrayStack<DNames> nameStack;
  private DNames currentNames;
  private boolean pushedNames;
  private int namePos = -1;

  FooWriter(JGenerator generator, boolean serializeNulls, boolean serializeEmpty) {
    this.generator = generator;
    this.serializeNulls = serializeNulls;
    this.serializeEmpty = serializeEmpty;
  }

  @Override
  public void close() {
    try {
      generator.close();
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public void flush() {
//    try {
      generator.flush();
//    } catch (Throwable e) {
//      throw new JsonIoException(e);
//    }
  }

  @Override
  public void pretty(boolean pretty) {
//    if (!pretty) {
//      generator.setPrettyPrinter(null);
//    } else {
//      generator.useDefaultPrettyPrinter();
//    }
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
    try {
      writeDeferredName();
      generator.writeStartArray();
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public void endArray() {
    //try {
      generator.writeEndArray();
    //} catch (IOException e) {
    //  throw new JsonIoException(e);
    //}
  }

  @Override
  public void beginObject() {
    try {
      writeDeferredName();
      generator.writeStartObject();
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public void endObject() {
    //try {
      generator.writeEndObject();
      if (pushedNames) {
        pushedNames = false;
        currentNames = nameStack != null ? nameStack.pop() : null;
      }
    //} catch (IOException e) {
    //  throw new JsonIoException(e);
    //}
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
      currentNames = (DNames) nextNames;
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
  public void emptyArray() {
    if (serializeEmpty) {
      try {
        writeDeferredName();
        generator.writeStartArray();
        generator.writeEndArray();
      } catch (IOException e) {
        throw new JsonIoException(e);
      }
    } else if (namePos >= 0) {
      namePos = -1;
    } else if (deferredName != null) {
      deferredName = null;
    }
  }

  @Override
  public void nullValue() {
    try {
      if (serializeNulls) {
        writeDeferredName();
        generator.writeNull();
      } else if (namePos >= 0) {
        namePos = -1;
      } else if (deferredName != null) {
        deferredName = null;
      }
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public void value(String value) {
    if (value == null) {
      nullValue();
    } else {
      try {
        writeDeferredName();
        generator.writeValue(value);
      } catch (IOException e) {
        throw new JsonIoException(e);
      }
    }
  }

  @Override
  public void value(boolean value) {
    try {
      writeDeferredName();
      generator.writeBoolean(value);
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }


  @Override
  public void value(int value) {
    try {
      writeDeferredName();
      generator.writeNumber(value);
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public void value(long value) {
    try {
      writeDeferredName();
      generator.writeNumber(value);
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public void value(double value) {
    try {
      writeDeferredName();
      generator.writeDouble(value);
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public void value(Boolean value) {
    if (value == null) {
      nullValue();
    } else {
      try {
        writeDeferredName();
        generator.writeBoolean(value);
      } catch (IOException e) {
        throw new JsonIoException(e);
      }
    }
  }

  @Override
  public void value(Integer value) {
    if (value == null) {
      nullValue();
    } else {
      try {
        writeDeferredName();
        generator.writeNumber(value);
      } catch (IOException e) {
        throw new JsonIoException(e);
      }
    }
  }

  @Override
  public void value(Long value) {
    if (value == null) {
      nullValue();
    } else {
      try {
        writeDeferredName();
        generator.writeNumber(value);
      } catch (IOException e) {
        throw new JsonIoException(e);
      }
    }
  }

  @Override
  public void value(Double value) {
    if (value == null) {
      nullValue();
    } else {
      try {
        writeDeferredName();
        generator.writeDouble(value);
      } catch (IOException e) {
        throw new JsonIoException(e);
      }
    }
  }

  @Override
  public void value(BigDecimal value) {
    if (value == null) {
      nullValue();
    } else {
      try {
        writeDeferredName();
        generator.writeDouble(value.doubleValue());
      } catch (IOException e) {
        throw new JsonIoException(e);
      }
    }
  }

  @Override
  public void value(BigInteger value) {
    if (value == null) {
      nullValue();
    } else {
      try {
        writeDeferredName();
        generator.writeNumber(value.longValueExact());
      } catch (IOException e) {
        throw new JsonIoException(e);
      }
    }
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
