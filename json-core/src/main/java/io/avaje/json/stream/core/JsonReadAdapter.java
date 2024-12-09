package io.avaje.json.stream.core;

import io.avaje.json.JsonReader;
import io.avaje.json.PropertyNames;

import java.math.BigDecimal;
import java.math.BigInteger;

final class JsonReadAdapter implements JsonReader {

  private final JsonParser reader;
  private final boolean failOnUnknown;
  private final BufferRecycler recycler;

  JsonReadAdapter(JsonParser reader, BufferRecycler recycler, boolean failOnUnknown) {
    this.reader = reader;
    this.failOnUnknown = failOnUnknown;
    this.recycler = recycler;
  }

  @Override
  public <T> T unwrap(Class<T> type) {
    return type.cast(reader);
  }

  @Override
  public void beginStream() {
    reader.startStream();
  }

  @Override
  public void endStream() {
    reader.endStream();
  }

  @Override
  public void beginArray() {
    reader.startArray();
  }

  @Override
  public void endArray() {
    reader.endArray();
  }

  @Override
  public void beginObject(PropertyNames names) {
    reader.startObject((JsonNames) names);
  }

  @Override
  public void beginObject() {
    reader.startObject();
  }

  @Override
  public void endObject() {
    reader.endObject();
  }

  @Override
  public boolean hasNextElement() {
    return reader.hasNextElement();
  }

  @Override
  public boolean hasNextStreamElement() {
    return reader.hasNextStreamElement();
  }

  @Override
  public boolean hasNextField() {
    byte nextToken = reader.nextToken();
    if (nextToken == '"') {
      return true;
    }
    if (nextToken == ',') {
      return reader.nextToken() == '"';
    } else {
      return false;
    }
  }

  @Override
  public String nextField() {
    return reader.nextField();
  }

  @Override
  public boolean readBoolean() {
    return reader.readBoolean();
  }

  @Override
  public int readInt() {
    return reader.readInt();
  }

  @Override
  public long readLong() {
    return reader.readLong();
  }

  @Override
  public double readDouble() {
    return reader.readDouble();
  }

  @Override
  public BigDecimal readDecimal() {
    return reader.readDecimal();
  }

  @Override
  public BigInteger readBigInteger() {
    return reader.readBigInteger();
  }

  @Override
  public String readString() {
    return reader.readString();
  }

  @Override
  public byte[] readBinary() {
    return reader.readBinary();
  }

  @Override
  public boolean isNullValue() {
    return reader.isNullValue();
  }

  @Override
  public String location() {
    return reader.location();
  }

  @Override
  public Token currentToken() {
    byte token = reader.currentToken();
    switch (token) {
      case '[':
        return Token.BEGIN_ARRAY;
      case '{':
        return Token.BEGIN_OBJECT;
      case 'n':
        return Token.NULL;
      case '\"':
        return Token.STRING;
      case 't':
      case 'f': {
        return Token.BOOLEAN;
      }
      default:
        return Token.NUMBER;
    }
  }

  @Override
  public void close() {
    reader.close();
    recycler.recycle(reader);
  }

  @Override
  public void skipValue() {
    reader.skipValue();
  }

  @Override
  public String readRaw() {
    return reader.readRaw();
  }

  @Override
  public void unmappedField(String fieldName) {
    if (failOnUnknown) {
      throw new IllegalStateException("Unknown property " + fieldName + " at " + reader.location());
    }
  }
}
