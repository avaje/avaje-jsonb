package io.avaje.json.stream.core;

import io.avaje.json.JsonWriter;
import io.avaje.json.stream.JsonStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pretty printing lays out arrays the same way at any depth and keeps empty containers on one line.
 */
class PrettyLayoutTest {

  private final JsonStream stream = JsonStream.builder().build();

  private String pretty(Consumer<JsonWriter> body) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (JsonWriter w = stream.writer(out)) {
      w.pretty(true);
      body.accept(w);
    }
    return out.toString();
  }

  @Test
  void nestedArrays_sameLayoutAsTopLevel() {
    String json = pretty(w -> {
      w.beginObject();
      w.name("empty"); w.beginArray(); w.endArray();
      w.name("top"); w.beginArray(); w.value(1); w.value(2); w.endArray();
      w.name("outer"); w.beginObject();
      w.name("list"); w.beginArray(); w.value(5); w.value(6); w.endArray();
      w.name("objects"); w.beginArray(); w.beginObject(); w.name("x"); w.value(1); w.endObject(); w.endArray();
      w.endObject();
      w.endObject();
    });
    assertThat(json).isEqualTo("{\n" +
      "  \"empty\": [],\n" +
      "  \"top\": [\n" +
      "    1,\n" +
      "    2\n" +
      "  ],\n" +
      "  \"outer\": {\n" +
      "    \"list\": [\n" +
      "      5,\n" +
      "      6\n" +
      "    ],\n" +
      "    \"objects\": [\n" +
      "      {\n" +
      "        \"x\": 1\n" +
      "      }\n" +
      "    ]\n" +
      "  }\n" +
      "}");
  }

  @Test
  void topLevelArray_ofArraysAndObjects() {
    String json = pretty(w -> {
      w.beginArray();
      w.value(1);
      w.beginArray(); w.value(2); w.beginArray(); w.endArray(); w.endArray();
      w.beginObject(); w.endObject();
      w.beginObject(); w.name("a"); w.beginObject(); w.endObject(); w.endObject();
      w.endArray();
    });
    assertThat(json).isEqualTo("[\n" +
      "  1,\n" +
      "  [\n" +
      "    2,\n" +
      "    []\n" +
      "  ],\n" +
      "  {},\n" +
      "  {\n" +
      "    \"a\": {}\n" +
      "  }\n" +
      "]");
  }

  @Test
  void emptyTopLevel() {
    assertThat(pretty(w -> { w.beginObject(); w.endObject(); })).isEqualTo("{}");
    assertThat(pretty(w -> { w.beginArray(); w.endArray(); })).isEqualTo("[]");
  }

  @Test
  void deepNesting_growsLevels() {
    String json = pretty(w -> {
      for (int i = 0; i < 40; i++) w.beginArray();
      w.value(1);
      for (int i = 0; i < 40; i++) w.endArray();
    });
    assertThat(json).contains("\n" + "  ".repeat(40) + "1\n");
    assertThat(json).endsWith("\n]");
  }
}
