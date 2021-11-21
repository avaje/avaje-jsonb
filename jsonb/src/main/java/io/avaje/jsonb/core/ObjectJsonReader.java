package io.avaje.jsonb.core;

import io.avaje.jsonb.JsonReader;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * JsonReader with a source of java objects, Map, Collection etc.
 */
final class ObjectJsonReader implements JsonReader {

  private final Object source;
  private Object currentValue;
  private Iterator<?> collectionIterator;
  private Iterator<Map.Entry<String, Object>> mapIterator;

  ObjectJsonReader(Object source) {
    this.source = source;
    this.currentValue = source;
  }

  @Override
  public void unmappedField(String fieldName) {

  }

  @Override
  public void close() {
    // do nothing
  }

  @Override
  public void skipValue() {
    // do nothing
  }

  @Override
  public void beginArray() {
    collectionIterator = ((Collection<?>) currentValue).iterator();
  }

  @Override
  public void endArray() {
    // do nothing (could maintain the current location for better errors?)
  }

  @Override
  public boolean hasNextElement() {
    if (collectionIterator.hasNext()) {
      currentValue = collectionIterator.next();
      return true;
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void beginObject() {
    Map<String, Object> map = (Map<String, Object>) currentValue;
    mapIterator = map.entrySet().iterator();
  }

  @Override
  public void endObject() {
    // do nothing (could maintain the current location for better errors?)
  }

  @Override
  public boolean hasNextField() {
    return mapIterator.hasNext();
  }

  @Override
  public String nextField() {
    final Map.Entry<String, Object> mapEntry = mapIterator.next();
    currentValue = mapEntry.getValue();
    return mapEntry.getKey();
  }

  @Override
  public boolean nextBoolean() {
    if (currentValue instanceof Boolean) {
      return (Boolean) currentValue;
    }
    return Boolean.parseBoolean(currentValue.toString());
  }

  @Override
  public int nextInt() {
    if (currentValue instanceof Integer) {
      return (Integer) currentValue;
    }
    return Integer.parseInt(currentValue.toString());
  }

  @Override
  public long nextLong() {
    if (currentValue instanceof Long) {
      return (Long) currentValue;
    }
    return Long.parseLong(currentValue.toString());
  }

  @Override
  public double nextDouble() {
    if (currentValue instanceof Double) {
      return (Double) currentValue;
    }
    return Double.parseDouble(currentValue.toString());
  }

  @Override
  public String nextString() {
    if (currentValue instanceof String) {
      return (String) currentValue;
    }
    return currentValue.toString();
  }

  @Override
  public BigDecimal nextDecimal() {
    if (currentValue instanceof BigDecimal) {
      return (BigDecimal) currentValue;
    }
    return new BigDecimal(currentValue.toString());
  }

  @Override
  public BigInteger nextBigInteger() {
    if (currentValue instanceof BigInteger) {
      return (BigInteger) currentValue;
    }
    return new BigInteger(currentValue.toString());
  }

  @Override
  public boolean peekIsNull() {
    return currentValue == null;
  }

  @Override
  public <T> T nextNull() {
    return null;
  }

  @Override
  public String path() {
    // not maintaining a path at this point
    return "unknown";
  }

  @Override
  public Token peek() {
    throw new IllegalStateException("not called");
  }

  @Override
  public String toString() {
    return String.valueOf(source);
  }
}
