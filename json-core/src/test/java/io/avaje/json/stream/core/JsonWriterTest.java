package io.avaje.json.stream.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import io.avaje.json.stream.BufferedJsonWriter;
import io.avaje.json.stream.BytesJsonWriter;
import io.avaje.json.JsonWriter;
import io.avaje.json.stream.JsonOutput;
import io.avaje.json.stream.JsonStream;
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

  @Test
  void largeString() {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    JGenerator dJsonWriter = new JGenerator(200);
    dJsonWriter.prepare(JsonOutput.ofStream(os));

    JsonWriteAdapter fw = new JsonWriteAdapter(dJsonWriter, HybridBufferRecycler.shared(), true, true);

    String largeValue = "_123456789_123456789_123456789_123456789_123456789".repeat(11);

    fw.beginObject();
    fw.name("key");
    fw.value(largeValue);
    fw.endObject();
    fw.close();

    String jsonResult = new String(os.toByteArray(), 0, os.toByteArray().length);
    assertThat(jsonResult).isEqualTo("{\"key\":\"" + largeValue + "\"}");

    byte[] effectiveBufferSize = dJsonWriter.ensureCapacity(0);
    assertThat(effectiveBufferSize.length)
      .describedAs("internal buffer should not grow")
      .isEqualTo(200);
  }

  @Test
  void largeStringUnicode() {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    JGenerator dJsonWriter = new JGenerator(100);
    dJsonWriter.prepare(JsonOutput.ofStream(os));

    JsonWriteAdapter fw = new JsonWriteAdapter(dJsonWriter, HybridBufferRecycler.shared(), true, true);

    String largeValue = "_12£45Ã789ǣ123456789_123456789Ŕ123456789_123456789".repeat(11);

    fw.beginObject();
    fw.name("key");
    fw.value(largeValue);
    fw.endObject();
    fw.close();

    String jsonResult = new String(os.toByteArray(), 0, os.toByteArray().length);
    assertThat(jsonResult).isEqualTo("{\"key\":\"" + largeValue + "\"}");

    byte[] effectiveBufferSize = dJsonWriter.ensureCapacity(0);
    assertThat(effectiveBufferSize.length)
      .describedAs("internal buffer should not grow")
      .isEqualTo(100);
  }

  @Test
  void largeStringAscii() {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    JGenerator dJsonWriter = new JGenerator(100);
    dJsonWriter.prepare(JsonOutput.ofStream(os));

    JsonWriteAdapter fw = new JsonWriteAdapter(dJsonWriter, HybridBufferRecycler.shared(), true, true);

    String largeValue = '"' + "_1234567890123456789".repeat(21) + '"';

    fw.beginObject();
    fw.name("key");
    fw.rawValue(largeValue);
    fw.endObject();
    fw.close();

    String jsonResult = new String(os.toByteArray(), 0, os.toByteArray().length);
    assertThat(jsonResult).isEqualTo("{\"key\":" + largeValue + "}");

    byte[] effectiveBufferSize = dJsonWriter.ensureCapacity(0);
    assertThat(effectiveBufferSize.length)
      .describedAs("internal buffer should not grow")
      .isEqualTo(100);
  }

  @Test
  void rawValue() {
    JsonStream build = CoreJsonStream.builder().serializeNulls(true).build();

    StringWriter writer = new StringWriter();
    JsonWriter fw = build.writer(writer);

    fw.beginObject();
    fw.name("key");
    fw.rawValue("\"abc\"");
    fw.endObject();
    fw.close();

    assertThat(writer.toString()).isEqualTo("{\"key\":\"abc\"}");
  }

  @Test
  void rawValue_unicode_stringWriter() {
    JsonStream build = CoreJsonStream.builder().serializeNulls(true).build();

    BufferedJsonWriter stringWriter = build.bufferedWriter();
    stringWriter.beginObject();
    stringWriter.name("key");
    stringWriter.rawValue("\"安\"");
    stringWriter.endObject();
    assertThat(stringWriter.result()).isEqualTo("{\"key\":\"安\"}");
  }

  @Test
  void rawValue_unicode_bytesWriter() {
    JsonStream build = CoreJsonStream.builder().serializeNulls(true).build();

    BytesJsonWriter bytesWriter = build.bufferedWriterAsBytes();
    bytesWriter.beginObject();
    bytesWriter.name("key");
    bytesWriter.rawValue("\"安\"");
    bytesWriter.endObject();
    assertThat(bytesWriter.result()).isEqualTo("{\"key\":\"安\"}".getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void rawValue_unicode_mixedWidths() {
    var build = CoreJsonStream.builder().serializeNulls(true).build();
    var raw = "\"a\u00e9\u5b89\uD83D\uDE00z\"";

    var bytesWriter = build.bufferedWriterAsBytes();
    bytesWriter.beginObject();
    bytesWriter.name("key");
    bytesWriter.rawValue(raw);
    bytesWriter.endObject();
    assertThat(bytesWriter.result())
        .isEqualTo(("{\"key\":" + raw + "}").getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void rawValue_unicode_large() {
    JsonStream build = CoreJsonStream.builder().serializeNulls(true).build();
    StringBuilder sb = new StringBuilder("\"");
    for (int i = 0; i < 20_000; i++) {
      sb.append("ab\u00e9\u5b89\uD83D\uDE00");
    }
    String raw = sb.append('"').toString();

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try (JsonWriter writer = build.writer(os)) {
      writer.beginObject();
      writer.name("key");
      writer.rawValue(raw);
      writer.endObject();
    }
    assertThat(os.toByteArray()).isEqualTo(("{\"key\":" + raw + "}").getBytes(StandardCharsets.UTF_8));

    BytesJsonWriter bytesWriter = build.bufferedWriterAsBytes();
    bytesWriter.rawValue(raw);
    assertThat(bytesWriter.result()).isEqualTo(raw.getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void rawValue_loneSurrogate_throws() {
    JsonStream build = CoreJsonStream.builder().serializeNulls(true).build();
    BytesJsonWriter bytesWriter = build.bufferedWriterAsBytes();
    assertThatThrownBy(() -> bytesWriter.rawValue("\"\uD83Dx\""))
      .hasMessageContaining("Invalid unicode codepoint d83d");
  }

  @Test
  void rawChunk() {
    JsonStream build = CoreJsonStream.builder().serializeNulls(true).build();

    StringWriter writer = new StringWriter();
    JsonWriter fw = build.writer(writer);

    fw.beginObject();
    fw.name("key");
    fw.rawChunkStart();
    fw.rawChunk('"');
    fw.rawChunk("a");
    fw.rawChunk("bc");
    fw.rawChunk('"');
    fw.rawChunkEnd();
    fw.name("x");
    fw.value(1);
    fw.endObject();
    fw.close();

    assertThat(writer.toString()).isEqualTo("{\"key\":\"abc\",\"x\":1}");
  }

  @Test
  void rawChunk_unicode() {
    var build = CoreJsonStream.builder().serializeNulls(true).build();

    var os = new ByteArrayOutputStream();
    var fw = build.writer(os);

    fw.beginObject();
    fw.name("key");
    fw.rawChunkStart();
    fw.rawChunk('"');
    fw.rawChunk("a\u5b89");
    fw.rawChunk('\u00e9');
    fw.rawChunk("\uD83D\uDE00z");
    fw.rawChunk('"');
    fw.rawChunkEnd();
    fw.name("x");
    fw.value(1);
    fw.endObject();
    fw.close();

    assertThat(os.toByteArray())
        .isEqualTo(
            "{\"key\":\"a\u5b89\u00e9\uD83D\uDE00z\",\"x\":1}".getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void rawChunkEncode() {
    JsonStream build = CoreJsonStream.builder().serializeNulls(true).build();

    StringWriter writer = new StringWriter();
    JsonWriter fw = build.writer(writer);

    fw.beginObject();
    fw.name("key");
    fw.rawChunkStart();
    fw.rawChunk('"');
    fw.rawChunkEncode("a\n");
    fw.rawChunkEncode("b£c");
    fw.rawChunk('"');
    fw.rawChunkEnd();
    fw.name("x");
    fw.value(1);
    fw.endObject();
    fw.close();

    assertThat(writer.toString()).isEqualTo("{\"key\":\"a\\nb£c\",\"x\":1}");
  }

  @Test
  void testAllUnicodeCharacters() {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    JGenerator dJsonWriter = new JGenerator(8192); // Larger buffer for Unicode
    dJsonWriter.prepare(JsonOutput.ofStream(os));
    JsonWriteAdapter fw =
        new JsonWriteAdapter(dJsonWriter, HybridBufferRecycler.shared(), true, true);

    // Test all valid Unicode code points (0x0000 to 0x10FFFF)
    // Excluding surrogate pairs (0xD800 to 0xDFFF)
    fw.beginObject();
    fw.name("unicode_test");
    fw.beginArray();

    // Basic Multilingual Plane (BMP): 0x0000 to 0xFFFF
    for (int codePoint = 0; codePoint <= 0xFFFF; codePoint++) {
      // Skip surrogate pair range
      if (codePoint >= 0xD800 && codePoint <= 0xDFFF) {
        continue;
      }
      fw.value(new String(Character.toChars(codePoint)));
    }

    // Supplementary planes: 0x10000 to 0x10FFFF
    for (int codePoint = 0x10000; codePoint <= 0x10FFFF; codePoint++) {
      if (Character.isValidCodePoint(codePoint)) {
        fw.value(new String(Character.toChars(codePoint)));
      }
    }

    fw.endArray();
    fw.endObject();
    fw.close();

    String jsonResult = new String(os.toByteArray(), StandardCharsets.UTF_8);
    assertThat(jsonResult).isNotEmpty();
    assertThat(jsonResult).startsWith("{\"unicode_test\":[");
    assertThat(jsonResult).endsWith("]}");
  }

  @Test
  void testUnicodeByPlane() {
    // Test specific Unicode planes separately for better debugging
    testUnicodePlane("Basic Latin", 0x0000, 0x007F);
    testUnicodePlane("Latin-1 Supplement", 0x0080, 0x00FF);
    testUnicodePlane("Greek and Coptic", 0x0370, 0x03FF);
    testUnicodePlane("Cyrillic", 0x0400, 0x04FF);
    testUnicodePlane("Hebrew", 0x0590, 0x05FF);
    testUnicodePlane("Arabic", 0x0600, 0x06FF);
    testUnicodePlane("CJK Unified Ideographs", 0x4E00, 0x4EFF); // Subset
    testUnicodePlane("Emoji", 0x1F600, 0x1F64F);
  }

  private void testUnicodePlane(String planeName, int start, int end) {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    JGenerator dJsonWriter = new JGenerator(4096);
    dJsonWriter.prepare(JsonOutput.ofStream(os));
    JsonWriteAdapter fw =
        new JsonWriteAdapter(dJsonWriter, HybridBufferRecycler.shared(), true, true);

    fw.beginObject();
    fw.name(planeName);

    StringBuilder sb = new StringBuilder();
    for (int codePoint = start; codePoint <= end; codePoint++) {
      if (Character.isValidCodePoint(codePoint) && ((codePoint < 0xD800) || (codePoint > 0xDFFF))) {
        sb.appendCodePoint(codePoint);
      }
    }

    fw.value(sb.toString());
    fw.endObject();
    fw.close();

    String jsonResult = new String(os.toByteArray(), StandardCharsets.UTF_8);
    assertThat(jsonResult).contains(planeName);
  }

  @Test
  void testSpecialUnicodeCharacters() {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    JGenerator dJsonWriter = new JGenerator(1024);
    dJsonWriter.prepare(JsonOutput.ofStream(os));
    JsonWriteAdapter fw =
        new JsonWriteAdapter(dJsonWriter, HybridBufferRecycler.shared(), true, true);

    fw.beginObject();

    // Zero-width characters
    fw.name("zero_width");
    fw.value("\u200B\u200C\u200D\uFEFF");

    // Right-to-left marks
    fw.name("rtl_marks");
    fw.value("\u200E\u200F");

    // Combining characters
    fw.name("combining");
    fw.value("e\u0301"); // é composed

    // Emoji with modifiers
    fw.name("emoji");
    fw.value("👋🏽"); // Waving hand with skin tone

    // Multi-byte characters
    fw.name("multibyte");
    fw.value("𝕳𝖊𝖑𝖑𝖔"); // Mathematical bold characters

    fw.endObject();
    fw.close();

    String jsonResult = new String(os.toByteArray(), StandardCharsets.UTF_8);
    assertThat(jsonResult)
        .isEqualTo(
            "{\"zero_width\":\"​‌‍﻿\",\"rtl_marks\":\"‎‏\",\"combining\":\"é\",\"emoji\":\"👋🏽\",\"multibyte\":\"𝕳𝖊𝖑𝖑𝖔\"}");
  }

  @Test
  void testAsciiCharacters() {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    JGenerator dJsonWriter = new JGenerator(100);
    dJsonWriter.prepare(JsonOutput.ofStream(os));
    JsonWriteAdapter fw =
        new JsonWriteAdapter(dJsonWriter, HybridBufferRecycler.shared(), true, true);

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 127; i++) {
      sb.append((char) i);
    }
    String largeValue = sb.toString();

    fw.beginObject();
    fw.name("key");
    fw.value(largeValue);
    fw.endObject();
    fw.close();

    String jsonResult = new String(os.toByteArray(), 0, os.toByteArray().length);
    assertThat(jsonResult)
        .isEqualTo(
            "{\"key\":\"\\u0000\\u0001\\u0002\\u0003\\u0004\\u0005\\u0006\\u0007\\b\\t\\n\\u000B\\f\\r\\u000E\\u000F\\u0010\\u0011\\u0012\\u0013\\u0014\\u0015\\u0016\\u0017\\u0018\\u0019\\u001A\\u001B\\u001C\\u001D\\u001E\\u001F !\\\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\"}");

    byte[] effectiveBufferSize = dJsonWriter.ensureCapacity(0);
    assertThat(effectiveBufferSize.length)
        .describedAs("internal buffer should not grow")
        .isEqualTo(100);
  }

  @Test
  void testEmojiGraphemeClusters() {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    JGenerator dJsonWriter = new JGenerator(4096);
    dJsonWriter.prepare(JsonOutput.ofStream(os));
    JsonWriteAdapter fw =
        new JsonWriteAdapter(dJsonWriter, HybridBufferRecycler.shared(), true, true);

    fw.beginObject();
    fw.name("emoji_clusters");
    fw.beginArray();

    // Single emoji (1 code point, 1 grapheme)
    fw.value("😀");

    // Emoji with skin tone modifier (2 code points, 1 grapheme)
    fw.value("👋🏽");
    fw.value("👨🏿");

    // Emoji ZWJ sequences (multiple code points, 1 grapheme)
    fw.value("👨‍👩‍👧‍👦");
    fw.value("👨‍💻");
    fw.value("👩‍🔬");

    // Regional indicator sequences (flags, 2 code points, 1 grapheme)
    fw.value("🇺🇸");
    fw.value("🇬🇧");
    fw.value("🇯🇵");

    // Emoji with variation selectors
    fw.value("❤️");

    fw.endArray();
    fw.endObject();
    fw.close();

    String jsonResult = new String(os.toByteArray(), StandardCharsets.UTF_8);
    assertThat(jsonResult)
        .isEqualTo(
            "{\"emoji_clusters\":[\"😀\",\"👋🏽\",\"👨🏿\",\"👨‍👩‍👧‍👦\",\"👨‍💻\",\"👩‍🔬\",\"🇺🇸\",\"🇬🇧\",\"🇯🇵\",\"❤️\"]}");
  }
}
