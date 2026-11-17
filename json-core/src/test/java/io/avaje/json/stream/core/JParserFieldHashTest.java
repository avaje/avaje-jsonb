package io.avaje.json.stream.core;

import io.avaje.json.JsonReader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class JParserFieldHashTest {

  final JsonNames names = JsonNames.of("m", "after");

  @Test
  void jsonNames_lookup() {
    JsonNames names = JsonNames.of("a", "b", "café");
    assertThat(names.lookup(JsonReader.nameHash("a"))).isSameAs("a");
    assertThat(names.lookup(JsonReader.nameHash("b"))).isSameAs("b");
    assertThat(names.lookup(JsonReader.nameHash("café"))).isEqualTo("café");
    assertThat(names.lookup(JsonReader.nameHash("c"))).isNull();
  }

  @Test
  void jsonNames_lookup_collidingNamesRemoved() {
    // "glbvs" and "yacxa" have the same FNV-1a hash
    assertThat(JsonReader.nameHash("glbvs")).isEqualTo(JsonReader.nameHash("yacxa"));
    JsonNames names = JsonNames.of("glbvs", "a", "yacxa");
    assertThat(names.lookup(JsonReader.nameHash("glbvs"))).isNull();
    assertThat(names.lookup(JsonReader.nameHash("a"))).isEqualTo("a");
  }

  @Test
  void nextFieldHash_bytes() {
    String content = "{\"m\" : 1, \"caf\\u00e9\":2,\"unknown\":3}";
    JsonReadAdapter reader = reader(content.getBytes(StandardCharsets.UTF_8));
    reader.beginObject(names);
    assertThat(reader.hasNextField()).isTrue();
    assertThat(reader.nextFieldHash()).isEqualTo(JsonReader.nameHash("m"));
    assertThat(reader.lastFieldName()).isEqualTo("m");
    assertThat(reader.readInt()).isEqualTo(1);
    assertThat(reader.hasNextField()).isTrue();
    assertThat(reader.nextFieldHash()).isEqualTo(JsonReader.nameHash("café"));
    assertThat(reader.lastFieldName()).isEqualTo("café");
    assertThat(reader.readInt()).isEqualTo(2);
    assertThat(reader.hasNextField()).isTrue();
    assertThat(reader.nextFieldHash()).isEqualTo(JsonReader.nameHash("unknown"));
    assertThat(reader.lastFieldName()).isEqualTo("unknown");
    assertThat(reader.readInt()).isEqualTo(3);
    assertThat(reader.hasNextField()).isFalse();
    reader.endObject();
  }

  @Test
  void nextFieldHash_stream_nameAndValueCrossBufferEnd() {
    String[] decoded = {"café", "日本", "😀", "b\"c", "x\ny"};
    String[] raw = {"café", "日本", "😀", "b\\\"c", "x\\ny"};
    // a small buffer, so that with the padding each name, colon and value crosses the end of the buffer
    for (int pad = 0; pad < 150; pad++) {
      String padKey = "p".repeat(pad);
      StringBuilder sb = new StringBuilder("{\"").append(padKey).append("\":0");
      for (int i = 0; i < raw.length; i++) {
        sb.append(",\"").append(raw[i]).append("\"  :  ").append(i + 1);
      }
      String content = sb.append('}').toString();

      JParser parser = new JParser(new char[100], new byte[100], 100, JParser.DoublePrecision.HIGH,
        JParser.UnknownNumberParsing.BIGDECIMAL, 100, 1000);
      parser.process(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
      JsonReadAdapter reader = new JsonReadAdapter(parser, Recyclers.ThreadLocalPool.shared(), true, true);

      reader.beginObject(names);
      assertThat(reader.hasNextField()).isTrue();
      assertThat(reader.nextFieldHash()).isEqualTo(JsonReader.nameHash(padKey));
      assertThat(reader.lastFieldName()).as("pad %d", pad).isEqualTo(padKey);
      reader.readInt();
      for (int i = 0; i < decoded.length; i++) {
        assertThat(reader.hasNextField()).isTrue();
        assertThat(reader.nextFieldHash()).as("pad %d", pad).isEqualTo(JsonReader.nameHash(decoded[i]));
        assertThat(reader.lastFieldName()).as("pad %d", pad).isEqualTo(decoded[i]);
        assertThat(reader.readInt()).isEqualTo(i + 1);
      }
      assertThat(reader.hasNextField()).isFalse();
      reader.endObject();
    }
  }

  private static JsonReadAdapter reader(byte[] content) {
    JParser parser = new JParser(new char[100], new byte[1000], 1000, JParser.DoublePrecision.HIGH,
      JParser.UnknownNumberParsing.BIGDECIMAL, 100, 1000);
    parser.process(content, content.length);
    return new JsonReadAdapter(parser, Recyclers.ThreadLocalPool.shared(), true, true);
  }

  @Test
  void lastFieldName_beforeAnyField_returnsNull() {
    byte[] content = "{}".getBytes(StandardCharsets.UTF_8);
    JsonReadAdapter byteReader = reader(content);
    byteReader.beginObject();
    assertThat(byteReader.lastFieldName()).isNull();
    byteReader.endObject();

    JParser streamParser = new JParser(new char[100], new byte[100], 100, JParser.DoublePrecision.HIGH,
      JParser.UnknownNumberParsing.BIGDECIMAL, 100, 1000);
    streamParser.process(new ByteArrayInputStream(content));
    JsonReadAdapter streamReader = new JsonReadAdapter(streamParser, Recyclers.ThreadLocalPool.shared(), true, true);
    streamReader.beginObject();
    assertThat(streamReader.lastFieldName()).isNull();
    streamReader.endObject();
  }

  @Test
  void lastFieldName_resetAcrossCloseAndReuse() {
    JParser parser = new JParser(new char[100], new byte[1000], 1000, JParser.DoublePrecision.HIGH,
      JParser.UnknownNumberParsing.BIGDECIMAL, 100, 1000);

    // 1) Unescaped field sets nameStart/nameEnd > 0; close() and process() must reset safely
    byte[] first = "{\"   \" : 0, \"first\":1}".getBytes(StandardCharsets.UTF_8);
    parser.process(first, first.length);
    JsonReadAdapter r1 = new JsonReadAdapter(parser, Recyclers.ThreadLocalPool.shared(), true, true);
    r1.beginObject();
    assertThat(r1.hasNextField()).isTrue();
    r1.nextFieldHash();
    r1.readInt();
    assertThat(r1.hasNextField()).isTrue();
    r1.nextFieldHash();
    assertThat(r1.lastFieldName()).isEqualTo("first");
    r1.readInt();
    r1.endObject();
    parser.close();
    assertThat(parser.lastFieldName()).isNull();

    // Reuse via process(byte[], int) without calling close() after escaped field sets lastName / nameEnd = -1
    byte[] escaped = "{\"fir\\u0073t\":1}".getBytes(StandardCharsets.UTF_8);
    parser.process(escaped, escaped.length);
    JsonReadAdapter r2 = new JsonReadAdapter(parser, Recyclers.ThreadLocalPool.shared(), true, true);
    r2.beginObject();
    assertThat(r2.lastFieldName()).isNull();
    assertThat(r2.hasNextField()).isTrue();
    r2.nextFieldHash();
    assertThat(r2.lastFieldName()).isEqualTo("first");
    r2.readInt();
    r2.endObject();

    // Reuse directly via process(byte[], int) -> must not retain stale lastName = "first"
    byte[] empty = "{}".getBytes(StandardCharsets.UTF_8);
    parser.process(empty, empty.length);
    assertThat(parser.lastFieldName()).isNull();

    // Reuse via process(InputStream) after setting lastName -> must also reset lastName to null
    parser.process(escaped, escaped.length);
    JsonReadAdapter r3 = new JsonReadAdapter(parser, Recyclers.ThreadLocalPool.shared(), true, true);
    r3.beginObject();
    assertThat(r3.hasNextField()).isTrue();
    r3.nextFieldHash();
    assertThat(r3.lastFieldName()).isEqualTo("first");
    r3.readInt();
    r3.endObject();

    parser.process(new ByteArrayInputStream(empty));
    assertThat(parser.lastFieldName()).isNull();
  }

  @Test
  void lastFieldName_staleLastNameAfterProcess() {
    JParser parser = new JParser(new char[100], new byte[1000], 1000, JParser.DoublePrecision.HIGH,
      JParser.UnknownNumberParsing.BIGDECIMAL, 100, 1000);

    byte[] escaped = "{\"fir\\u0073t\":1}".getBytes(StandardCharsets.UTF_8);
    parser.process(escaped, escaped.length);
    JsonReadAdapter r1 = new JsonReadAdapter(parser, Recyclers.ThreadLocalPool.shared(), true, true);
    r1.beginObject();
    assertThat(r1.hasNextField()).isTrue();
    r1.nextFieldHash();
    assertThat(r1.lastFieldName()).isEqualTo("first");
    r1.readInt();
    r1.endObject();

    // process(byte[], int) currently leaves nameEnd = -1 and lastName = "first"
    byte[] empty = "{}".getBytes(StandardCharsets.UTF_8);
    parser.process(empty, empty.length);
    assertThat(parser.lastFieldName()).isNull();
  }
}
