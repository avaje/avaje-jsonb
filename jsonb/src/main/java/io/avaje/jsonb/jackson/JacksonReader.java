package io.avaje.jsonb.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.avaje.jsonb.JsonReader;

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
  public void close() throws IOException {
    parser.close();
  }

  @Override
  public void unmappedField(String fieldName) {
    if (failOnUnknown) {
      throw new IllegalStateException("Unknown property " + fieldName + " at " + parser.getCurrentLocation());
    }
  }

  @Override
  public void skipValue() throws IOException {
    parser.skipChildren();
  }

  @Override
  public void beginArray() throws IOException {
    if (parser.currentToken() == null) {
      parser.nextToken();
    }
  }

  @Override
  public void endArray() {

  }

  @Override
  public boolean hasNextElement() throws IOException {
    JsonToken token = parser.nextToken();
    return token != JsonToken.END_ARRAY;
  }

  @Override
  public String path() {
    return parser.getCurrentLocation().toString();
  }

  @Override
  public boolean hasNextField() throws IOException {
    return parser.nextToken() == JsonToken.FIELD_NAME;
  }

  @Override
  public String nextField() throws IOException {
    String nextName = parser.getCurrentName();
    // move to next token
    parser.nextToken();
    return nextName;
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
  public boolean nextBoolean() throws IOException {
    return parser.getValueAsBoolean();
  }

  @Override
  public int nextInt() throws IOException {
    return parser.getValueAsInt();
  }

  @Override
  public BigDecimal nextDecimal() throws IOException {
    return parser.getDecimalValue();
  }

  @Override
  public BigInteger nextBigInteger() throws IOException {
    return parser.getBigIntegerValue();
  }

  @Override
  public long nextLong() throws IOException {
    return parser.getValueAsLong();
  }

  @Override
  public double nextDouble() throws IOException {
    return parser.getValueAsDouble();
  }

  @Override
  public String nextString() throws IOException {
    return parser.getValueAsString();
  }

  @Override
  public void beginObject() throws IOException {
    if (parser.currentToken() == JsonToken.START_OBJECT) {
      return;
    }
    if (parser.nextToken() != JsonToken.START_OBJECT) {
      throw new IllegalStateException("Expected start object " + parser.getCurrentLocation() + " but got " + parser.currentToken());
    }
  }

  @Override
  public void endObject() {
    if (parser.currentToken() != JsonToken.END_OBJECT) {
      throw new IllegalStateException("Expected end object " + parser.getCurrentLocation() + " but got " + parser.currentToken());
    }
  }

  @Override
  public Token peek() throws IOException {
    JsonToken token = parser.currentToken();
    if (token == null) {
      token = parser.nextToken();
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
