package io.avaje.jsonb.core;

import io.avaje.json.JsonDataException;
import io.avaje.json.JsonReader;
import io.avaje.json.stream.JsonStream;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaxNumberDigitsTest {

  static final JsonStream stream = JsonStream.builder().build();

  static final String LARGE_NUMBER =
      "1234567890123456789012345678901234567890123456789012345678901234567890"
          + "1234567890123456789012345678901234567890123456789012345678901234567890"
          + "1234567890123456789012345678901234567890123456789012345678901234567890"
          + "1234567890123456789012345678901234567890123456789012345678901234567890"
          + "12345678901234567890123456789012345678901234567890";

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

  @Test
  void largeNumber_usesJsonbBuilderLimit() {
    var jsonb = Jsonb.builder().parserMaxNumberDigits(500).build();

    assertThat(jsonb.type(BigInteger.class).fromJson(LARGE_NUMBER))
      .isEqualTo(new BigInteger(LARGE_NUMBER));
  }

  @Test
  void largeString_usesJsonbBuilderLimit() {
    var jsonb = Jsonb.builder().parserMaxStringBuffer(8192).build();
    String value = "x".repeat(5000);

    assertThat(jsonb.type(String.class).fromJson("\"" + value + "\"")).isEqualTo(value);
  }
}
