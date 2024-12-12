package io.avaje.json.simple;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CustomAdapterTest {

  static final SimpleMapper simpleMapper = SimpleMapper.builder().build();

  @Test
  void mapUsingCustomAdapter() {

    MyAdapter myAdapter = new MyAdapter(simpleMapper);

    SimpleMapper.Type<MyCustomType> type = simpleMapper.type(myAdapter);

    MyCustomType source = new MyCustomType();
    source.foo = "hi";
    source.bar = 42;
    String asJson = type.toJson(source);

    MyCustomType fromJson = type.fromJson(asJson);

    assertThat(fromJson.foo).isEqualTo(source.foo);
    assertThat(fromJson.bar).isEqualTo(source.bar);
  }

  static class MyCustomType {
    public String foo;
    public int bar;
  }

  static class MyAdapter implements JsonAdapter<MyCustomType> {

    private final SimpleMapper.Type<Map<String, Object>> map;

    public MyAdapter(SimpleMapper simpleMapper) {
      this.map = simpleMapper.map();
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
