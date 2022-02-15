package io.avaje.jsonb.diesel;

import io.avaje.jsonb.JsonIoException;
import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.spi.PropertyNames;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class JsonReadAdapter implements JsonReader {

  private final JsonParser reader;
  private final boolean failOnUnknown;

  public JsonReadAdapter(JsonParser reader, boolean failOnUnknown) {
    this.reader = reader;
    this.failOnUnknown = failOnUnknown;
  }

  @Override
  public void names(PropertyNames names) {
    reader.names((JsonNames)names);
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
      byte nextToken = reader.getNextToken();
      if (nextToken == ',') {
        reader.getNextToken();
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
      byte nextToken = reader.getNextToken();
      if (nextToken == '"') {
        return true;
      }
      if (nextToken == ',') {
        nextToken = reader.getNextToken();
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
  public boolean nextBoolean() {
    try {
      return reader.readBool();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int nextInt() {
    try {
      return reader.readInt();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public long nextLong() {
    try {
      return reader.readLong();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public double nextDouble() {
    try {
      return reader.readDouble();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public BigDecimal nextDecimal() {
    try {
      return reader.readDecimal();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public BigInteger nextBigInteger() {
    try {
      return reader.readBigInt();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String nextString() {
    try {
      return reader.readString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean peekIsNull() {
    byte last = reader.last();
    return last == 'n';
  }

  @Override
  public <T> T nextNull() {
    return null;
  }

  @Override
  public String path() {
    return null;
  }

  @Override
  public Token peek() {
    return null;
  }

  @Override
  public void close() {
    reader.reset();
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
      throw new IllegalStateException("Unknown property " + fieldName + " at " + reader.positionDescription(0));
    }
  }
}
