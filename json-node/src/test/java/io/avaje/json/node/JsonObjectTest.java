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
    Map<String, JsonNode> map = new LinkedHashMap<>();
    map.put("name", JsonString.of("foo"));
    map.put("other", JsonInteger.of(42));

    JsonObject immutableJsonObject = JsonObject.of(map);
    assertThat(immutableJsonObject.elements()).containsOnlyKeys("name", "other");
  }

  @Test
  void isEqualTo() {
    var other = JsonObject.create()
      .add("name", JsonString.of("foo"))
      .add("other", JsonInteger.of(42));

    assertThat(basicObject).isEqualTo(other);
    assertThat(basicObject).isNotEqualTo(JsonString.of("NotFoo"));
    assertThat(basicObject).isNotEqualTo(JsonBoolean.of(false));
  }

  @Test
  void isEqualTo_expect_false() {
    var other = JsonObject.create()
      .add("name", JsonString.of("foo"));

    assertThat(basicObject).isNotEqualTo(other);
    assertThat(basicObject).isNotEqualTo(JsonBoolean.of(false));
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
  void remove() {
    var obj = JsonObject.create().add("one", 1).add("two", 2).add("three", 3);
    assertThat(obj.elements().keySet()).containsExactly("one", "two", "three");

    JsonNode two = obj.remove("two");
    assertThat(two).isNotNull();
    assertThat(two).isInstanceOf(JsonInteger.class);
    assertThat(two.text()).isEqualTo("2");

    assertThat(obj.elements().keySet()).containsExactly("one", "three");
    assertThat(obj.toString()).isEqualTo("{one=1, three=3}");
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

    assertThat(node.extractOrEmpty("person.missing")).isEmpty();
    assertThat(node.extractOrEmpty("person.name")).isNotEmpty().asString().contains("myName");

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

  @Test
  void toPlain() {
    final var source = JsonObject.create()
      .add("name", "foo")
      .add("other", JsonObject.create().add("b", 42));

    Map<String, Object> plainMap = source.toPlain();
    assertThat(plainMap).containsOnlyKeys("name", "other");
    assertThat(plainMap.get("name")).isEqualTo("foo");
    assertThat(plainMap.get("other")).isEqualTo(Map.of("b", 42));
  }

  @Test
  void nullValuesInMap() {
    String s = "{\"a\":1,\"b\":null,\"c\":null,\"d\":4}";
    JsonNodeMapper mapper = JsonNodeMapper.builder().build();
    JsonObject jsonObject = mapper.fromJsonObject(s);

    assertThat(jsonObject.elements().keySet()).containsExactly("a", "b", "c", "d");
  }

  @Test
  void extractNumbers() {
    String s = "{\"aInt\":7,\"aDouble\":23.5,\"text\":\"foo\",\"bool\":true,\"aNull\":null}";
    JsonNodeMapper mapper = JsonNodeMapper.builder().build();
    JsonObject jsonObject = mapper.fromJsonObject(s);

    // Number becomes a Long if it can or otherwise a Double
    assertThat(jsonObject.extract("aInt", BigDecimal.TEN)).isEqualTo(7L);
    assertThat(jsonObject.extract("aDouble", BigDecimal.TEN)).isEqualTo(23.5D);

    assertThat(jsonObject.extract("aInt", 0)).isEqualTo(7);
    assertThat(jsonObject.extract("aInt", 0L)).isEqualTo(7L);
    assertThat(jsonObject.extract("aInt", 0D)).isEqualTo(7D);

    assertThat(jsonObject.extract("aDouble", 0)).isEqualTo(23);
    assertThat(jsonObject.extract("aDouble", 0L)).isEqualTo(23L);
    assertThat(jsonObject.extract("aDouble", 0D)).isEqualTo(23.5D);

    assertThat(jsonObject.extract("doesNotExist", 3)).isEqualTo(3);
    assertThat(jsonObject.extract("doesNotExist", 3L)).isEqualTo(3L);
    assertThat(jsonObject.extract("doesNotExist", 3.5D)).isEqualTo(3.5D);

    assertThat(jsonObject.extract("text", 3)).isEqualTo(3);
    assertThat(jsonObject.extract("text", 3L)).isEqualTo(3L);
    assertThat(jsonObject.extract("text", 3.5D)).isEqualTo(3.5D);

    assertThat(jsonObject.extract("bool", 3)).isEqualTo(3);
    assertThat(jsonObject.extract("bool", 3L)).isEqualTo(3L);
    assertThat(jsonObject.extract("bool", 3.5D)).isEqualTo(3.5D);

    assertThat(jsonObject.extract("aNull", 3)).isEqualTo(3);
    assertThat(jsonObject.extract("aNull", 3L)).isEqualTo(3L);
    assertThat(jsonObject.extract("aNull", 3.5D)).isEqualTo(3.5D);
  }
}
