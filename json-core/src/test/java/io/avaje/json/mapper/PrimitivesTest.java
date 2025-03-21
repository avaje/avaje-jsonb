package io.avaje.json.mapper;

import io.avaje.json.JsonDataException;
import io.avaje.json.JsonReader;
import io.avaje.json.stream.JsonStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PrimitivesTest {

  static final JsonStream defaultJsonStream = JsonStream.builder().build();
  static final JsonStream strictJsonStream = JsonStream.builder().failOnNullPrimitives(true).build();

  @Test
  void booleanTest() {
    String input = "{\"a\":true, \"b\": false}";

    try (JsonReader reader = defaultJsonStream.reader(input)) {
      reader.beginObject();
      assertTrue(reader.hasNextField());
      assertEquals("a", reader.nextField());
      assertTrue(reader.readBoolean());
      assertTrue(reader.hasNextField());
      assertEquals("b", reader.nextField());
      assertFalse(reader.readBoolean());
      reader.endObject();
    }
  }

  @Test
  void booleanNullTest() {
    String input = "{\"a\":true, \"b\": null, \"c\": 7}";

    try (JsonReader reader = defaultJsonStream.reader(input)) {
      reader.beginObject();
      assertTrue(reader.hasNextField());
      assertEquals("a", reader.nextField());
      assertTrue(reader.readBoolean());
      assertTrue(reader.hasNextField());
      assertEquals("b", reader.nextField());
      assertFalse(reader.readBoolean());
      assertTrue(reader.hasNextField());
      assertEquals("c", reader.nextField());
      assertEquals(7, reader.readInt());
      reader.endObject();
    }
  }

  @Test
  void intNullTest() {
    String input = "{\"a\":3, \"b\": null, \"c\": 7}";

    try (JsonReader reader = defaultJsonStream.reader(input)) {
      reader.beginObject();
      assertTrue(reader.hasNextField());
      assertEquals("a", reader.nextField());
      assertEquals(3, reader.readInt());
      assertTrue(reader.hasNextField());
      assertEquals("b", reader.nextField());
      assertEquals(0, reader.readInt());
      assertTrue(reader.hasNextField());
      assertEquals("c", reader.nextField());
      assertEquals(7, reader.readInt());
      reader.endObject();
    }
  }

  @Test
  void longNullTest() {
    String input = "{\"a\":3, \"b\": null, \"c\": 7}";

    try (JsonReader reader = defaultJsonStream.reader(input)) {
      reader.beginObject();
      assertTrue(reader.hasNextField());
      assertEquals("a", reader.nextField());
      assertEquals(3, reader.readLong());
      assertTrue(reader.hasNextField());
      assertEquals("b", reader.nextField());
      assertEquals(0, reader.readLong());
      assertTrue(reader.hasNextField());
      assertEquals("c", reader.nextField());
      assertEquals(7, reader.readLong());
      reader.endObject();
    }
  }

  @Test
  void doubleNullTest() {
    String input = "{\"a\":3, \"b\": null, \"c\": 7}";

    try (JsonReader reader = defaultJsonStream.reader(input)) {
      reader.beginObject();
      assertTrue(reader.hasNextField());
      assertEquals("a", reader.nextField());
      assertEquals(3, reader.readLong());
      assertTrue(reader.hasNextField());
      assertEquals("b", reader.nextField());
      assertEquals(0, reader.readDouble());
      assertTrue(reader.hasNextField());
      assertEquals("c", reader.nextField());
      assertEquals(7, reader.readLong());
      reader.endObject();
    }
  }

  @Test
  void readBoolean_strictMode_failOnNull() {
    String input = "{\"a\":true, \"b\": null, \"c\": 7}";

    try (JsonReader reader = strictJsonStream.reader(input)) {
      reader.beginObject();
      assertTrue(reader.hasNextField());
      assertEquals("a", reader.nextField());
      assertTrue(reader.readBoolean());
      assertTrue(reader.hasNextField());
      assertEquals("b", reader.nextField());
      assertThrows(JsonDataException.class, reader::readBoolean);
    }
  }

  @Test
  void readInt_strictMode_failOnNull() {
    String input = "{\"a\":3, \"b\": null, \"c\": 7}";

    try (JsonReader reader = strictJsonStream.reader(input)) {
      reader.beginObject();
      assertTrue(reader.hasNextField());
      assertEquals("a", reader.nextField());
      assertEquals(3, reader.readInt());
      assertTrue(reader.hasNextField());
      assertEquals("b", reader.nextField());
      assertThrows(JsonDataException.class, reader::readInt);
    }
  }

  @Test
  void readLong_strictMode_failOnNull() {
    String input = "{\"a\":3, \"b\": null, \"c\": 7}";

    try (JsonReader reader = strictJsonStream.reader(input)) {
      reader.beginObject();
      assertTrue(reader.hasNextField());
      assertEquals("a", reader.nextField());
      assertEquals(3, reader.readLong());
      assertTrue(reader.hasNextField());
      assertEquals("b", reader.nextField());
      assertThrows(JsonDataException.class, reader::readLong);
    }
  }

  @Test
  void readDouble_strictMode_failOnNull() {
    String input = "{\"a\":3, \"b\": null, \"c\": 7}";

    try (JsonReader reader = strictJsonStream.reader(input)) {
      reader.beginObject();
      assertTrue(reader.hasNextField());
      assertEquals("a", reader.nextField());
      assertEquals(3, reader.readLong());
      assertTrue(reader.hasNextField());
      assertEquals("b", reader.nextField());
      assertThrows(JsonDataException.class, reader::readDouble);
    }
  }
}
