package io.avaje.json.simple;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomAdapter2Test {

  static final SimpleMapper simpleMapper = SimpleMapper.builder().build();

  @Test
  void mapUsingCustomAdapter() {

    MyAdapterUsingRaw myAdapter = new MyAdapterUsingRaw();

    SimpleMapper.Type<MyOtherType> type = simpleMapper.type(myAdapter);

    MyOtherType source = new MyOtherType();
    source.foo = "hi";
    source.bar = 42;
    String asJson = type.toJson(source);

    MyOtherType fromJson = type.fromJson(asJson);

    assertThat(fromJson.foo).isEqualTo(source.foo);
    assertThat(fromJson.bar).isEqualTo(source.bar);
  }

  static class MyOtherType {
    String foo;
    int bar;
  }

  static class MyAdapterUsingRaw implements JsonAdapter<MyOtherType> {

    @Override
    public void toJson(JsonWriter writer, MyOtherType value) {
      writer.beginObject();
      writer.name("foo");
      writer.value(value.foo);
      writer.name("bar");
      writer.value(value.bar);
      writer.endObject();
    }

    @Override
    public MyOtherType fromJson(JsonReader reader) {
      MyOtherType result = new MyOtherType();

      reader.beginObject();

      String key;
      while (reader.hasNextField()) {
        key = reader.nextField();
        switch (key) {
          case "foo":
            result.foo = reader.readString();
            break;
          case "bar":
            result.bar = reader.readInt();
            break;
          default:
            reader.skipValue();
        }
      }
      reader.endObject();
      return result;
    }
  }
}
