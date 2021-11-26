package io.avaje.jsonb.spi;

import io.avaje.jsonb.JsonWriter;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Provides a delegating JsonWriter.
 */
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
  public void beginArray() {
    delegate.beginArray();
  }

  @Override
  public final void endArray() {
    delegate.endArray();
  }

  @Override
  public final void emptyArray() {
    delegate.emptyArray();
  }

  @Override
  public final  void beginObject() {
    delegate.beginObject();
  }

  @Override
  public final void endObject() {
    delegate.endObject();
  }

  @Override
  public final void name(String name) {
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
  public void nullValue() {
    delegate.nullValue();
  }

  @Override
  public final void value(String value) {
    delegate.value(value);
  }

  @Override
  public void value(boolean value) {
    delegate.value(value);
  }

  @Override
  public void value(int value) {
    delegate.value(value);
  }

  @Override
  public void value(long value) {
    delegate.value(value);
  }

  @Override
  public void value(double value) {
    delegate.value(value);
  }

  @Override
  public void value(Boolean value) {
    delegate.value(value);
  }

  @Override
  public void value(Integer value) {
    delegate.value(value);
  }

  @Override
  public void value(Long value) {
    delegate.value(value);
  }

  @Override
  public void value(Double value) {
    delegate.value(value);
  }

  @Override
  public void value(BigDecimal value) {
    delegate.value(value);
  }

  @Override
  public void value(BigInteger value) {
    delegate.value(value);
  }

  @Override
  public void jsonValue(Object value) {
    delegate.jsonValue(value);
  }

  @Override
  public void close() {
    delegate.close();
  }

  @Override
  public void flush() {
    delegate.flush();
  }
}
