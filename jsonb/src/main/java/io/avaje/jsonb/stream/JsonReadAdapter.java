package io.avaje.jsonb.stream;

import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.spi.PropertyNames;

import java.math.BigDecimal;
import java.math.BigInteger;

final class JsonReadAdapter implements JsonReader {

  private final JsonParser reader;
  private final boolean failOnUnknown;

  public JsonReadAdapter(JsonParser reader, boolean failOnUnknown) {
    this.reader = reader;
    this.failOnUnknown = failOnUnknown;
  }

  @Override
  public void names(PropertyNames names) {
    reader.names((JsonNames) names);
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
  public void beginObject() {
    reader.startObject();
  }

  @Override
  public void endObject() {
    reader.endObject();
  }

  @Override
  public boolean hasNextElement() {
    byte nextToken = reader.nextToken();
    if (nextToken == ',') {
      reader.nextToken();
      return true;
    }
    return nextToken != ']';
  }

  @Override
  public boolean hasNextField() {
    byte nextToken = reader.nextToken();
    if (nextToken == '"') {
      return true;
    }
    if (nextToken == ',') {
      nextToken = reader.nextToken();
      return nextToken == '"';
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
  }

  @Override
  public void skipValue() {
    reader.skipValue();
  }

  @Override
  public void unmappedField(String fieldName) {
    if (failOnUnknown) {
      throw new IllegalStateException("Unknown property " + fieldName + " at " + reader.location());
    }
  }
}
