package io.avaje.json.stream;

import io.avaje.json.JsonReader;
import io.avaje.json.PropertyNames;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

}
