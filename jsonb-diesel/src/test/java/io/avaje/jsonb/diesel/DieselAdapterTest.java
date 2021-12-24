package io.avaje.jsonb.diesel;

import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.spi.BufferedJsonWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class DieselAdapterTest {

  final DieselAdapter adapter = new DieselAdapter(true, true, false);

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
    assertThat(os.toString(StandardCharsets.UTF_8)).isEqualTo("{\"one\":\"hello\"}");

    ByteArrayOutputStream os1 = new ByteArrayOutputStream();
    try (JsonWriter jw1 = adapter.writer(os1)) {
      writeHello(jw1, "hi");
    }
    assertThat(os1.toString(StandardCharsets.UTF_8)).isEqualTo("{\"one\":\"hi\"}");
  }

  private void writeHello(JsonWriter jw, String message) {
    jw.beginObject();
    jw.name("one");
    jw.value(message);
    jw.endObject();
  }
}
