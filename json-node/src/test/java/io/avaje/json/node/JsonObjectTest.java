package io.avaje.json.node;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JsonObjectTest {

  final JsonObject basicObject = JsonObject.create()
    .add("name", JsonString.of("foo"))
    .add("other", JsonInteger.of(42));

  final JsonObject emptyObject = JsonObject.create();

  @Test
  void of() {
    Map<String,JsonNode> map = new LinkedHashMap<>();
    map.put("name", JsonString.of("foo"));
    map.put("other", JsonInteger.of(42));

    JsonObject immutableJsonObject = JsonObject.of(map);
    assertThat(immutableJsonObject.elements()).containsOnlyKeys("name", "other");
  }

  @Test
  void type() {
    assertThat(emptyObject.type()).isEqualTo(JsonNode.Type.OBJECT);
  }

  @Test
  void text() {
    assertThat(emptyObject.text()).isEqualTo("{}");
    assertThat(basicObject.text()).isEqualTo("{name=foo, other=42}");
  }

  @Test
  void isEmpty() {
    assertThat(basicObject.isEmpty()).isFalse();
    assertThat(emptyObject.isEmpty()).isTrue();
  }

  @Test
  void size() {
    assertThat(basicObject.size()).isEqualTo(2);
    assertThat(emptyObject.size()).isEqualTo(0);
  }

  @Test
  void containsKey() {
    assertThat(basicObject.containsKey("name")).isTrue();
    assertThat(basicObject.containsKey("DoesNotExist")).isFalse();
    assertThat(emptyObject.containsKey("DoesNotExist")).isFalse();
  }

  @Test
  void elements() {
    assertThat(emptyObject.elements()).isInstanceOf(Map.class);
    assertThat(emptyObject.elements()).isEmpty();
    assertThat(basicObject.elements()).isInstanceOf(Map.class);
    assertThat(basicObject.elements()).hasSize(2);
  }

  @Test
  void add() {
    var obj = JsonObject.create()
      .add("name", JsonString.of("foo"));
    assertThat(obj.containsKey("name")).isTrue();
    assertThat(obj.size()).isEqualTo(1);
  }

  @Test
  void get() {
    Optional<JsonNode> name = basicObject.get("name");
    assertThat(name).isNotEmpty();
    String nameVal = name.stream()
      .map(JsonNode::text)
      .findFirst()
      .orElse("NotPresent");

    assertThat(nameVal).isEqualTo("foo");
  }
}
