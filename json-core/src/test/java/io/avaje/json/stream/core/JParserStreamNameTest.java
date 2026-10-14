package io.avaje.json.stream.core;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JParserStreamNameTest {

  static final String AFTER = "after";

  final JsonNames names = JsonNames.of("m", AFTER);

  @Test
  void nextField_objectInsideObjectWithNames_nameCrossesBufferEnd() {
    // a small buffer, so that with the padding each name crosses the end of the buffer at every byte
    for (int pad = 0; pad < 150; pad++) {
      String padKey = "p".repeat(pad);
      String content = "{\"m\":{\"" + padKey + "\":0,\"café\":1,\"日本\":2,\"😀\":3},\"after\":4}";

      JParser parser = new JParser(new char[100], new byte[100], 100, JParser.DoublePrecision.HIGH,
        JParser.UnknownNumberParsing.BIGDECIMAL, 100, 1000);
      parser.process(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
      JsonReadAdapter reader = new JsonReadAdapter(parser, Recyclers.ThreadLocalPool.shared(), true, true);

      reader.beginObject(names);
      assertThat(reader.hasNextField()).isTrue();
      assertThat(reader.nextField()).isEqualTo("m");
      reader.beginObject();
      List<String> keys = new ArrayList<>();
      while (reader.hasNextField()) {
        keys.add(reader.nextField());
        reader.readInt();
      }
      reader.endObject();
      assertThat(keys).as("pad %d", pad).containsExactly(padKey, "café", "日本", "😀");

      assertThat(reader.hasNextField()).isTrue();
      // a known name is still found via the PropertyNames lookup
      assertThat(reader.nextField()).as("pad %d", pad).isSameAs(AFTER);
      assertThat(reader.readInt()).isEqualTo(4);
      assertThat(reader.hasNextField()).isFalse();
      reader.endObject();
    }
  }

  @Test
  void nextField_objectInsideObjectWithNames_escapedNameCrossesBufferEnd() {
    String[] decoded = {"b\"c", "x\ny", "café"};
    String[] raw = {"b\\\"c", "x\\ny", "caf\\u00e9"};
    for (int pad = 0; pad < 150; pad++) {
      String padKey = "p".repeat(pad);
      String content = "{\"m\":{\"" + padKey + "\":0,\"" + raw[0] + "\":1,\"" + raw[1] + "\":2,\"" + raw[2] + "\":3},\"after\":4}";

      JParser parser = new JParser(new char[100], new byte[100], 100, JParser.DoublePrecision.HIGH,
        JParser.UnknownNumberParsing.BIGDECIMAL, 100, 1000);
      parser.process(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
      JsonReadAdapter reader = new JsonReadAdapter(parser, Recyclers.ThreadLocalPool.shared(), true, true);

      reader.beginObject(names);
      assertThat(reader.hasNextField()).isTrue();
      assertThat(reader.nextField()).isEqualTo("m");
      reader.beginObject();
      assertThat(reader.hasNextField()).isTrue();
      assertThat(reader.nextField()).isEqualTo(padKey);
      reader.readInt();
      for (int i = 0; i < decoded.length; i++) {
        assertThat(reader.hasNextField()).isTrue();
        assertThat(reader.nextField()).as("pad %d", pad).isEqualTo(decoded[i]);
        reader.readInt();
      }
      assertThat(reader.hasNextField()).isFalse();
      reader.endObject();
    }
  }
}
