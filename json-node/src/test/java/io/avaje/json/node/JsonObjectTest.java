package io.avaje.json.node;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    assertThat(emptyObject.type().isObject()).isTrue();
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
    JsonNode name = basicObject.get("name");
    assertThat(name.text()).isEqualTo("foo");
  }

  @Test
  void find() {
    JsonNode name = basicObject.find("name");
    assertThat(name).isNotNull();
    assertThat(name).isInstanceOf(JsonString.class);
  }


  @Test
  void findNested() {
    var node = JsonObject.create()
      .add("person", JsonObject.create().add("name", "myName").add("type", "doo").add("active", true))
      .add("address", JsonObject.create().add("size", 42).add("junk", 99L).add("other", JsonObject.create().add("deep", "one")));

    JsonNode name = node.find("name");
    assertThat(name).isNull();

    JsonNode personName = node.find("person.name");
    assertThat(personName).isNotNull();
    assertThat(personName.text()).isEqualTo("myName");

    assertThat(node.extract("person.name")).isEqualTo("myName");
    assertThat(node.extract("person.missing", "SomeDefaultValue")).isEqualTo("SomeDefaultValue");
    assertThat(node.extract("person.active")).isEqualTo("true");
    assertThat(node.extract("person.active", false)).isEqualTo(true);
    assertThat(node.extract("person.missing", false)).isEqualTo(false);

    assertThat(node.extract("address.size", -1)).isEqualTo(42);
    assertThat(node.extract("address.junk", -1L)).isEqualTo(99L);
    assertThat(node.extract("address.notSize", -1)).isEqualTo(-1);
    assertThat(node.extract("address.notJunk", -1L)).isEqualTo(-1L);
    assertThat(node.extract("address.notJunk", -1.9D)).isEqualTo(-1.9D);
    assertThat(node.extract("address.size", BigDecimal.TEN)).isEqualTo(42);
    assertThat(node.extract("address.notJunk", BigDecimal.TEN)).isEqualTo(BigDecimal.TEN);

    assertThat(node.extract("address.other.deep")).isEqualTo("one");
  }

  @Test
  void copy() {
    final JsonObject source = JsonObject.create()
      .add("name", "foo")
      .add("other", JsonObject.create().add("b", 42));

    JsonObject copy = source.copy();
    assertThat(copy.toString()).isEqualTo(source.toString());

    copy.add("canMutate", true);
    assertThat(copy.containsKey("canMutate")).isTrue();
    assertThat(source.containsKey("canMutate")).isFalse();
  }

  @Test
  void unmodifiable() {
    final JsonObject source = JsonObject.create()
      .add("name", "foo")
      .add("other", JsonObject.create().add("b", 42));

    JsonObject copy = source.unmodifiable();
    assertThat(copy.toString()).isEqualTo(source.toString());

    assertThatThrownBy(() -> copy.add("canMutate", true))
      .isInstanceOf(UnsupportedOperationException.class);
  }
}
