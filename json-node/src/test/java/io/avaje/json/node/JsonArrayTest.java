package io.avaje.json.node;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

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

  static final JsonNodeAdapter node = JsonNodeAdapter.builder().build();

  static final JsonArray basicArray = JsonArray.create()
    .add(JsonInteger.of(42))
    .add(JsonString.of("foo"));

  @Test
  void streamFilter() {

    JsonObject top = node.fromJson(JsonObject.class, input);
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
}
