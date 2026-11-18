package io.avaje.json.stream.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import io.avaje.json.JsonReader;
import io.avaje.json.stream.JsonStream;

class JParserKnownStringTest {

  final JsonStream stream = JsonStream.builder().build();
  // "glbvs" and "yacxa" have the same FNV-1a hash
  final JsonNames values = JsonNames.of("one", "glbvs", "yacxa", "café", "b\"c");

  @Test
  void position() {
    JsonNames names = JsonNames.of("a", "b", "glbvs", "yacxa");
    assertThat(names.position(JsonReader.nameHash("a"))).isEqualTo(0);
    assertThat(names.position(JsonReader.nameHash("b"))).isEqualTo(1);
    assertThat(names.position(JsonReader.nameHash("glbvs"))).isEqualTo(-1);
    assertThat(names.position(JsonReader.nameHash("c"))).isEqualTo(-1);
    assertThat(names.name(3)).isEqualTo("yacxa");
  }

  @Test
  void position_manyNames() {
    String[] all = new String[300];
    for (int i = 0; i < all.length; i++) {
      all[i] = "n" + i;
    }
    JsonNames names = JsonNames.of(all);
    for (int i = 0; i < all.length; i++) {
      assertThat(names.position(JsonReader.nameHash(all[i]))).isEqualTo(i);
    }
    assertThat(names.position(JsonReader.nameHash("n300"))).isEqualTo(-1);
    assertThat(JsonNames.EMPTY.position(JsonReader.nameHash("x"))).isEqualTo(-1);
  }

  @Test
  void readString_matched_returnsSameInstance() {
    try (JsonReader reader = stream.reader("[\"one\", \"café\",\"one\"]")) {
      reader.beginArray();
      assertThat(reader.hasNextElement()).isTrue();
      assertThat(reader.readString(values)).isSameAs(values.name(0));
      assertThat(reader.hasNextElement()).isTrue();
      assertThat(reader.readString(values)).isSameAs(values.name(3));
      assertThat(reader.hasNextElement()).isTrue();
      assertThat(reader.readString(values)).isSameAs(values.name(0));
      assertThat(reader.hasNextElement()).isFalse();
      reader.endArray();
    }
  }

  @Test
  void readString_unknownCollidingEscaped() {
    try (JsonReader reader = stream.reader("[\"two\",\"glbvs\",\"yacxa\",\"b\\\"c\",\"on\\u0065\",\"\"]")) {
      reader.beginArray();
      String[] expected = {"two", "glbvs", "yacxa", "b\"c", "one", ""};
      for (String value : expected) {
        assertThat(reader.hasNextElement()).isTrue();
        assertThat(reader.readString(values)).isEqualTo(value);
      }
      assertThat(reader.hasNextElement()).isFalse();
      reader.endArray();
    }
  }

  @Test
  void readString_hashCollisionWithSingleValue_notMatched() {
    JsonNames single = JsonNames.of("glbvs");
    try (JsonReader reader = stream.reader("[\"yacxa\"]")) {
      reader.beginArray();
      assertThat(reader.hasNextElement()).isTrue();
      assertThat(reader.readString(single)).isEqualTo("yacxa");
      assertThat(reader.hasNextElement()).isFalse();
      reader.endArray();
    }
  }

  @Test
  void readString_stream_valueCrossesBufferEnd() {
    for (int pad = 0; pad < 150; pad++) {
      String content = "[\"" + "p".repeat(pad) + "\",\"one\",\"café\",\"glbvs\",\"one\"]";
      JsonReadAdapter reader = streamReader(content);

      reader.beginArray();
      assertThat(reader.hasNextElement()).isTrue();
      assertThat(reader.readString(values)).isEqualTo("p".repeat(pad));
      for (String value : new String[]{"one", "café", "glbvs", "one"}) {
        assertThat(reader.hasNextElement()).isTrue();
        assertThat(reader.readString(values)).as("pad %d", pad).isEqualTo(value);
      }
      assertThat(reader.hasNextElement()).isFalse();
      reader.endArray();
    }
  }

  @Test
  void hasNextElement_stream_stringEndsAtBufferEnd() {
    for (int pad = 0; pad < 150; pad++) {
      String content = "[\"" + "p".repeat(pad) + "\",\"a\",\"bc\",\"d\"]";
      JsonReadAdapter reader = streamReader(content);
      reader.beginArray();
      for (String value : new String[]{"p".repeat(pad), "a", "bc", "d"}) {
        assertThat(reader.hasNextElement()).as("pad %d", pad).isTrue();
        assertThat(reader.readString()).as("pad %d", pad).isEqualTo(value);
      }
      assertThat(reader.hasNextElement()).isFalse();
      reader.endArray();
    }
  }

  @Test
  void hasNextStreamElement_stream_elementEndsAtBufferEnd() {
    for (int pad = 0; pad < 150; pad++) {
      String[] values = {"p".repeat(pad), "a", "bc"};
      String content = "{\"k\":\"" + values[0] + "\"}\n{\"k\":\"a\"}\n{\"k\":\"bc\"}";
      JsonReadAdapter reader = streamReader(content);
      reader.beginStream();
      for (String value : values) {
        assertThat(reader.hasNextStreamElement()).as("pad %d", pad).isTrue();
        reader.beginObject();
        assertThat(reader.hasNextField()).isTrue();
        assertThat(reader.nextField()).isEqualTo("k");
        assertThat(reader.readString()).as("pad %d", pad).isEqualTo(value);
        assertThat(reader.hasNextField()).isFalse();
        reader.endObject();
      }
      assertThat(reader.hasNextStreamElement()).isFalse();
      reader.endStream();
    }
  }

  private static JsonReadAdapter streamReader(String content) {
    JParser parser = new JParser(new char[100], new byte[100], 100, JParser.DoublePrecision.HIGH,
      JParser.UnknownNumberParsing.BIGDECIMAL, 100, 1000);
    parser.process(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    return new JsonReadAdapter(parser, Recyclers.ThreadLocalPool.shared(), true, true);
  }

  @Test
  void writeValue() {
    var names = stream.properties("one", "b\"c", "café");
    try (var writer = stream.bufferedWriter()) {
      writer.beginObject(names);
      writer.name(0);
      writer.value(names, 1);
      writer.name(2);
      writer.beginArray();
      writer.value(names, 0);
      writer.value(names, 2);
      writer.endArray();
      writer.endObject();
      assertThat(writer.result()).isEqualTo("{\"one\":\"b\\\"c\",\"café\":[\"one\",\"café\"]}");
    }
  }

  @Test
  void writeValue_pretty() {
    var names = stream.properties("one", "two");
    try (var writer = stream.bufferedWriter()) {
      writer.pretty(true);
      writer.beginArray();
      writer.value(names, 0);
      writer.value(names, 1);
      writer.endArray();
      assertThat(writer.result()).isEqualTo(prettyStrings());
    }
  }

  private String prettyStrings() {
    try (var writer = stream.bufferedWriter()) {
      writer.pretty(true);
      writer.beginArray();
      writer.value("one");
      writer.value("two");
      writer.endArray();
      return writer.result();
    }
  }
}
