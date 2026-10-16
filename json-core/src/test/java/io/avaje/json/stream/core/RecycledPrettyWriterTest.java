package io.avaje.json.stream.core;

import io.avaje.json.JsonWriter;
import io.avaje.json.stream.BufferRecycleStrategy;
import io.avaje.json.stream.JsonStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A recycled pretty writer must not keep the indentation of a write that failed halfway.
 */
class RecycledPrettyWriterTest {

  private static String write(JsonStream stream, boolean fail) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (JsonWriter w = stream.writer(out)) {
      w.pretty(true);
      w.beginObject();
      w.name("a");
      w.beginObject();
      w.name("b");
      if (fail) throw new IllegalStateException("failed halfway");
      w.value(1);
      w.endObject();
      w.endObject();
    } catch (IllegalStateException e) {
      return null;
    }
    return out.toString();
  }

  @Test
  void prettyDepthReset_afterIncompleteWrite() {
    JsonStream stream = JsonStream.builder().bufferRecycling(BufferRecycleStrategy.THREAD_LOCAL).build();
    String first = write(stream, false);
    assertThat(first).isEqualTo("{\n  \"a\": {\n    \"b\": 1\n  }\n}");

    assertThat(write(stream, true)).isNull();
    assertThat(write(stream, false)).isEqualTo(first);
  }

  @Test
  void prettyArrayDepthReset_afterIncompleteWrite() {
    JsonStream stream = JsonStream.builder().bufferRecycling(BufferRecycleStrategy.THREAD_LOCAL).build();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (JsonWriter w = stream.writer(out)) {
      w.pretty(true);
      w.beginArray();
      w.beginArray();
      throw new IllegalStateException("failed halfway");
    } catch (IllegalStateException ignored) {
    }
    assertThat(write(stream, false)).isEqualTo("{\n  \"a\": {\n    \"b\": 1\n  }\n}");
  }
}
