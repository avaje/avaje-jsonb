package io.avaje.json.stream;

import io.avaje.json.JsonReader;
import io.avaje.json.PropertyNames;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonStreamTest {

  static final String input = "{\"key0\":\"val0\", \"key1\": 42, \"key2\": true, \"key3\":\"val3\"}";

  static final JsonStream stream = JsonStream.builder().build();
  // PropertyNames names = stream.properties("key0", "key1", "key2", "key3");
  static final PropertyNames names = stream.properties("key0", "key2");

  @Test
  void readKnownProperties() {

    String key0 = null;
    Long key1 = null;
    Boolean key2 = null;
    String key3 = null;

    try (JsonReader reader = stream.reader(input)) {
      // can use known property names for performance
      reader.beginObject(names);
      while (reader.hasNextField()) {
        final String fieldName = reader.nextField();
        switch (fieldName) {
          case "key0":
            key0 = reader.readString();
            break;
          case "key1":
            key1 = reader.readLong();
            break;
          case "key2":
            key2 = reader.readBoolean();
            break;
          case "key3":
            key3 = reader.readString();
            break;
          default:
            reader.unmappedField(fieldName);
            reader.skipValue();
        }
      }
      reader.endObject();
    }

    assertThat(key0).isEqualTo("val0");
    assertThat(key1).isEqualTo(42L);
    assertThat(key2).isEqualTo(true);
    assertThat(key3).isEqualTo("val3");
  }

  @Test
  void readUnknown() {

  }

  static final String LARGE_NUMBER = "1.0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001";

  @Test
  void maxNumberDigits_systemProperty() {
    // surefire sets jsonb.maxNumberDigits=200, so the large number parses cleanly
    try (JsonReader reader = stream.reader(LARGE_NUMBER)) {
      assertThat(reader.readDouble()).isEqualTo(1.0);
    }
  }

  @Test
  void parserLimits_applyPerStreamAndDoNotLeakThroughRecycler() {
    var defaults = new ParserLimits(4, 4096, 1000);
    var stream = JsonStream.builder()
      .bufferRecycling(BufferRecycleStrategy.THREAD_LOCAL)
      .parserLimits(defaults)
      .build();
    var largerStream = JsonStream.builder()
      .bufferRecycling(BufferRecycleStrategy.THREAD_LOCAL)
      .parserLimits(new ParserLimits(25, 8192, 8192))
      .build();

    String number = "123456789012345678901";
    try (JsonReader reader = stream.reader(number)) {
      assertThatThrownBy(reader::readDecimal).hasMessageContaining("Maximum allowed is 4");
    }

    try (JsonReader reader = largerStream.reader(number)) {
      assertThat(reader.readDecimal()).isEqualTo(new BigDecimal(number));
    }

    try (JsonReader reader = stream.reader(number)) {
      assertThatThrownBy(reader::readDecimal).hasMessageContaining("Maximum allowed is 4");
    }

    String longString = "{\"value\":\"" + "x".repeat(5000) + "\"}";
    try (JsonReader reader = stream.reader(longString)) {
      reader.beginObject();
      assertThat(reader.hasNextField()).isTrue();
      assertThat(reader.nextField()).isEqualTo("value");
      assertThatThrownBy(reader::readString).hasMessageContaining("Maximum string buffer limit exceeded");
    }
    try (JsonReader reader = largerStream.reader(longString)) {
      reader.beginObject();
      assertThat(reader.hasNextField()).isTrue();
      assertThat(reader.nextField()).isEqualTo("value");
      assertThat(reader.readString()).hasSize(5000);
    }
    try (JsonReader reader = stream.reader(longString)) {
      reader.beginObject();
      assertThat(reader.hasNextField()).isTrue();
      assertThat(reader.nextField()).isEqualTo("value");
      assertThatThrownBy(reader::readString).hasMessageContaining("Maximum string buffer limit exceeded");
    }

    String longRaw = "{\"value\":\"" + "x".repeat(5000) + "\"}";
    try (JsonReader reader = stream.reader(new ByteArrayInputStream(longRaw.getBytes(StandardCharsets.UTF_8)))) {
      reader.beginObject();
      assertThat(reader.hasNextField()).isTrue();
      assertThat(reader.nextField()).isEqualTo("value");
      assertThatThrownBy(reader::readRaw).hasMessageContaining("Maximum buffer limit exceeded for raw content");
    }
    try (JsonReader reader = largerStream.reader(new ByteArrayInputStream(longRaw.getBytes(StandardCharsets.UTF_8)))) {
      reader.beginObject();
      assertThat(reader.hasNextField()).isTrue();
      assertThat(reader.nextField()).isEqualTo("value");
      assertThat(reader.readRaw()).hasSize(5002);
    }
  }

}
