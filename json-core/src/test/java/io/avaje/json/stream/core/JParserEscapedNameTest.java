package io.avaje.json.stream.core;

import io.avaje.json.JsonReader;
import io.avaje.json.PropertyNames;
import io.avaje.json.stream.JsonStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class JParserEscapedNameTest {

  final JsonStream stream = JsonStream.builder().build();
  final PropertyNames names = stream.properties("anb", "name");

  @Test
  void escapedName_doesNotMatchKnownNameWithSameBytesLessBackslash() {
    String json = "{\"a\\nb\":1,\"n\\u0061me\":2}";
    assertNames(stream.reader(json));
    assertNames(stream.reader(json.getBytes(StandardCharsets.UTF_8)));
    assertNames(stream.reader(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))));
  }

  private void assertNames(JsonReader reader) {
    try (reader) {
      reader.beginObject(names);
      assertThat(reader.hasNextField()).isTrue();
      assertThat(reader.nextField()).isEqualTo("a\nb");
      assertThat(reader.readInt()).isEqualTo(1);
      assertThat(reader.hasNextField()).isTrue();
      assertThat(reader.nextField()).isEqualTo("name");
      assertThat(reader.readInt()).isEqualTo(2);
      assertThat(reader.hasNextField()).isFalse();
      reader.endObject();
    }
  }
}
