package io.avaje.json.stream.core;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonDataException;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.json.core.CoreTypes;
import io.avaje.json.stream.JsonStream;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ArrayTest {

  final JsonStream adapter = CoreJsonStream.builder().serializeNulls(true).serializeEmpty(true).failOnUnknown(false).build();

  @Test
  void coreTypes_createArray() {
    JsonAdapter<Long[]> arrayOfLong = CoreTypes.createArray(Long.class);
    try (JsonReader reader = adapter.reader("[1,2,3]")) {
      Long[] asLong = arrayOfLong.fromJson(reader);
      assertThat(asLong).hasSize(3);
      assertThat(asLong).contains(1L, 2L, 3L);
    }

    Long[] longArrayVal = {42L, 43L};
    StringWriter writer = new StringWriter();
    try (JsonWriter jsonWriter = adapter.writer(writer)) {
      arrayOfLong.toJson(jsonWriter, longArrayVal);
    }
    assertThat(writer.toString()).isEqualTo("[42,43]");
  }

  @Test
  void coreTypes_createArray2() {
    JsonAdapter<Object> longAdapter = CoreTypes.create(Long.class);
    JsonAdapter<Object> arrayOfLong = CoreTypes.createArray(Long.class, longAdapter);
    try (JsonReader reader = adapter.reader("[1,2,3]")) {
      Object result = arrayOfLong.fromJson(reader);
      Long[] asLong = (Long[]) result;
      assertThat(asLong).hasSize(3);
      assertThat(asLong).contains(1L, 2L, 3L);
    }
  }

  @Test
  void readArrayEmpty() {
    try (JsonReader reader = adapter.reader("[]")) {
      reader.beginArray();
      assertFalse(reader.hasNextElement());
      reader.endArray();
    }
  }

  @Test
  void readArrayEmptyWithWhitespace() {
    try (JsonReader reader = adapter.reader("[ \n \n  ]")) {
      reader.beginArray();
      assertFalse(reader.hasNextElement());
      reader.endArray();
    }
  }

  @Test
  void readArraySingleNumber() {
    try (JsonReader reader = adapter.reader("[42]")) {
      reader.beginArray();
      assertTrue(reader.hasNextElement());
      assertThat(reader.readInt()).isEqualTo(42);
      assertFalse(reader.hasNextElement());
      reader.endArray();
    }
  }

  @Test
  void readArrayMultipleNumber() {
    try (JsonReader reader = adapter.reader("[ 42, 43]")) {
      reader.beginArray();
      assertTrue(reader.hasNextElement());
      assertThat(reader.readInt()).isEqualTo(42);
      assertTrue(reader.hasNextElement());
      assertThat(reader.readInt()).isEqualTo(43);
      assertFalse(reader.hasNextElement());
      reader.endArray();
    }
  }


  @Test
  void readArrayMultipleString() {
    try (JsonReader reader = adapter.reader("[\"zz\",\"xx\",\"yy\"]")) {
      reader.beginArray();
      assertTrue(reader.hasNextElement());
      assertEquals("zz", reader.readString());
      assertTrue(reader.hasNextElement());
      assertEquals("xx", reader.readString());
      assertTrue(reader.hasNextElement());
      assertEquals("yy", reader.readString());
      assertFalse(reader.hasNextElement());
      reader.endArray();
    }
  }

  @Test
  void readMultipleObject() {
    try (JsonReader reader = adapter.reader("[ {\"key\":\"a\"}, {\"key\":\"b\"} ]")) {
      reader.beginArray();
      assertTrue(reader.hasNextElement());
      reader.beginObject();
      assertThat(reader.hasNextField()).isTrue();
      assertThat(reader.nextField()).isEqualTo("key");
      assertThat(reader.readString()).isEqualTo("a");
      reader.endObject();

      assertTrue(reader.hasNextElement());
      reader.beginObject();
      assertThat(reader.hasNextField()).isTrue();
      assertThat(reader.nextField()).isEqualTo("key");
      assertThat(reader.readString()).isEqualTo("b");
      reader.endObject();

      assertFalse(reader.hasNextElement());
      reader.endArray();
    }
  }

  @Test
  void missingCommasAsString() {
    try (JsonReader reader = adapter.reader("[ \"a\" \"b\"]")) {
      reader.beginArray();
      assertTrue(reader.hasNextElement());
      assertThat(reader.readString()).isEqualTo("a");
      String message = assertThrows(JsonDataException.class, reader::hasNextElement).getMessage();
      assertThat(message).contains("Expecting ']' or ',' for end of array element, instead found '\"'");
    }
  }

  @Test
  void missingCommasAsObject() {
    try (JsonReader reader = adapter.reader("[ {\"key\":\"a\"} {\"key\":\"b\"} ]")) {
      reader.beginArray();
      assertTrue(reader.hasNextElement());
      reader.beginObject();
      assertThat(reader.hasNextField()).isTrue();
      assertThat(reader.nextField()).isEqualTo("key");
      assertThat(reader.readString()).isEqualTo("a");
      reader.endObject();

      String message = assertThrows(JsonDataException.class, reader::hasNextElement).getMessage();
      assertThat(message).contains("Expecting ']' or ',' for end of array element, instead found '{'");
    }
  }

  @Test
  void missingCommasAsNumber_expect_errorParsingNumber() {
    try (JsonReader reader = adapter.reader("[ 42 43]")) {
      reader.beginArray();
      assertTrue(reader.hasNextElement());
      String message = assertThrows(JsonDataException.class, reader::readInt).getMessage();
      assertThat(message).contains("Error parsing number");
    }
  }

  @Test
  void extraCommasAsNumber_expect_errorParsingNumber() {
    try (JsonReader reader = adapter.reader("[ 42, 43, ]")) {
      reader.beginArray();
      assertTrue(reader.hasNextElement());
      assertThat(reader.readInt()).isEqualTo(42);
      assertTrue(reader.hasNextElement());
      assertThat(reader.readInt()).isEqualTo(43);
      assertTrue(reader.hasNextElement());
      String message = assertThrows(JsonDataException.class, reader::readInt).getMessage();
      assertThat(message).contains("Error parsing number");
    }
  }
}
