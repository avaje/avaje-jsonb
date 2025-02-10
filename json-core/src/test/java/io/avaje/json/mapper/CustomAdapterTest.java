package io.avaje.json.mapper;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.json.stream.JsonStream;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class CustomAdapterTest {

  static final JsonStream jsonStream = JsonStream.builder().build();
  static final JsonMapper mapper = JsonMapper.builder().jsonStream(jsonStream).build();
  static final MyAdapter myAdapter = new MyAdapter(mapper);
  static final JsonMapper.Type<MyCustomType> type = mapper.type(myAdapter);

  @Test
  void mapUsingCustomAdapter() {
    JsonMapper mapper = JsonMapper.builder().build();
    JsonMapper.Type<MyCustomType> myType = mapper.type(MyAdapter::new);

    MyCustomType source = new MyCustomType();
    source.foo = "hi";
    source.bar = 42;
    String asJson = myType.toJson(source);

    MyCustomType fromJson = myType.fromJson(asJson);

    assertThat(fromJson.foo).isEqualTo(source.foo);
    assertThat(fromJson.bar).isEqualTo(source.bar);
  }

  @Test
  void list() {
    JsonMapper.Type<List<MyCustomType>> listType = type.list();

    var v0 = as("a", 1);
    var v1 = as("b", 2);
    var list = List.of(v0, v1);

    String asJson = listType.toJson(list);

    List<MyCustomType> fromJson = listType.fromJson(asJson);
    assertThat(fromJson).isEqualTo(list);
  }

  @Test
  void map() {
    JsonMapper.Type<Map<String, MyCustomType>> mapType = type.map();

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

    private final JsonMapper.Type<Map<String, Object>> map;

    public MyAdapter(JsonMapper mapper) {
      this.map = mapper.map();
    }

    @Override
    public void toJson(JsonWriter writer, MyCustomType value) {
      Map<String, Object> foo = Map.of("foo", value.foo, "bar", value.bar);
      map.toJson(foo, writer);
    }

    @Override
    public MyCustomType fromJson(JsonReader reader) {
      Map<String, Object> mapValue = map.fromJson(reader);
      MyCustomType myCustomType = new MyCustomType();
      myCustomType.foo = (String)mapValue.get("foo");
      Number bar = (Number)mapValue.get("bar");
      myCustomType.bar = bar == null ? 0 : bar.intValue();
      return myCustomType;
    }
  }
}
