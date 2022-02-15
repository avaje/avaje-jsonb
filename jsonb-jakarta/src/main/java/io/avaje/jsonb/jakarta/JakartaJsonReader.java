package io.avaje.jsonb.jakarta;

import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.spi.PropertyNames;
import jakarta.json.stream.JsonParser;

import java.math.BigDecimal;
import java.math.BigInteger;

final class JakartaJsonReader implements JsonReader {

  private final JsonParser parser;
  private final boolean failOnUnknown;
  private JsonParser.Event currenEvent;

  JakartaJsonReader(JsonParser parser, boolean failOnUnknown) {
    this.parser = parser;
    this.failOnUnknown = failOnUnknown;
  }

  @Override
  public void names(PropertyNames names) {
    // ignore
  }

  @Override
  public void beginArray() {
    if (currenEvent == null) {
      currenEvent = parser.next();
    }
  }

  @Override
  public void endArray() {

  }

  @Override
  public boolean hasNextElement() {
    currenEvent = parser.next();
    return currenEvent != JsonParser.Event.END_ARRAY;
  }

  @Override
  public void beginObject() {
    if (currenEvent == JsonParser.Event.START_OBJECT) {
      return;
    }
    currenEvent = parser.next();
    if (currenEvent != JsonParser.Event.START_OBJECT) {
      throw new IllegalStateException("Expected start object " + parser.getLocation() + " but got " + currenEvent);
    }
  }

  @Override
  public void endObject() {
    if (currenEvent != JsonParser.Event.END_OBJECT) {
      throw new IllegalStateException("Expected end object " + parser.getLocation() + " but got " + currenEvent);
    }
  }

  @Override
  public boolean hasNextField() {
    currenEvent = parser.next();
    return currenEvent == JsonParser.Event.KEY_NAME;
  }

  @Override
  public String nextField() {
    String nextName = parser.getString();
    // move to next token
    currenEvent = parser.next();
    return nextName;
  }

  @Override
  public boolean readBoolean() {
    return currenEvent == JsonParser.Event.VALUE_TRUE;
  }

  @Override
  public int readInt() {
    return parser.getInt();
  }

  @Override
  public long readLong() {
    return parser.getLong();
  }

  @Override
  public double readDouble() {
    return parser.getBigDecimal().doubleValue();
  }

  @Override
  public BigDecimal readDecimal() {
    return parser.getBigDecimal();
  }

  @Override
  public BigInteger readBigInteger() {
    return parser.getBigDecimal().toBigInteger();
  }

  @Override
  public String readString() {
    return parser.getString();
  }

  @Override
  public boolean isNull() {
    return currenEvent == JsonParser.Event.VALUE_NULL;
  }

  @Override
  public String location() {
    return parser.getLocation().toString();
  }

  @Override
  public Token currentToken() {
    if (currenEvent == null) {
      currenEvent = parser.next();
    }
    switch (currenEvent) {
      case START_OBJECT:
        return Token.BEGIN_OBJECT;
      case START_ARRAY:
        return Token.BEGIN_ARRAY;
      case VALUE_NULL:
        return Token.NULL;
      case VALUE_STRING:
        return Token.STRING;
      case VALUE_NUMBER:
        return Token.NUMBER;
      case VALUE_FALSE:
      case VALUE_TRUE:
        return Token.BOOLEAN;
    }
    throw new IllegalStateException("Unhandled token " + currenEvent);
  }

  @Override
  public void close() {
    parser.close();
  }

  @Override
  public void skipValue() {
    parser.skipChildren();
//    if (currenEvent == JsonParser.Event.START_ARRAY) {
//      parser.skipArray();
//    } else if (currenEvent == JsonParser.Event.START_OBJECT){
//      parser.skipObject();
//    } else {
//      parser.getValue();
//    }
  }

  @Override
  public void unmappedField(String fieldName) {
    if (failOnUnknown) {
      throw new IllegalStateException("Unknown property " + fieldName + " at " + parser.getLocation());
    }
  }
}
