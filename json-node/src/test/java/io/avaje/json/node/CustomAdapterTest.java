package io.avaje.json.node;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomAdapterTest {

  static final JsonNodeMapper mapper = JsonNodeMapper.builder().build();

  @Test
  void mapUsingCustomAdapter() {

    MyAdapter myAdapter = new MyAdapter(mapper);

    NodeMapper<MyCustomType> typeMapper = mapper.mapper(myAdapter);

    MyCustomType source = new MyCustomType();
    source.foo = "hi";
    source.bar = 42;
    String asJson = typeMapper.toJson(source);

    MyCustomType fromJson = typeMapper.fromJson(asJson);

    assertThat(fromJson.foo).isEqualTo(source.foo);
    assertThat(fromJson.bar).isEqualTo(source.bar);
  }

  static class MyCustomType {
    public String foo;
    public int bar;
  }

  static class MyAdapter implements JsonAdapter<MyCustomType> {

    final NodeMapper<JsonObject> objectMapper;

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
