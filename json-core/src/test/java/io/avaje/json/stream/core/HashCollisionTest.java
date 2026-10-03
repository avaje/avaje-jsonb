package io.avaje.json.stream.core;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A JSON key whose avaje name-hash collides with a declared property name must not be
 * resolved to that property. The parser must compare the actual key, not only the hash.
 */
class HashCollisionTest {

  final JParser.DoublePrecision doublePrecision = JParser.DoublePrecision.HIGH;
  final JParser.UnknownNumberParsing unknownNumbers = JParser.UnknownNumberParsing.BIGDECIMAL;

  // "6ktbvb" shares the avaje name-hash of "admin"; "9m3dV" shares the hash of "name".
  @Test
  void collidingKey_inBuffer_returnsActualKey() {
    assertThat(readSingleField("{\"6ktbvb\":true}", false, "name", "admin"))
      .isEqualTo("6ktbvb");
  }

  @Test
  void collidingKey_stream_crossBuffer_returnsActualKey() {
    assertThat(readSingleField("{\"6ktbvb\":true}", true, "name", "admin"))
      .isEqualTo("6ktbvb");
  }

  @Test
  void escapedKey_doesNotMatchUnescapedName_inBuffer() {
    // key "\n" escape-hashes to the same value as "n" but is not the property "n"
    assertThat(readSingleField("{\"\\n\":true}", false, "n"))
      .isNotEqualTo("n");
  }

  @Test
  void escapedKey_doesNotMatchUnescapedName_stream() {
    assertThat(readSingleField("{\"\\n\":true}", true, "n"))
      .isNotEqualTo("n");
  }

  @Test
  void genuineName_stillMatches_inBuffer() {
    assertThat(readSingleField("{\"admin\":true}", false, "name", "admin"))
      .isEqualTo("admin");
  }

  @Test
  void genuineName_stillMatches_stream() {
    assertThat(readSingleField("{\"admin\":true}", true, "name", "admin"))
      .isEqualTo("admin");
  }

  @Test
  void multipleFields_mixOfRealAndColliding() {
    List<String> fields = readFields("{\"name\":\"x\",\"6ktbvb\":true,\"admin\":false}", false, "name", "admin");
    assertThat(fields).containsExactly("name", "6ktbvb", "admin");
  }

  private String readSingleField(String json, boolean stream, String... names) {
    return readFields(json, stream, names).get(0);
  }

  private List<String> readFields(String json, boolean stream, String... names) {
    // small buffer so the streaming path crosses buffers (calcHashAndCopyName)
    final int len = stream ? 20 : 1000;
    final JParser parser = new JParser(new char[len], new byte[len], len, doublePrecision, unknownNumbers, 100, 1000);
    if (stream) {
      parser.process(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
    } else {
      final byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
      parser.process(bytes, bytes.length);
    }
    parser.startObject(JsonNames.of(names));
    final List<String> fields = new ArrayList<>();
    while (hasNextField(parser)) {
      fields.add(parser.nextField());
      parser.skipValue();
    }
    parser.endObject();
    return fields;
  }

  // mirrors JsonReadAdapter.hasNextField
  private boolean hasNextField(JParser parser) {
    byte nextToken = parser.nextToken();
    if (nextToken == '"') {
      return true;
    }
    if (nextToken == ',') {
      return parser.nextToken() == '"';
    }
    return false;
  }
}
