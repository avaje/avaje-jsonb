package io.avaje.jsonb.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import io.avaje.json.JsonIoException;
import io.avaje.json.JsonReader;
import io.avaje.json.PropertyNames;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

final class JacksonReader implements JsonReader {

  private final JsonParser parser;
  private final boolean failOnUnknown;
  private boolean streamArray;

  JacksonReader(JsonParser parser, boolean failOnUnknown) {
    this.parser = parser;
    this.failOnUnknown = failOnUnknown;
  }

  @Override
  public <T> T unwrap(Class<T> type) {
    return type.cast(parser);
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
  public String readRaw() {
    try {
      TreeNode tree = parser.getCodec().readTree(parser);
      return tree.toString();
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public JsonReader streamArray(boolean streamArray) {
    this.streamArray = streamArray;
    return this;
  }

  @Override
  public void beginStream() {
    try {
      if (streamArray && parser.currentToken() == null) {
        parser.nextToken();
      }
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public void endStream() {

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
      // token can be null when streaming new line delimited content
      return token != null && token != JsonToken.END_ARRAY;
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public String location() {
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
  public boolean isNullValue() {
    return parser.hasToken(JsonToken.VALUE_NULL);
  }

  @Override
  public boolean readBoolean() {
    try {
      return parser.getValueAsBoolean();
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public int readInt() {
    try {
      return parser.getValueAsInt();
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public BigDecimal readDecimal() {
    try {
      return parser.getDecimalValue();
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public BigInteger readBigInteger() {
    try {
      return parser.getBigIntegerValue();
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public long readLong() {
    try {
      return parser.getValueAsLong();
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public double readDouble() {
    try {
      return parser.getValueAsDouble();
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public String readString() {
    try {
      return parser.getValueAsString();
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public byte[] readBinary() {
    try {
      return parser.getBinaryValue();
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public void beginObject(PropertyNames names) {
    beginObject();
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
  public Token currentToken() {
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
