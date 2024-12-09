package io.avaje.json.stream.core;

import io.avaje.json.JsonReader;
import io.avaje.json.stream.JsonStream;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StreamTest {

  final JsonStream adapter = JsonStream.builder().serializeNulls(true).serializeEmpty(true).failOnUnknown(false).build();

  @Test
  void readStreamMultipleObjectArrays() {
    readStream("[ {\"key\":\"a\"} , {\"key\":\"b\"} ]");
    readStream("[ {\"key\":\"a\"} \n {\"key\":\"b\"} ]");
  }

  @Test
  void readStreamMultipleObject() {
    readStream("{\"key\":\"a\"},{\"key\":\"b\"}");
    readStream("{\"key\":\"a\"}\n{\"key\":\"b\"}");
    readStream("{\"key\":\"a\"} \n {\"key\":\"b\"}");
  }

  private void readStream(String rawJson) {
    try (JsonReader reader = adapter.reader(rawJson)) {
      reader.beginStream();
      assertTrue(reader.hasNextStreamElement());
      reader.beginObject();
      assertThat(reader.hasNextField()).isTrue();
      assertThat(reader.nextField()).isEqualTo("key");
      assertThat(reader.readString()).isEqualTo("a");
      reader.endObject();

      assertTrue(reader.hasNextStreamElement());
      reader.beginObject();
      assertThat(reader.hasNextField()).isTrue();
      assertThat(reader.nextField()).isEqualTo("key");
      assertThat(reader.readString()).isEqualTo("b");
      reader.endObject();

      assertFalse(reader.hasNextStreamElement());
      reader.endStream();
    }
  }
}
