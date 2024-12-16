package io.avaje.json.core;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;

import java.util.List;
import java.util.Map;

final class BasicObjectAdapter implements JsonAdapter<Object>, CoreTypes.CoreAdapters  {

  private final JsonAdapter<String> stringAdapter;
  private final JsonAdapter<Double> doubleAdapter;
  private final JsonAdapter<Integer> intAdapter;
  private final JsonAdapter<Long> longAdapter;
  private final JsonAdapter<Boolean> booleanAdapter;
  private final JsonAdapter<List<Object>> listAdapter;
  private final JsonAdapter<Map<String,Object>> mapAdapter;

  BasicObjectAdapter() {
    this.stringAdapter = CoreTypes.create(String.class);
    this.intAdapter = CoreTypes.create(Integer.class);
    this.doubleAdapter = CoreTypes.create(Double.class);
    this.longAdapter = CoreTypes.create(Long.class);
    this.booleanAdapter = CoreTypes.create(Boolean.class);
    this.listAdapter = CoreTypes.createList(this);
    this.mapAdapter = CoreTypes.createMap(this);
  }

  @Override
  public JsonAdapter<Object> objectAdapter() {
    return this;
  }

  @Override
  public JsonAdapter<List<Object>> listAdapter() {
    return listAdapter;
  }

  @Override
  public JsonAdapter<Map<String, Object>> mapAdapter() {
    return mapAdapter;
  }

  @Override
  public Object fromJson(JsonReader reader) {
    switch (reader.currentToken()) {
      case BEGIN_ARRAY:
        return listAdapter.fromJson(reader);
      case BEGIN_OBJECT:
        return mapAdapter.fromJson(reader);
      case STRING:
        return stringAdapter.fromJson(reader);
      case NUMBER:
        var d = doubleAdapter.fromJson(reader);
        if (d % 1 == 0) {
          return d.longValue();
        }
        return d;
      case BOOLEAN:
        return booleanAdapter.fromJson(reader);
      case NULL:
        reader.isNullValue();
        return null;
      default:
        throw new IllegalStateException("Expected a value but was " + reader.currentToken() + " at path " + reader.location());
    }
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void toJson(JsonWriter writer, Object value) {
    if (value == null) {
      writer.nullValue();
      return;
    }
    final Class<?> valueClass = value.getClass();
    if (valueClass == String.class) {
      stringAdapter.toJson(writer, (String)value);
    } else if (value instanceof List) {
      listAdapter.toJson(writer, (List) value);
    } else if (value instanceof Boolean) {
      booleanAdapter.toJson(writer, (Boolean)value);
    } else if (value instanceof Integer) {
      intAdapter.toJson(writer, (Integer)value);
    } else if (value instanceof Long) {
      longAdapter.toJson(writer, (Long)value);
    } else if (value instanceof Double) {
      doubleAdapter.toJson(writer, (Double)value);
    } else if (Map.class.isAssignableFrom(valueClass)) {
      mapAdapter.toJson(writer, (Map)value);
    } else {
      throw new UnsupportedOperationException("Type not supported " + value.getClass());
    }
  }

  @Override
  public String toString() {
    return "BasicObjectAdapter";
  }
}
