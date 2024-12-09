package org.example.customer.stream;

import io.avaje.json.*;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class StreamBasicTest {

  Jsonb jsonb = Jsonb.builder().build();
  JsonType<MyBasic> type = jsonb.type(MyBasic.class);
  List<MyBasic> basics = List.of(new MyBasic(1, "a"), new MyBasic(2, "b"), new MyBasic(3, "c"));

  @Test
  void stream_toJson() {
    String asJson = type.stream().toJson(basics.stream());
    assertThat(asJson).isEqualTo("[{\"id\":1,\"name\":\"a\"},{\"id\":2,\"name\":\"b\"},{\"id\":3,\"name\":\"c\"}]");
  }

  @Test
  void stream_traditionalArray() {
    String arrayJson = jsonb.toJson(basics);
    StringBuilder sb = new StringBuilder();

    try (JsonReader reader = jsonb.reader(arrayJson)) {
      try (Stream<MyBasic> asStream = type.stream(reader)) {
        asStream.forEach(sb::append);
      }
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
    String arrayJson = jsonb.toJson(basics);
    String newLineDelimitedJson = arrayJson.replaceAll("[\\[|\\]]", "").replace("},{", "}\n{");

    StringBuilder sb = new StringBuilder();
    try (JsonReader reader = jsonb.reader(newLineDelimitedJson)) {
      Stream<MyBasic> asStream = type.stream(reader);
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
    String arrayJson = "\n" + jsonb.toJson(basics) + "\n";
    String spaceDelimitedJson = arrayJson.replaceAll("[\\[|\\]]", "").replace("},{", "} {");

    StringBuilder sb = new StringBuilder();
    try (JsonReader reader = jsonb.reader(spaceDelimitedJson)) {
      type.stream(reader).forEach(sb::append);
    }

    assertThat(sb.toString()).isEqualTo("MyBasic[id=1, name=a]MyBasic[id=2, name=b]MyBasic[id=3, name=c]");
  }

}
