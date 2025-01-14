package io.avaje.json.node.adapter;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.json.PropertyNames;
import io.avaje.json.node.*;
import io.avaje.json.simple.SimpleMapper;
import io.avaje.json.stream.JsonStream;

import java.lang.reflect.Type;
import java.util.function.Function;

final class DJsonNodeMapper implements JsonNodeMapper {

  static final BooleanAdapter BOOLEAN_ADAPTER = new BooleanAdapter();
  static final StringAdapter STRING_ADAPTER = new StringAdapter();
  static final IntegerAdapter INTEGER_ADAPTER = new IntegerAdapter();
  static final LongAdapter LONG_ADAPTER = new LongAdapter();
  static final DoubleAdapter DOUBLE_ADAPTER = new DoubleAdapter();
  static final DecimalAdapter DECIMAL_ADAPTER = new DecimalAdapter();
  static final NumberAdapter NUMBER_ADAPTER = new NumberAdapter();

  private final JsonStream jsonStream;
  private final NodeAdapter nodeAdapter;
  private final ObjectAdapter objectAdapter;
  private final ArrayAdapter arrayAdapter;

  DJsonNodeMapper(JsonStream jsonStream, NodeAdapter nodeAdapter, ObjectAdapter objectAdapter, ArrayAdapter arrayAdapter) {
    this.jsonStream = jsonStream;
    this.nodeAdapter = nodeAdapter;
    this.objectAdapter = objectAdapter;
    this.arrayAdapter = arrayAdapter;
  }

  @Override
  public PropertyNames properties(String... names) {
    return jsonStream.properties(names);
  }

  @Override
  public <T> SimpleMapper.Type<T> type(JsonAdapter<T> customAdapter) {
    return new DMapper<>(customAdapter, jsonStream);
  }

  @Override
  public <T> SimpleMapper.Type<T> type(Function<JsonNodeMapper, JsonAdapter<T>> adapterFunction) {
    return type(adapterFunction.apply(this));
  }

  @Override
  public SimpleMapper.Type<JsonNode> nodeMapper() {
    return new DMapper<>(nodeAdapter, jsonStream);
  }

  @Override
  public SimpleMapper.Type<JsonObject> objectMapper() {
    return new DMapper<>(objectAdapter, jsonStream);
  }

  @Override
  public SimpleMapper.Type<JsonArray> arrayMapper() {
    return new DMapper<>(arrayAdapter, jsonStream);
  }

  @Override
  public String toJson(JsonNode node) {
    final var writer = jsonStream.bufferedWriter();
    nodeAdapter.toJson(writer, node);
    return writer.result();
  }

  @Override
  public void toJson(JsonNode node, JsonWriter jsonWriter) {
    nodeAdapter.toJson(jsonWriter, node);
  }

  @Override
  public JsonNode fromJson(String json) {
    try (JsonReader reader = jsonStream.reader(json)) {
      return nodeAdapter.fromJson(reader);
    }
  }

  @Override
  public JsonObject fromJsonObject(String json) {
    try (JsonReader reader = jsonStream.reader(json)) {
      return objectAdapter.fromJson(reader);
    }
  }

  @Override
  public JsonArray fromJsonArray(String json) {
    try (JsonReader reader = jsonStream.reader(json)) {
      return arrayAdapter.fromJson(reader);
    }
  }

  @Override
  public JsonNode fromJson(JsonReader jsonReader) {
    return nodeAdapter.fromJson(jsonReader);
  }

  @Override
  public JsonObject fromJsonObject(JsonReader jsonReader) {
    return objectAdapter.fromJson(jsonReader);
  }

  @Override
  public JsonArray fromJsonArray(JsonReader jsonReader) {
    return arrayAdapter.fromJson(jsonReader);
  }

  @Override
  public <T extends JsonNode> T fromJson(Class<T> type, String json) {
    JsonAdapter<T> adapter = adapter(type);
    try (JsonReader reader = jsonStream.reader(json)) {
      return adapter.fromJson(reader);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public JsonAdapter<?> adapter(Type type) {
    if (type instanceof Class) {
      Class<?> cls = (Class<?>) type;
      if (JsonNode.class.isAssignableFrom(cls)) {
        return adapter((Class<? extends JsonNode>)cls);
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends JsonNode> JsonAdapter<T> adapter(Class<T> type) {
    if (type == JsonNode.class) return (JsonAdapter<T>) nodeAdapter;
    if (type == JsonObject.class) return (JsonAdapter<T>) objectAdapter;
    if (type == JsonArray.class) return (JsonAdapter<T>) arrayAdapter;
    if (type == JsonBoolean.class) return (JsonAdapter<T>) BOOLEAN_ADAPTER;
    if (type == JsonString.class) return (JsonAdapter<T>) STRING_ADAPTER;
    if (type == JsonInteger.class) return (JsonAdapter<T>) INTEGER_ADAPTER;
    if (type == JsonLong.class) return (JsonAdapter<T>) LONG_ADAPTER;
    if (type == JsonDouble.class) return (JsonAdapter<T>) DOUBLE_ADAPTER;
    if (type == JsonDecimal.class) return (JsonAdapter<T>) DECIMAL_ADAPTER;
    if (type == JsonNumber.class) return (JsonAdapter<T>) NUMBER_ADAPTER;

    throw new IllegalArgumentException("Unexpected type " + type + " is not a JsonNode?");
  }
}
