package io.avaje.jsonb.diesel;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class FooWriterTest {

  @Test
  void basic() {

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    JGenerator dJsonWriter = new JGenerator();
    dJsonWriter.reset(os);

    FooWriter fw = new FooWriter(dJsonWriter, true, true);

    fw.beginArray();
    fw.beginObject();
    fw.name("one");
    fw.value("hello");
    fw.name("size");
    fw.value(43);
    fw.endObject();
    fw.beginObject();
    fw.name("one");
    fw.value("another");
    fw.name("active");
    fw.value(true);
    fw.name("flags");
    fw.beginArray();
    fw.value(42);
    fw.value(43);
    fw.endArray();
    fw.endObject();
    fw.endArray();
    fw.close();

    String asJson = os.toString(StandardCharsets.UTF_8);
    assertThat(asJson).isEqualTo("[{\"one\":\"hello\",\"size\":43},{\"one\":\"another\",\"active\":true,\"flags\":[42,43]}]");
  }


  @Test
  void using_names() {

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    JGenerator dJsonWriter = new JGenerator();
    dJsonWriter.reset(os);

    FooWriter fw = new FooWriter(dJsonWriter, true, true);

    DNames names = DNames.of("one", "size", "active","flags");

    fw.beginArray();

    fw.beginObject();
    fw.names(names);
    fw.name(0);
    fw.value("hello");
    fw.name(1);
    fw.value(43);
    fw.endObject();

    fw.beginObject();
    fw.names(names);
    fw.name(0);
    fw.value("another");
    fw.name(2);
    fw.value(true);
    fw.name(3);
    fw.beginArray();
    fw.value(42);
    fw.value(43);
    fw.endArray();
    fw.endObject();
    fw.endArray();
    fw.close();

    String asJson = os.toString(StandardCharsets.UTF_8);
    assertThat(asJson).isEqualTo("[{\"one\":\"hello\",\"size\":43},{\"one\":\"another\",\"active\":true,\"flags\":[42,43]}]");

  }
}
