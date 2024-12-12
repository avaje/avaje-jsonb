package io.avaje.json.node;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExtractNodeTest {

  static final String content =
    "  {\n" +
    "    \"people\": [\n" +
    "      {\n" +
    "        \"type\": \"contact\",\n" +
    "        \"person\": {\n" +
    "          \"firstName\": \"Aa\",\n" +
    "          \"lastName\": \"ALast\"\n" +
    "          \"other\": \"AOther\"\n" +
    "        }\n" +
    "      },\n" +
    "      {\n" +
    "        \"type\": \"family\",\n" +
    "        \"person\": {\n" +
    "          \"firstName\": \"Bb\",\n" +
    "          \"lastName\": \"Blast\"\n" +
    "          \"other\": \"BOther\"\n" +
    "        }\n" +
    "      },\n" +
    "      {\n" +
    "        \"type\": \"family\",\n" +
    "        \"person\": {\n" +
    "          \"firstName\": \"Cc\",\n" +
    "          \"lastName\": \"CLast\"\n" +
    "        }\n" +
    "      }\n" +
    "    ]\n" +
    "  }";

  static final JsonNodeMapper mapper = JsonNodeMapper.builder().build();

  @Test
  void extract() {

    JsonObject object = ExtractNodeTest.mapper.fromJson(JsonObject.class, content);
    JsonArray arrayWithNestedPerson = (JsonArray) object.get("people");

    List<JsonNode> peopleNodes =
      arrayWithNestedPerson.stream()
      .filter(node -> "family".equals(node.extract("type")))
      .map(node -> node.extractNode("person"))
      .collect(Collectors.toList());

    assertThat(peopleNodes).hasSize(2);
    assertThat(peopleNodes.get(0)).isInstanceOf(JsonObject.class);

    List<JsonNode> lastNamesNodes =
      arrayWithNestedPerson.stream()
        .filter(node -> "family".equals(node.extract("type")))
        .map(node -> node.extractNode("person.lastName"))
        .collect(Collectors.toList());

    assertThat(lastNamesNodes).hasSize(2);
    assertThat(lastNamesNodes.get(0)).isInstanceOf(JsonString.class);


    List<String> missingOther =
      arrayWithNestedPerson.stream()
        .filter(node -> "family".equals(node.extract("type")))
        .map(node -> node.extractNode("person.other", JsonString.of("MISSING!")))
        .map(JsonNode::text)
        .collect(Collectors.toList());

    assertThat(missingOther).hasSize(2);
    assertThat(missingOther).containsExactly("BOther", "MISSING!");
  }

  @Test
  void extractMissing_expect_IllegalArgumentException() {
    JsonObject object = ExtractNodeTest.mapper.fromJson(JsonObject.class, content);

    assertThatThrownBy(() -> object.extractNode("missing.path.here"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Node not present for missing.path.here");
  }

  @Test
  void extractMissing_missing_object() {
    JsonObject object = ExtractNodeTest.mapper.fromJson(JsonObject.class, content);

    JsonNode result = object.extractNode("missing.path.here", JsonObject.empty());
    assertThat(result).isSameAs(JsonObject.empty());
  }

  @Test
  void extractMissing_missing_array() {
    JsonObject object = ExtractNodeTest.mapper.fromJson(JsonObject.class, content);

    JsonNode result = object.extractNode("missing.path.here", JsonArray.empty());
    assertThat(result).isSameAs(JsonArray.empty());
  }
}
