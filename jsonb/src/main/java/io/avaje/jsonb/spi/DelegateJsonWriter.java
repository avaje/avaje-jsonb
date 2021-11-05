package io.avaje.jsonb.spi;

import io.avaje.jsonb.JsonWriter;

import java.io.IOException;
import java.math.BigDecimal;

public abstract class DelegateJsonWriter implements JsonWriter {

  protected final JsonWriter delegate;

  public DelegateJsonWriter(JsonWriter delegate) {
    this.delegate = delegate;
  }

  @Override
  public void indent(String indent) {
    delegate.indent(indent);
  }

  @Override
  public String indent() {
    return delegate.indent();
  }

  @Override
  public void lenient(boolean lenient) {
    delegate.lenient(lenient);
  }

  @Override
  public boolean lenient() {
    return delegate.lenient();
  }

  @Override
  public void serializeNulls(boolean serializeNulls) {
    delegate.serializeNulls(serializeNulls);
  }

  @Override
  public boolean serializeNulls() {
    return delegate.serializeNulls();
  }

  @Override
  public boolean serializeEmpty() {
    return delegate.serializeEmpty();
  }

  @Override
  public JsonWriter serializeEmpty(boolean serializeEmpty) {
    delegate.serializeEmpty(serializeEmpty);
    return this;
  }

  @Override
  public String path() {
    return delegate.path();
  }

  @Override
  public JsonWriter beginArray() throws IOException {
    delegate.beginArray();
    return this;
  }

  @Override
  public JsonWriter endArray() throws IOException {
    delegate.endArray();
    return this;
  }

  @Override
  public JsonWriter emptyArray() throws IOException {
    return null;
  }

  @Override
  public JsonWriter beginObject() throws IOException {
    delegate.beginObject();
    return this;
  }

  @Override
  public JsonWriter endObject() throws IOException {
    delegate.endObject();
    return this;
  }

  @Override
  public JsonWriter name(String name) throws IOException {
    delegate.name(name);
    return this;
  }

  @Override
  public JsonWriter nullValue() throws IOException {
    delegate.nullValue();
    return this;
  }

  @Override
  public JsonWriter value(String value) throws IOException {
    delegate.value(value);
    return this;
  }

  @Override
  public JsonWriter value(boolean value) throws IOException {
    delegate.value(value);
    return this;
  }

  @Override
  public JsonWriter value(int value) throws IOException {
    delegate.value(value);
    return this;
  }

  @Override
  public JsonWriter value(long value) throws IOException {
    delegate.value(value);
    return this;
  }

  @Override
  public JsonWriter value(double value) throws IOException {
    delegate.value(value);
    return this;
  }

  @Override
  public JsonWriter value(Boolean value) throws IOException {
    delegate.value(value);
    return this;
  }

  @Override
  public JsonWriter value(Integer value) throws IOException {
    delegate.value(value);
    return this;
  }

  @Override
  public JsonWriter value(Long value) throws IOException {
    delegate.value(value);
    return this;
  }

  @Override
  public JsonWriter value(Double value) throws IOException {
    delegate.value(value);
    return this;
  }

  @Override
  public JsonWriter value(BigDecimal value) throws IOException {
    delegate.value(value);
    return this;
  }

  @Override
  public JsonWriter jsonValue(Object value) throws IOException {
    delegate.jsonValue(value);
    return this;
  }

  @Override
  public void close() throws IOException {
    delegate.close();
  }

  @Override
  public void flush() throws IOException {
    delegate.flush();
  }
}
