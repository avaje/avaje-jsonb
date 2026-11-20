package io.avaje.json.stream.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import io.avaje.json.JsonReader;
import org.junit.jupiter.api.Test;

class JParserNameCollisionTest {

  static final int ADMIN = JsonReader.nameHash("admin");
  static final int CAFE = JsonReader.nameHash("café");

  final JsonNames names = JsonNames.of("name", "admin", "café", "😀");

  @Test
  void collidingKeys() {
    assertThat(JsonReader.nameHash("6ktbvb")).isEqualTo(ADMIN);
    assertThat(JsonReader.nameHash("glbvs")).isEqualTo(JsonReader.nameHash("yacxa"));
  }

  @Test
  void knownHash() {
    assertThat(names.knownHash("admin")).isEqualTo(ADMIN);
    assertThat(names.knownHash("café")).isEqualTo(CAFE);
    assertThat(names.knownHash("unknown")).isEqualTo(JsonReader.nameHash("unknown"));
    assertThat(names.knownHash("6ktbvb")).isNotEqualTo(ADMIN).isEqualTo(names.unknownHash());
    assertNotKnown(names, names.unknownHash());
  }

  @Test
  void knownHash_clashingNames() {
    JsonNames clash = JsonNames.of("glbvs", "a", "yacxa");
    int hash = JsonReader.nameHash("glbvs");
    assertThat(clash.isClash(hash)).isTrue();
    assertThat(clash.knownHash("glbvs")).isEqualTo(hash);
    assertThat(clash.knownHash("yacxa")).isEqualTo(hash);
    assertThat(clash.knownHash("a")).isEqualTo(JsonReader.nameHash("a"));
    assertThat(names.isClash(ADMIN)).isFalse();
  }

  @Test
  void nextFieldHash_bytes() {
    JsonReadAdapter reader = reader("{\"admin\":1,\"6ktbvb\":2,\"café\":3,\"caf\\u00e9\":4,\"other\":5,\"adm\":6,\"😀\":7,\"\":8}");
    reader.beginObject();
    assertNext(reader, ADMIN, "admin", 1);
    assertNext(reader, names.unknownHash(), "6ktbvb", 2);
    assertNext(reader, CAFE, "café", 3);
    assertNext(reader, CAFE, "café", 4);
    assertNext(reader, JsonReader.nameHash("other"), "other", 5);
    assertNext(reader, JsonReader.nameHash("adm"), "adm", 6);
    assertNext(reader, JsonReader.nameHash("😀"), "😀", 7);
    assertNext(reader, JsonReader.nameHash(""), "", 8);
    assertThat(reader.hasNextField()).isFalse();
    reader.endObject();
  }

  @Test
  void nextFieldHash_escapedCollidingKey() {
    JsonReadAdapter reader = reader("{\"6ktbv\\u0062\":1,\"admi\\u006e\":2}");
    reader.beginObject();
    assertNext(reader, names.unknownHash(), "6ktbvb", 1);
    assertNext(reader, ADMIN, "admin", 2);
    reader.endObject();
  }

  @Test
  void nextField_strictPreparedNames() {
    JsonReadAdapter reader = reader("{\"admin\":1,\"6ktbvb\":2}");
    reader.beginObject(names);
    assertThat(reader.hasNextField()).isTrue();
    assertThat(reader.nextField()).isEqualTo("admin");
    assertThat(reader.readInt()).isEqualTo(1);
    assertThat(reader.hasNextField()).isTrue();
    assertThat(reader.nextField()).isEqualTo("6ktbvb");
    assertThat(reader.readInt()).isEqualTo(2);
    assertThat(reader.hasNextField()).isFalse();
    reader.endObject();
  }

  @Test
  void nextField_notStrict_preparedNamesStillMatchByHash() {
    JsonReadAdapter reader = reader("{\"6ktbvb\":1}", false);
    reader.beginObject(names);
    assertThat(reader.hasNextField()).isTrue();
    assertThat(reader.nextField()).isEqualTo("admin");
    assertThat(reader.readInt()).isEqualTo(1);
    reader.endObject();
  }

  @Test
  void nextFieldHash_clashingNames() {
    JsonNames clash = JsonNames.of("glbvs", "yacxa");
    int hash = JsonReader.nameHash("glbvs");
    JsonReadAdapter reader = reader("{\"glbvs\":1,\"yacxa\":2,\"6ktbvb\":3}");
    reader.beginObject();
    assertThat(reader.hasNextField()).isTrue();
    assertThat(reader.nextFieldHash(clash)).isEqualTo(hash);
    assertThat(reader.readInt()).isEqualTo(1);
    assertThat(reader.hasNextField()).isTrue();
    assertThat(reader.nextFieldHash(clash)).isEqualTo(hash);
    assertThat(reader.lastFieldName()).isEqualTo("yacxa");
    assertThat(reader.readInt()).isEqualTo(2);
    assertThat(reader.hasNextField()).isTrue();
    assertThat(reader.nextFieldHash(clash)).isEqualTo(JsonReader.nameHash("6ktbvb"));
    assertThat(reader.lastFieldName()).isEqualTo("6ktbvb");
    assertThat(reader.readInt()).isEqualTo(3);
    reader.endObject();
  }

  @Test
  void nextFieldHash_streamAcrossBufferBoundary() {
    for (int pad = 0; pad < 150; pad++) {
      String json = "{\"" + "p".repeat(pad) + "\":0,\"admin\":1,\"6ktbvb\":2,\"café\":3,\"6ktbvb\":4}";
      JParser parser = new JParser(new char[100], new byte[64], 64, JParser.DoublePrecision.HIGH,
        JParser.UnknownNumberParsing.BIGDECIMAL, 100, 1000);
      parser.process(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
      JsonReadAdapter reader = new JsonReadAdapter(parser, Recyclers.ThreadLocalPool.shared(), true, true, true);
      reader.beginObject();
      assertNext(reader, JsonReader.nameHash("p".repeat(pad)), "p".repeat(pad), 0);
      assertNext(reader, ADMIN, "admin", 1);
      assertNext(reader, names.unknownHash(), "6ktbvb", 2);
      assertNext(reader, CAFE, "café", 3);
      assertNext(reader, names.unknownHash(), "6ktbvb", 4);
      reader.endObject();
    }
  }

  @Test
  void nextFieldHash_notStrict_matchesByHashOnly() {
    byte[] content = "{\"6ktbvb\":1}".getBytes(StandardCharsets.UTF_8);
    JParser parser = new JParser(new char[100], new byte[1000], 1000, JParser.DoublePrecision.HIGH,
      JParser.UnknownNumberParsing.BIGDECIMAL, 100, 1000);
    parser.process(content, content.length);
    JsonReadAdapter reader = new JsonReadAdapter(parser, Recyclers.ThreadLocalPool.shared(), true, true);
    reader.beginObject();
    assertThat(reader.hasNextField()).isTrue();
    assertThat(reader.nextFieldHash(names)).isEqualTo(ADMIN);
    assertThat(reader.readInt()).isEqualTo(1);
    reader.endObject();
  }

  private void assertNext(JsonReadAdapter reader, int expectedHash, String name, int value) {
    assertThat(reader.hasNextField()).isTrue();
    assertThat(reader.nextFieldHash(names)).as(name).isEqualTo(expectedHash);
    assertThat(reader.lastFieldName()).isEqualTo(name);
    assertThat(reader.readInt()).isEqualTo(value);
  }

  private static void assertNotKnown(JsonNames names, int hash) {
    for (int i = 0; i < 4; i++) {
      assertThat(JsonReader.nameHash(names.name(i))).isNotEqualTo(hash);
    }
  }

  private static JsonReadAdapter reader(String json) {
    return reader(json, true);
  }

  private static JsonReadAdapter reader(String json, boolean strictPropertyNames) {
    byte[] content = json.getBytes(StandardCharsets.UTF_8);
    JParser parser = new JParser(new char[100], new byte[1000], 1000, JParser.DoublePrecision.HIGH,
      JParser.UnknownNumberParsing.BIGDECIMAL, 100, 1000);
    parser.process(content, content.length);
    return new JsonReadAdapter(parser, Recyclers.ThreadLocalPool.shared(), true, true, strictPropertyNames);
  }
}
