package io.avaje.json.stream;

import io.avaje.json.JsonWriter;
import io.avaje.json.PropertyNames;

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
  public <T> T unwrap(Class<T> type) {
    return delegate.unwrap(type);
  }

  @Override
  public final void serializeNulls(boolean serializeNulls) {
    delegate.serializeNulls(serializeNulls);
  }

  @Override
  public final boolean serializeNulls() {
    return delegate.serializeNulls();
  }

  @Override
  public final boolean serializeEmpty() {
    return delegate.serializeEmpty();
  }

  @Override
  public final void serializeEmpty(boolean serializeEmpty) {
    delegate.serializeEmpty(serializeEmpty);
  }

  @Override
  public final void pretty(boolean pretty) {
    delegate.pretty(pretty);
  }

  @Override
  public final String path() {
    return delegate.path();
  }

  @Override
  public final void beginArray() {
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
  public final void beginObject(PropertyNames names) {
    delegate.beginObject(names);
  }

  @Override
  public final void beginObject() {
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
  public void allNames(PropertyNames names) {
    delegate.allNames(names);
  }

  @Override
  public final void name(int position) {
    delegate.name(position);
  }

  @Override
  public final void nullValue() {
    delegate.nullValue();
  }

  @Override
  public final void value(String value) {
    delegate.value(value);
  }

  @Override
  public final void value(boolean value) {
    delegate.value(value);
  }

  @Override
  public final void value(int value) {
    delegate.value(value);
  }

  @Override
  public final void value(long value) {
    delegate.value(value);
  }

  @Override
  public final void value(double value) {
    delegate.value(value);
  }

  @Override
  public final void value(Boolean value) {
    delegate.value(value);
  }

  @Override
  public final void value(Integer value) {
    delegate.value(value);
  }

  @Override
  public final void value(Long value) {
    delegate.value(value);
  }

  @Override
  public final void value(Double value) {
    delegate.value(value);
  }

  @Override
  public final void value(BigDecimal value) {
    delegate.value(value);
  }

  @Override
  public final void value(BigInteger value) {
    delegate.value(value);
  }

  @Override
  public void value(byte[] value) {
    delegate.value(value);
  }

  @Override
  public void rawValue(String value) {
    delegate.rawValue(value);
  }

  @Override
  public final void jsonValue(Object value) {
    delegate.jsonValue(value);
  }

  @Override
  public final void writeNewLine() {
    delegate.writeNewLine();
  }

  @Override
  public final void flush() {
    delegate.flush();
  }

  @Override
  public void close() {
    delegate.close();
  }

  @Override
  public void markIncomplete() {
    delegate.markIncomplete();
  }
}
