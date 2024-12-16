package io.avaje.json.node;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.json.simple.SimpleMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class CustomAdapterTest {

  static final JsonNodeMapper mapper = JsonNodeMapper.builder().build();
  static final MyAdapter myAdapter = new MyAdapter(mapper);
  static final SimpleMapper.Type<MyCustomType> typeMapper = mapper.mapper(myAdapter);

  @Test
  void mapUsingCustomAdapter() {
    MyCustomType source = new MyCustomType();
    source.foo = "hi";
    source.bar = 42;
    String asJson = typeMapper.toJson(source);

    MyCustomType fromJson = typeMapper.fromJson(asJson);

    assertThat(fromJson.foo).isEqualTo(source.foo);
    assertThat(fromJson.bar).isEqualTo(source.bar);
  }

  @Test
  void list() {
    SimpleMapper.Type<List<MyCustomType>> listType = typeMapper.list();

    var v0 = as("a", 1);
    var v1 = as("b", 2);
    var list = List.of(v0, v1);

    String asJson = listType.toJson(list);

    List<MyCustomType> fromJson = listType.fromJson(asJson);
    assertThat(fromJson).isEqualTo(list);
  }

  @Test
  void map() {
    SimpleMapper.Type<Map<String, MyCustomType>> mapType = typeMapper.map();

    var v0 = as("a", 1);
    var v1 = as("b", 2);
    Map<String, MyCustomType> map = Map.of("one", v0, "two", v1);

    String asJson = mapType.toJson(map);

    var fromJson = mapType.fromJson(asJson);
    assertThat(fromJson).isEqualTo(map);
  }

  private MyCustomType as(String foo, int bar) {
    MyCustomType customType = new MyCustomType();
    customType.foo = foo;
    customType.bar = bar;
    return customType;
  }

  static class MyCustomType {
    public String foo;
    public int bar;

    @Override
    public boolean equals(Object object) {
      if (this == object) return true;
      if (!(object instanceof MyCustomType)) return false;
      MyCustomType that = (MyCustomType) object;
      return bar == that.bar && Objects.equals(foo, that.foo);
    }

    @Override
    public int hashCode() {
      return Objects.hash(foo, bar);
    }
  }

  static class MyAdapter implements JsonAdapter<MyCustomType> {

    final SimpleMapper.Type<JsonObject> objectMapper;

    public MyAdapter(JsonNodeMapper mapper) {
      this.objectMapper = mapper.objectMapper();
    }

    @Override
    public void toJson(JsonWriter writer, MyCustomType value) {
      var jsonObject = JsonObject.create()
        .add("foo", value.foo)
        .add("bar", value.bar);
      objectMapper.toJson(jsonObject, writer);
    }

    @Override
    public MyCustomType fromJson(JsonReader reader) {
      JsonObject jsonObject = objectMapper.fromJson(reader);

      MyCustomType myCustomType = new MyCustomType();
      myCustomType.foo = jsonObject.extract("foo");
      myCustomType.bar = jsonObject.extract("bar", 0);
      return myCustomType;
    }
  }
}
