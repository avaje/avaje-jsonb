package io.avaje.json.simple;

import io.avaje.json.JsonAdapter;
import io.avaje.json.core.CoreTypes;
import io.avaje.json.stream.JsonStream;

import java.util.List;
import java.util.Map;

final class DSimpleMapper implements SimpleMapper {

  private final JsonStream jsonStream;
  private final Type<Object> objectType;
  private final Type<Map<String,Object>> mapType;
  private final Type<List<Object>> listType;

  DSimpleMapper(JsonStream jsonStream, CoreTypes.CoreAdapters adapters) {
    this.jsonStream = jsonStream;
    this.objectType = new DTypeMapper<>(adapters.objectAdapter(), jsonStream);
    this.mapType = new DTypeMapper<>(adapters.mapAdapter(), jsonStream);
    this.listType = new DTypeMapper<>(adapters.listAdapter(), jsonStream);
  }

  @Override
  public <T> Type<T> type(JsonAdapter<T> myAdapter) {
    return new DTypeMapper<>(myAdapter, jsonStream);
  }

  @Override
  public Type<Object> object() {
    return objectType;
  }

  @Override
  public Type<Map<String, Object>> map() {
    return mapType;
  }

  @Override
  public Type<List<Object>> list() {
    return listType;
  }

  @Override
  public String toJson(Object object) {
    return objectType.toJson(object);
  }

  @Override
  public Object fromJson(String json) {
    return objectType.fromJson(json);
  }

  @Override
  public Map<String,Object> fromJsonObject(String json) {
    return mapType.fromJson(json);
  }

  @Override
  public List<Object> fromJsonArray(String json) {
    return listType.fromJson(json);
  }
}
