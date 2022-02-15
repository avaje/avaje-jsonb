package io.avaje.jsonb.diesel;

import io.avaje.jsonb.JsonIoException;
import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.spi.PropertyNames;

import java.io.IOException;
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
    try {
      reader.startArray();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void endArray() {
    try {
      reader.endArray();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void beginObject() {
    try {
      reader.startObject();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void endObject() {
    try {
      reader.endObject();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean hasNextElement() {
    try {
      byte nextToken = reader.nextToken();
      if (nextToken == ',') {
        reader.nextToken();
        return true;
      }
      return nextToken != ']';
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public boolean hasNextField() {
    try {
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
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public String nextField() {
    try {
      return reader.nextField();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean readBoolean() {
    try {
      return reader.readBoolean();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int readInt() {
    try {
      return reader.readInt();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public long readLong() {
    try {
      return reader.readLong();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public double readDouble() {
    try {
      return reader.readDouble();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public BigDecimal readDecimal() {
    try {
      return reader.readDecimal();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public BigInteger readBigInteger() {
    try {
      return reader.readBigInteger();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String readString() {
    try {
      return reader.readString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean isNull() {
    try {
      return reader.currentIsNull();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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
    try {
      reader.skip();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void unmappedField(String fieldName) {
    if (failOnUnknown) {
      throw new IllegalStateException("Unknown property " + fieldName + " at " + reader.location());
    }
  }
}
