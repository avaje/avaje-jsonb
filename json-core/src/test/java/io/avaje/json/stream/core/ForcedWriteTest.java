package io.avaje.json.stream.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonWriter;
import io.avaje.json.core.CoreTypes;
import io.avaje.json.stream.JsonStream;

public class ForcedWriteTest {
  JsonStream stream = CoreJsonStream.builder().serializeNulls(false).serializeEmpty(false).build();

  JsonAdapter<Boolean> booleanAdapter = CoreTypes.create(Boolean.class);
  JsonAdapter<List<Integer>> intListAdapter = CoreTypes.createList(CoreTypes.create(Integer.class));

  @Test
  void forcedWrite_nullObject() throws IOException {
    StringWriter sw = new StringWriter();
    JsonWriter writer = stream.writer(sw);

    writer.beginObject();
    writer.name("true");
    booleanAdapter.toJson(writer, true);
    writer.forceSerialize();
    writer.name("forcedNull");
    booleanAdapter.toJson(writer, null);
    writer.name("skippedNull");
    booleanAdapter.toJson(writer, null);
    writer.endObject();

    writer.close();
    String asJson = sw.toString();
    sw.close();

    assertThat(asJson).isEqualTo("{\"true\":true,\"forcedNull\":null}");
  }

  @Test
  void forcedWrite_emptyList() throws IOException {
    StringWriter sw = new StringWriter();
    JsonWriter writer = stream.writer(sw);

    writer.beginObject();
    writer.name("contents");
    intListAdapter.toJson(writer, List.of(2, 3));
    writer.forceSerialize();
    writer.name("forcedEmpty");
    intListAdapter.toJson(writer, List.of());
    writer.name("skippedEmpty");
    intListAdapter.toJson(writer, List.of());
    writer.endObject();

    writer.close();
    String asJson = sw.toString();
    sw.close();

    assertThat(asJson).isEqualTo("{\"contents\":[2,3],\"forcedEmpty\":[]}");
  }
}
