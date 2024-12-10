package io.avaje.json.node.adapter;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.node.*;
import io.avaje.json.stream.JsonStream;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JsonNodeAdaptersTest {

  static final JsonNodeAdapter node = JsonNodeAdapter.builder().build();

  static final JsonStream stream = JsonStream.builder().build();
  static final JsonAdapter<JsonNode> nodeAdapter = node.of(JsonNode.class);

  @Test
  void create_expect_null() {
    assertThat(node.create(LocalDate.class)).isNull();
  }

  @Test
  void create_JsonNode_expect_sameInstance() {
    JsonAdapter<?> jsonAdapter = node.create(JsonNode.class);
    JsonAdapter<JsonNode> adapter = node.of(JsonNode.class);
    assertThat(jsonAdapter).isSameAs(adapter);
  }

  @Test
  void create_JsonObject_expect_sameInstance() {
    JsonAdapter<?> jsonAdapter = node.create(JsonObject.class);
    JsonAdapter<JsonObject> adapter = node.of(JsonObject.class);
    assertThat(jsonAdapter).isSameAs(adapter);
  }

  @Test
  void create_JsonArray_expect_sameInstance() {
    JsonAdapter<?> jsonAdapter = node.create(JsonArray.class);
    JsonAdapter<JsonArray> adapter = node.of(JsonArray.class);
    assertThat(jsonAdapter).isSameAs(adapter);
  }

  @Test
  void create_JsonInteger_expect_sameInstance() {
    JsonAdapter<?> jsonAdapter = node.create(JsonInteger.class);
    JsonAdapter<JsonInteger> adapter = node.of(JsonInteger.class);
    assertThat(jsonAdapter).isSameAs(adapter);
  }

  @Test
  void create_JsonLong_expect_sameInstance() {
    JsonAdapter<?> jsonAdapter = node.create(JsonLong.class);
    JsonAdapter<JsonLong> adapter = node.of(JsonLong.class);
    assertThat(jsonAdapter).isSameAs(adapter);
  }

  @Test
  void create_JsonDouble_expect_sameInstance() {
    JsonAdapter<?> jsonAdapter = node.create(JsonDouble.class);
    JsonAdapter<JsonDouble> adapter = node.of(JsonDouble.class);
    assertThat(jsonAdapter).isSameAs(adapter);
  }

  @Test
  void create_JsonDecimal_expect_sameInstance() {
    JsonAdapter<?> jsonAdapter = node.create(JsonDecimal.class);
    JsonAdapter<JsonDecimal> adapter = node.of(JsonDecimal.class);
    assertThat(jsonAdapter).isSameAs(adapter);
  }

  @Test
  void create_JsonNumber_expect_sameInstance() {
    JsonAdapter<?> jsonAdapter = node.create(JsonNumber.class);
    JsonAdapter<JsonNumber> adapter = node.of(JsonNumber.class);
    assertThat(jsonAdapter).isSameAs(adapter);
  }

  @Test
  void create_JsonBoolean_expect_sameInstance() {
    JsonAdapter<?> jsonAdapter = node.create(JsonBoolean.class);
    JsonAdapter<JsonBoolean> adapter = node.of(JsonBoolean.class);
    assertThat(jsonAdapter).isSameAs(adapter);
  }


  @Test
  void create_JsonString_expect_sameInstance() {
    JsonAdapter<?> jsonAdapter = node.create(JsonString.class);
    JsonAdapter<JsonString> adapter = node.of(JsonString.class);
    assertThat(jsonAdapter).isSameAs(adapter);
  }


  @Test
  void arrayCreateOfMixed_defaultStream() {
    JsonArray jsonArray = JsonArray.create()
      .add(JsonInteger.of(42))
      .add(JsonString.of("foo"));

    var asJson = node.toJson(jsonArray);
    assertThat(asJson).isEqualTo("[42,\"foo\"]");

    JsonArray arrayFromJson = node.fromJson(JsonArray.class, asJson);
    assertThat(arrayFromJson.elements()).hasSize(2);

    JsonNode jsonNodeFromJson = node.fromJson(asJson);
    assertThat(jsonNodeFromJson).isInstanceOf(JsonArray.class);
  }

  @Test
  void arrayOfMixed_explicitUseOfStream() {
    JsonArray jsonArray = JsonArray.of(List.of(JsonInteger.of(42), JsonString.of("foo")));

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

    String asJson0 = node.toJson(obj);
    assertThat(asJson0).isEqualTo("{\"name\":\"foo\",\"other\":42}");

    JsonObject jsonObjectFromJson = node.fromJson(JsonObject.class, asJson0);
    assertThat(jsonObjectFromJson.elements()).containsKeys("name", "other");

    var writer = stream.bufferedWriter();
    nodeAdapter.toJson(writer, obj);
    var asJson = writer.result();
    assertThat(asJson).isEqualTo("{\"name\":\"foo\",\"other\":42}");
  }
}
