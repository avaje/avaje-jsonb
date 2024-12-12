package io.avaje.json.node;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonArrayTest {

  String input =
    "{\n" +
    "    \"firstName\": \"John\",\n" +
    "    \"lastName\": \"doe\",\n" +
    "    \"age\": 26,\n" +
    "    \"address\": {\n" +
    "        \"streetAddress\": \"naist street\",\n" +
    "        \"city\": \"Nara\",\n" +
    "        \"postalCode\": \"630-0192\"\n" +
    "    },\n" +
    "    \"phoneNumbers\": [\n" +
    "        {\n" +
    "            \"type\": \"iPhone\",\n" +
    "            \"number\": \"0123-4567-8888\"\n" +
    "        },\n" +
    "        {\n" +
    "            \"type\": \"home\",\n" +
    "            \"number\": \"0123-4567-8910\"\n" +
    "        },\n" +
    "        {\n" +
    "            \"type\": \"home\",\n" +
    "            \"number\": \"563-4567-8910\"\n" +
    "        }\n" +
    "    ]\n" +
    "}";

  static final JsonNodeMapper mapper = JsonNodeMapper.builder().build();

  static final JsonArray basicArray = JsonArray.create()
    .add(42)
    .add("foo");


  @Test
  void isEqualTo() {
    var array0 = JsonArray.create().add(1).add(2);
    var array1 = JsonArray.create().add(1).add(2);

    assertThat(array1).isEqualTo(array0);
  }

  @Test
  void isEqualTo_expect_false() {
    var array0 = JsonArray.create().add(1).add(2);
    var array1 = JsonArray.create().add(1);

    assertThat(array1).isNotEqualTo(array0);
  }

  @Test
  void streamFilter() {

    JsonObject top = mapper.fromJson(JsonObject.class, input);
    JsonArray phoneNumbers = (JsonArray)top.get("phoneNumbers");

    List<String> result =
      phoneNumbers.stream()
      .filter(n -> "home".equals(n.extract("type")))
      .map(n -> n.extract("number"))
      .collect(Collectors.toList());

    assertThat(result).hasSize(2);
  }

  @Test
  void type() {
    assertThat(basicArray.type()).isEqualTo(JsonNode.Type.ARRAY);
    assertThat(basicArray.type().isArray()).isTrue();
    assertThat(basicArray.type().isObject()).isFalse();
    assertThat(basicArray.type().isValue()).isFalse();
  }

  @Test
  void text() {
    assertThat(JsonArray.create().text()).isEqualTo("[]");
    assertThat(basicArray.text()).isEqualTo("[42, foo]");
  }

  @Test
  void add() {
    JsonArray array = JsonArray.create()
      .add("string")
      .add(1).add(99L)
      .add(true)
      .add(JsonObject.create());

    List<JsonNode> elements = array.elements();
    assertThat(elements).hasSize(5);
    assertThat(elements.get(0)).isInstanceOf(JsonString.class);
    assertThat(elements.get(1)).isInstanceOf(JsonInteger.class);
    assertThat(elements.get(2)).isInstanceOf(JsonLong.class);
    assertThat(elements.get(3)).isInstanceOf(JsonBoolean.class);
    assertThat(elements.get(4)).isInstanceOf(JsonObject.class);
  }

  @Test
  void copy() {
    final JsonArray source = JsonArray.create()
            .add("foo")
            .add(JsonObject.create().add("b", 42));

    JsonArray copy = source.copy();
    assertThat(copy.toString()).isEqualTo(source.toString());

    copy.add("canMutate");
    assertThat(copy.size()).isEqualTo(3);
    assertThat(source.size()).isEqualTo(2);
  }

  @Test
  void unmodifiable() {
    final JsonArray source = JsonArray.create()
            .add("foo")
            .add(JsonObject.create().add("b", 42));

    JsonArray copy = source.unmodifiable();
    assertThat(copy.toString()).isEqualTo(source.toString());

    assertThatThrownBy(() -> copy.add("canMutate"))
            .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void toPlain() {
    final JsonArray source = JsonArray.create()
      .add("foo")
      .add(JsonObject.create().add("b", 42));

    List<Object> plain = source.toPlain();
    assertThat(plain).hasSize(2);
    assertThat(plain.get(0)).isEqualTo("foo");
    assertThat(plain.get(1)).isEqualTo(Map.of("b", 42));
  }

}
