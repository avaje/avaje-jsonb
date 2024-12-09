package org.example;

import io.avaje.jsonb.Json;
import io.avaje.json.JsonReader;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class StreamBasicTest {

  Jsonb jsonb = Jsonb.builder()
    .add(MyBasic.class, MyBasicJsonAdapter::new)
    .build();

  JsonType<MyBasic> type = jsonb.type(MyBasic.class);

  @Json
  public static class MyBasic {
    public final int id;
    public final String name;

    public MyBasic(int id, String name) {
      this.id = id;
      this.name = name;
    }

    @Override
    public String toString() {
      return "MyBasic[id=" + id + ", name=" + name + "]";
    }
  }

  private List<MyBasic> basicList() {
    List<MyBasic> basics = new ArrayList<>();
    basics.add(new MyBasic(1,"a"));
    basics.add(new MyBasic(2,"b"));
    basics.add(new MyBasic(3,"c"));
    return basics;
  }

  @Test
  void stream_toJson() {
    String asJson = type.stream().toJson(basicList().stream());
    assertThat(asJson).isEqualTo("[{\"id\":1,\"name\":\"a\"},{\"id\":2,\"name\":\"b\"},{\"id\":3,\"name\":\"c\"}]");
  }

  @Test
  void stream_traditionalArray() {
    String arrayJson = jsonb.toJson(basicList());
    StringBuilder sb = new StringBuilder();

    try (JsonReader reader = jsonb.reader(arrayJson)) {
      Stream<MyBasic> asStream = type.stream(reader.streamArray(true));
      asStream.forEach(sb::append);
    }

    assertThat(sb.toString()).isEqualTo("MyBasic[id=1, name=a]MyBasic[id=2, name=b]MyBasic[id=3, name=c]");

    // same test but using - JsonType<Stream<T>>
    JsonType<Stream<MyBasic>> streamJsonType = type.stream();

    sb = new StringBuilder();
    streamJsonType
      .fromJson(arrayJson)
      .forEach(sb::append);

    assertThat(sb.toString()).isEqualTo("MyBasic[id=1, name=a]MyBasic[id=2, name=b]MyBasic[id=3, name=c]");
  }

  @Test
  void stream_newLineDelimited() {
    String arrayJson = jsonb.toJson(basicList());
    String newLineDelimitedJson = arrayJson.replaceAll("[\\[|\\]]", "").replace("},{", "}\n{");

    StringBuilder sb = new StringBuilder();
    try (JsonReader reader = jsonb.reader(newLineDelimitedJson)) {
      Stream<MyBasic> asStream = type.stream(reader.streamArray(false));
      asStream.forEach(sb::append);
    }

    assertThat(sb.toString()).isEqualTo("MyBasic[id=1, name=a]MyBasic[id=2, name=b]MyBasic[id=3, name=c]");

    // same test but using - JsonType<Stream<T>>
    JsonType<Stream<MyBasic>> streamJsonType = type.stream();

    sb = new StringBuilder();
    try (Stream<MyBasic> myBasicStream = streamJsonType.fromJson(newLineDelimitedJson)) {
      myBasicStream.forEach(sb::append);
      assertThat(sb.toString()).isEqualTo("MyBasic[id=1, name=a]MyBasic[id=2, name=b]MyBasic[id=3, name=c]");
    }
  }

  @Test
  void stream_spaceDelimited() {
    String arrayJson = "\n" + jsonb.toJson(basicList()) + "\n";
    String spaceDelimitedJson = arrayJson.replaceAll("[\\[|\\]]", "").replace("},{", "} {");

    StringBuilder sb = new StringBuilder();
    try (JsonReader reader = jsonb.reader(spaceDelimitedJson)) {
      type.stream(reader).forEach(sb::append);
    }

    assertThat(sb.toString()).isEqualTo("MyBasic[id=1, name=a]MyBasic[id=2, name=b]MyBasic[id=3, name=c]");
  }

}
