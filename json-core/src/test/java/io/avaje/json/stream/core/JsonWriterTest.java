package io.avaje.json.stream.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;

import io.avaje.json.stream.JsonOutput;
import io.avaje.json.stream.JsonStream;
import org.junit.jupiter.api.Test;

import io.avaje.json.JsonWriter;
import io.avaje.json.stream.core.HybridBufferRecycler.StripedLockFreePool;
import io.avaje.json.stream.core.Recyclers.ThreadLocalPool;

class JsonWriterTest {

  @Test
  void flush_expect_flushUnderlyingWriter() {
    JsonStream build = CoreJsonStream.builder().serializeNulls(true).build();

    CharArrayWriter writer = new CharArrayWriter();
    JsonWriter jsonWriter = build.writer(writer);

    jsonWriter.value("test");
    jsonWriter.flush();
    assertThat(writer.toCharArray()).isEqualTo("\"test\"".toCharArray());
    jsonWriter.close();
    assertThat(writer.toCharArray()).isEqualTo("\"test\"".toCharArray());
  }

  @Test
  void recycle() {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    JsonGenerator generator = ThreadLocalPool.shared().generator(JsonOutput.of(os));

    writeHello(generator, "hello");

    String asJson = os.toString();
    assertThat(asJson).isEqualTo("{\"one\":\"hello\"}");

    ByteArrayOutputStream os1 = new ByteArrayOutputStream();
    JsonGenerator generator1 = ThreadLocalPool.shared().generator(JsonOutput.of(os1));

    writeHello(generator1, "hi");

    String asJson1 = os1.toString();
    assertThat(asJson1).isEqualTo("{\"one\":\"hi\"}");
  }

  @Test
  void recycle_toString() {

    JsonGenerator generator = StripedLockFreePool.shared().generator();
    writeHello(generator, "hello");
    assertThat(generator.toString()).isEqualTo("{\"one\":\"hello\"}");

    JsonGenerator generator1 = StripedLockFreePool.shared().generator();
    writeHello(generator1, "hi");
    assertThat(generator1.toString()).isEqualTo("{\"one\":\"hi\"}");
  }

  private void writeHello(JsonGenerator generator, String message) {
    JsonWriteAdapter fw = new JsonWriteAdapter(generator, ThreadLocalPool.shared(), true, true);

    fw.beginObject();
    fw.name("one");
    fw.value(message);
    fw.endObject();
    fw.close();
  }

  @Test
  void basic() {

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    JGenerator dJsonWriter = new JGenerator();
    dJsonWriter.prepare(JsonOutput.of(os));

    JsonWriteAdapter fw = new JsonWriteAdapter(dJsonWriter, HybridBufferRecycler.shared(), true, true);

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

    String asJson = os.toString();
    assertThat(asJson).isEqualTo("[{\"one\":\"hello\",\"size\":43},{\"one\":\"another\",\"active\":true,\"flags\":[42,43]}]");
  }


  @Test
  void using_names() {

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    JGenerator dJsonWriter = new JGenerator();
    dJsonWriter.prepare(JsonOutput.of(os));

    JsonWriteAdapter fw = new JsonWriteAdapter(dJsonWriter, HybridBufferRecycler.shared(), true, true);

    JsonNames names = JsonNames.of("one", "size", "active", "flags");

    fw.beginArray();

    fw.beginObject(names);
    fw.name(0);
    fw.value("hello");
    fw.name(1);
    fw.value(43);
    fw.endObject();

    fw.beginObject(names);
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

    String asJson = os.toString();
    assertThat(asJson).isEqualTo("[{\"one\":\"hello\",\"size\":43},{\"one\":\"another\",\"active\":true,\"flags\":[42,43]}]");

  }
}
