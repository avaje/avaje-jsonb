package org.example.other;

import io.avaje.json.node.JsonNode;
import io.avaje.json.node.JsonObject;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonNodeAdaptersTest {

  static final Jsonb jsonb = Jsonb.builder().build();
  static final JsonType<JsonNode> nodeType = jsonb.type(JsonNode.class);

  @Test
  void test() {
    var obj = JsonObject.create().add("name", "foo").add("val", 42);

    String asJson = nodeType.toJson(obj);
    assertThat(asJson).isEqualTo("{\"name\":\"foo\",\"val\":42}");

    String asJson2 = jsonb.toJson(obj);
    assertThat(asJson2).isEqualTo(asJson);

    JsonNode fromJson = nodeType.fromJson(asJson);
    assertThat(fromJson).isInstanceOf(JsonObject.class);

    JsonObject objFromJson = (JsonObject) fromJson;
    assertThat(objFromJson.elements()).containsKeys("name", "val");
  }

}
