package io.avaje.json.stream.core;

import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.json.stream.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DieselAdapterTest {

  final JsonStream adapter = CoreJsonStream.builder().serializeNulls(true).serializeEmpty(true).failOnUnknown(false).build();

  @Test
  void readArray() {
    try (JsonReader reader = adapter.reader("{\"a\":\"hi\",\"b\":[\"zz\",\"xx\",\"yy\"], \"c\":\"bye\"}")) {
      reader.beginObject();
      assertTrue(reader.hasNextField());
      assertEquals("a", reader.nextField());
      assertEquals("hi", reader.readString());
      assertTrue(reader.hasNextField());
      assertEquals("b", reader.nextField());
      reader.beginArray();
      assertTrue(reader.hasNextElement());
      assertEquals("zz", reader.readString());
      assertTrue(reader.hasNextElement());
      assertEquals("xx", reader.readString());
      assertTrue(reader.hasNextElement());
      assertEquals("yy", reader.readString());
      assertFalse(reader.hasNextElement());
      assertTrue(reader.hasNextField());
      assertEquals("c", reader.nextField());
      assertEquals("bye", reader.readString());
      assertFalse(reader.hasNextField());
      reader.endObject();
    }
  }

  @Test
  void write_usingBufferedWriter() {
    BufferedJsonWriter jw0 = adapter.bufferedWriter();
    writeHello(jw0, "hello");
    assertThat(jw0.result()).isEqualTo("{\"one\":\"hello\"}");

    BufferedJsonWriter jw1 = adapter.bufferedWriter();
    writeHello(jw1, "hi");
    assertThat(jw1.result()).isEqualTo("{\"one\":\"hi\"}");
  }

  @Test
  void write_to_writer() {
    StringWriter sw = new StringWriter();
    try (JsonWriter jw0 = adapter.writer(sw)) {
      writeHello(jw0, "hello");
    }
    assertThat(sw.toString()).isEqualTo("{\"one\":\"hello\"}");

    StringWriter sw1 = new StringWriter();
    try (JsonWriter jw1 = adapter.writer(sw1)) {
      writeHello(jw1, "hi");
    }
    assertThat(sw1.toString()).isEqualTo("{\"one\":\"hi\"}");
  }

  @Test
  void write_to_OutputStream() {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try (JsonWriter jw0 = adapter.writer(os)) {
      writeHello(jw0, "hello");
    }
    assertThat(os.toString()).isEqualTo("{\"one\":\"hello\"}");

    ByteArrayOutputStream os1 = new ByteArrayOutputStream();
    try (JsonWriter jw1 = adapter.writer(os1)) {
      writeHello(jw1, "hi");
    }
    assertThat(os1.toString()).isEqualTo("{\"one\":\"hi\"}");
  }

  @Test
  void write_markIncomplete_withError() {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      try (JsonWriter jw = adapter.writer(os)) {
        jw.beginObject();
        jw.name("one");
        jw.value("I will be incomplete");
        jw.markIncomplete();
        throwAnError();
      }
    } catch (Exception e) {
      // ignore
    }
    assertThat(os.toString()).isEqualTo("");
  }

  private void throwAnError() {
    throw new IllegalStateException("foo");
  }

  private void writeHello(JsonWriter jw, String message) {
    jw.beginObject();
    jw.name("one");
    jw.value(message);
    jw.endObject();
  }
}
