package io.avaje.jsonb.diesel;

import io.avaje.jsonb.JsonIoException;
import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.diesel.read.JReader;
import io.avaje.jsonb.spi.PropertyNames;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class JsonReadAdapter implements JsonReader {

  final JReader reader;

  public JsonReadAdapter(JReader reader) {
    this.reader = reader;
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
      if (reader.last() != ']') {
        reader.endArray();
      }
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
      //reader.getNextToken();
      return reader.readInt();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public long nextLong() {
    return 0;
  }

  @Override
  public double nextDouble() {
    return 0;
  }

  @Override
  public BigDecimal nextDecimal() {
    return null;
  }

  @Override
  public BigInteger nextBigInteger() {
    return null;
  }

  @Override
  public String nextString() {
    try {
      //byte nextToken = reader.getNextToken();
      //if (nextToken != '"') {
      //  throw new RuntimeException("expected quote");
      //}
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

  }

  @Override
  public void unmappedField(String fieldName) {

  }
}
