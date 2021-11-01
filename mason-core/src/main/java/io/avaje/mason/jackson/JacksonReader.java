package io.avaje.mason.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.avaje.mason.JsonReader;

import java.io.IOException;

class JacksonReader implements JsonReader {

  private final JsonParser parser;

  JacksonReader(JsonParser parser) {
    this.parser = parser;
  }

  @Override
  public void beginArray() {

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

//  boolean promoteNameToValue;
//
//  @Override
//  public void promoteNameToValue() {
//    promoteNameToValue = true;
//  }

  @Override
  public boolean hasNextField() throws IOException {
    return parser.nextToken() == JsonToken.FIELD_NAME;
  }

  @Override
  public String nextField() throws IOException {
    String nextName = parser.currentName();
    // move to next token
    parser.nextToken();
    return nextName;
  }

  @Override
  public boolean peekIsNull() {
    return parser.currentToken() == JsonToken.VALUE_NULL;
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
  public long nextLong() throws IOException {
    return parser.getValueAsLong();
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
    JsonToken token = parser.nextToken();
    if (token != JsonToken.START_OBJECT) {
      throw new IllegalStateException("Expected start object "+parser.getCurrentLocation());
    }
  }

  @Override
  public void endObject() {
    JsonToken token = parser.currentToken();
    if (token != JsonToken.END_OBJECT) {
      throw new IllegalStateException("Expected end object "+parser.getCurrentLocation());
    }
  }

}
