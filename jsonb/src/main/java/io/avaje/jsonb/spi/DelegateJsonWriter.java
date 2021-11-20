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
  public void serializeEmpty(boolean serializeEmpty) {
    delegate.serializeEmpty(serializeEmpty);
  }

  @Override
  public String path() {
    return delegate.path();
  }

  @Override
  public void beginArray() throws IOException {
    delegate.beginArray();
  }

  @Override
  public final void endArray() throws IOException {
    delegate.endArray();
  }

  @Override
  public final void emptyArray() throws IOException {
    delegate.emptyArray();
  }

  @Override
  public final  void beginObject() throws IOException {
    delegate.beginObject();
  }

  @Override
  public final void endObject() throws IOException {
    delegate.endObject();
  }

  @Override
  public final void name(String name) throws IOException {
    delegate.name(name);
  }

  @Override
  public void names(PropertyNames names) {
    delegate.names(names);
  }

  @Override
  public void name(int position) {
    delegate.name(position);
  }

  @Override
  public void nullValue() throws IOException {
    delegate.nullValue();
  }

  @Override
  public void rawValue(String value) throws IOException {
    delegate.rawValue(value);
  }

  @Override
  public final void value(String value) throws IOException {
    delegate.value(value);
  }

  @Override
  public void value(boolean value) throws IOException {
    delegate.value(value);
  }

  @Override
  public void value(int value) throws IOException {
    delegate.value(value);
  }

  @Override
  public void value(long value) throws IOException {
    delegate.value(value);
  }

  @Override
  public void value(double value) throws IOException {
    delegate.value(value);
  }

  @Override
  public void value(Boolean value) throws IOException {
    delegate.value(value);
  }

  @Override
  public void value(Integer value) throws IOException {
    delegate.value(value);
  }

  @Override
  public void value(Long value) throws IOException {
    delegate.value(value);
  }

  @Override
  public void value(Double value) throws IOException {
    delegate.value(value);
  }

  @Override
  public void value(BigDecimal value) throws IOException {
    delegate.value(value);
  }

  @Override
  public void jsonValue(Object value) throws IOException {
    delegate.jsonValue(value);
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
