package io.avaje.jsonb.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.avaje.jsonb.JsonIoException;
import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.spi.PropertyNames;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

final class JacksonReader implements JsonReader {

  private final JsonParser parser;
  private final boolean failOnUnknown;

  JacksonReader(JsonParser parser, boolean failOnUnknown) {
    this.parser = parser;
    this.failOnUnknown = failOnUnknown;
  }

  @Override
  public void names(PropertyNames names) {
    // ignore
  }

  @Override
  public void close() {
    try {
      parser.close();
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public void unmappedField(String fieldName) {
    if (failOnUnknown) {
      throw new IllegalStateException("Unknown property " + fieldName + " at " + parser.getCurrentLocation());
    }
  }

  @Override
  public void skipValue() {
    try {
      parser.skipChildren();
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public void beginArray() {
    try {
      if (parser.currentToken() == null) {
        parser.nextToken();
      }
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public void endArray() {

  }

  @Override
  public boolean hasNextElement() {
    try {
      JsonToken token = parser.nextToken();
      return token != JsonToken.END_ARRAY;
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public String path() {
    return parser.getCurrentLocation().toString();
  }

  @Override
  public boolean hasNextField() {
    try {
      return parser.nextToken() == JsonToken.FIELD_NAME;
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public String nextField() {
    try {
      String nextName = parser.getCurrentName();
      // move to next token
      parser.nextToken();
      return nextName;
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public boolean peekIsNull() {
    return parser.hasToken(JsonToken.VALUE_NULL);
    //return parser.currentToken() == JsonToken.VALUE_NULL;
  }

  @Override
  public <T> T nextNull() {
    // do nothing
    return null;
  }

  @Override
  public boolean nextBoolean() {
    try {
      return parser.getValueAsBoolean();
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public int nextInt() {
    try {
      return parser.getValueAsInt();
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public BigDecimal nextDecimal() {
    try {
      return parser.getDecimalValue();
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public BigInteger nextBigInteger() {
    try {
      return parser.getBigIntegerValue();
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public long nextLong() {
    try {
      return parser.getValueAsLong();
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public double nextDouble() {
    try {
      return parser.getValueAsDouble();
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public String nextString() {
    try {
      return parser.getValueAsString();
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public void beginObject() {
    if (parser.currentToken() == JsonToken.START_OBJECT) {
      return;
    }
    try {
      if (parser.nextToken() != JsonToken.START_OBJECT) {
        throw new IllegalStateException("Expected start object " + parser.getCurrentLocation() + " but got " + parser.currentToken());
      }
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public void endObject() {
    if (parser.currentToken() != JsonToken.END_OBJECT) {
      throw new IllegalStateException("Expected end object " + parser.getCurrentLocation() + " but got " + parser.currentToken());
    }
  }

  @Override
  public Token peek() {
    JsonToken token = parser.currentToken();
    if (token == null) {
      try {
        token = parser.nextToken();
      } catch (IOException e) {
        throw new JsonIoException(e);
      }
    }
    switch (token) {
      case START_OBJECT:
        return Token.BEGIN_OBJECT;
      case START_ARRAY:
        return Token.BEGIN_ARRAY;
      case VALUE_NULL:
        return Token.NULL;
      case VALUE_STRING:
        return Token.STRING;
      case VALUE_NUMBER_FLOAT:
      case VALUE_NUMBER_INT:
        return Token.NUMBER;
      case VALUE_FALSE:
      case VALUE_TRUE:
        return Token.BOOLEAN;
    }
    throw new IllegalStateException("Unhandled token " + token);
  }
}
