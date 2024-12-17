package io.avaje.json.node.adapter;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.node.*;
import io.avaje.json.simple.SimpleMapper;
import io.avaje.json.stream.BufferedJsonWriter;
import io.avaje.json.stream.JsonStream;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JsonNodeAdaptersTest {

  static final JsonNodeMapper mapper = JsonNodeMapper.builder().build();

  static final JsonStream stream = JsonStream.builder().build();
  static final JsonAdapter<JsonNode> nodeAdapter = mapper.adapter(JsonNode.class);

  @Test
  void create_expect_null() {
    assertThat(mapper.adapter(LocalDate.class)).isNull();
  }

  @Test
  void create_JsonNode_expect_sameInstance() {
    JsonAdapter<?> jsonAdapter = mapper.adapter(JsonNode.class);
    JsonAdapter<JsonNode> adapter = mapper.adapter(JsonNode.class);
    assertThat(jsonAdapter).isSameAs(adapter);
  }

  @Test
  void create_JsonObject_expect_sameInstance() {
    JsonAdapter<?> jsonAdapter = mapper.adapter(JsonObject.class);
    JsonAdapter<JsonObject> adapter = mapper.adapter(JsonObject.class);
    assertThat(jsonAdapter).isSameAs(adapter);
  }

  @Test
  void create_JsonArray_expect_sameInstance() {
    JsonAdapter<?> jsonAdapter = mapper.adapter(JsonArray.class);
    JsonAdapter<JsonArray> adapter = mapper.adapter(JsonArray.class);
    assertThat(jsonAdapter).isSameAs(adapter);
  }

  @Test
  void create_JsonInteger_expect_sameInstance() {
    JsonAdapter<?> jsonAdapter = mapper.adapter(JsonInteger.class);
    JsonAdapter<JsonInteger> adapter = mapper.adapter(JsonInteger.class);
    assertThat(jsonAdapter).isSameAs(adapter);
  }

  @Test
  void create_JsonLong_expect_sameInstance() {
    JsonAdapter<?> jsonAdapter = mapper.adapter(JsonLong.class);
    JsonAdapter<JsonLong> adapter = mapper.adapter(JsonLong.class);
    assertThat(jsonAdapter).isSameAs(adapter);
  }

  @Test
  void create_JsonDouble_expect_sameInstance() {
    JsonAdapter<?> jsonAdapter = mapper.adapter(JsonDouble.class);
    JsonAdapter<JsonDouble> adapter = mapper.adapter(JsonDouble.class);
    assertThat(jsonAdapter).isSameAs(adapter);
  }

  @Test
  void create_JsonDecimal_expect_sameInstance() {
    JsonAdapter<JsonDecimal> jsonAdapter = mapper.adapter(JsonDecimal.class);
    JsonAdapter<JsonDecimal> adapter = mapper.adapter(JsonDecimal.class);
    assertThat(jsonAdapter).isSameAs(adapter);

    SimpleMapper.Type<JsonDecimal> type = mapper.type(jsonAdapter);

    String asJson1 = type.toJson(JsonDecimal.of(new BigDecimal("23.45")));
    assertThat(asJson1).isEqualTo("23.45");
    assertThat(type.fromJson(asJson1)).isEqualTo(JsonDecimal.of(new BigDecimal("23.45")));
  }

  @Test
  void create_JsonNumber_expect_sameInstance() {
    JsonAdapter<?> jsonAdapter = mapper.adapter(JsonNumber.class);
    JsonAdapter<JsonNumber> adapter = mapper.adapter(JsonNumber.class);
    assertThat(jsonAdapter).isSameAs(adapter);
  }

  @Test
  void create_JsonBoolean_expect_sameInstance() {
    JsonAdapter<?> jsonAdapter = mapper.adapter(JsonBoolean.class);
    JsonAdapter<JsonBoolean> adapter = mapper.adapter(JsonBoolean.class);
    assertThat(jsonAdapter).isSameAs(adapter);
  }


  @Test
  void create_JsonString_expect_sameInstance() {
    JsonAdapter<?> jsonAdapter = mapper.adapter(JsonString.class);
    JsonAdapter<JsonString> adapter = mapper.adapter(JsonString.class);
    assertThat(jsonAdapter).isSameAs(adapter);
  }

  @Test
  void toJsonWriter() {
    BufferedJsonWriter writer = stream.bufferedWriter();
    mapper.toJson(JsonArray.create().add(1).add(2), writer);
    assertThat(writer.result()).isEqualTo("[1,2]");
  }

  @Test
  void fromJson_usingReader() {
    try (var reader = stream.reader("[42, \"foo\"]")) {
      JsonNode node = mapper.fromJson(reader);
      assertThat(node).isEqualTo(JsonArray.create().add(42L).add("foo"));
    }
  }

  @Test
  void fromJsonArray_usingReader() {
    try (var reader = stream.reader("[42, \"foo\"]")) {
      JsonArray node = mapper.fromJsonArray(reader);
      assertThat(node).isEqualTo(JsonArray.create().add(42L).add("foo"));
    }
  }

  @Test
  void fromJsonObject_usingReader() {
    try (var reader = stream.reader("{\"a\":1,\"b\":2}")) {
      JsonObject node = mapper.fromJsonObject(reader);
      assertThat(node).isEqualTo(JsonObject.create().add("a", 1L).add("b", 2L));
    }
  }

  @Test
  void arrayCreateOfMixed_defaultStream() {
    JsonArray jsonArray = JsonArray.create()
      .add(42)
      .add("foo");

    var asJson = mapper.toJson(jsonArray);
    assertThat(asJson).isEqualTo("[42,\"foo\"]");

    JsonArray arrayFromJson = mapper.fromJsonArray(asJson);
    assertThat(arrayFromJson.elements()).hasSize(2);

    JsonNode jsonNodeFromJson = mapper.fromJson(asJson);
    assertThat(jsonNodeFromJson).isInstanceOf(JsonArray.class);
  }

  @Test
  void arrayOfMixed_explicitUseOfStream() {
    JsonArray jsonArray = JsonArray.of(List.of(JsonInteger.of(42), JsonString.of("foo")));

    assertThat(jsonArray.isEmpty()).isFalse();
    assertThat(jsonArray.size()).isEqualTo(2);

    var writer = stream.bufferedWriter();
    nodeAdapter.toJson(writer, jsonArray);
    var asJson = writer.result();
    assertThat(asJson).isEqualTo("[42,\"foo\"]");

    JsonReader reader = stream.reader(asJson);
    JsonNode fromJsonNode = nodeAdapter.fromJson(reader);
    assertThat(fromJsonNode).isInstanceOf(JsonArray.class);
  }

  @Test
  void object() {
    var obj = JsonObject.create()
      .add("name", JsonString.of("foo"))
      .add("other", JsonInteger.of(42));

    assertThat(obj.isEmpty()).isFalse();
    assertThat(obj.size()).isEqualTo(2);
    assertThat(obj.containsKey("name")).isTrue();
    assertThat(obj.containsKey("DoesNotExist")).isFalse();

    String asJson0 = mapper.toJson(obj);
    assertThat(asJson0).isEqualTo("{\"name\":\"foo\",\"other\":42}");

    JsonObject jsonObjectFromJson = mapper.fromJson(JsonObject.class, asJson0);
    assertThat(jsonObjectFromJson.elements()).containsKeys("name", "other");

    var writer = stream.bufferedWriter();
    nodeAdapter.toJson(writer, obj);
    var asJson = writer.result();
    assertThat(asJson).isEqualTo("{\"name\":\"foo\",\"other\":42}");
  }
}
