package io.avaje.jsonb.core;

import io.avaje.json.JsonDataException;
import io.avaje.json.JsonReader;
import io.avaje.json.stream.JsonStream;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaxNumberDigitsTest {

  static final JsonStream stream = JsonStream.builder().build();

  static final String LARGE_NUMBER =
    "1.0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001";

  @Test
  void largeNumber_exceedsDefaultLimit_throws() {
    assertThatThrownBy(() -> {
      try (JsonReader reader = stream.reader(LARGE_NUMBER)) {
        reader.readDouble();
      }
    })
      .isInstanceOf(JsonDataException.class)
      .hasMessageContaining("Too many digits");
  }
}
