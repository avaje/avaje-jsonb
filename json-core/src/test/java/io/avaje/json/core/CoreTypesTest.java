package io.avaje.json.core;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.stream.BufferedJsonWriter;
import io.avaje.json.stream.JsonStream;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CoreTypesTest {

  static final JsonStream stream = JsonStream.builder().build();

  @Test
  void mapOfScalar() {
    JsonAdapter<Long> longAdapter = CoreTypes.create(Long.class);
    JsonAdapter<Map<String, Long>> mapAdapter = CoreTypes.createMap(longAdapter);

    Map<String, Long> map = new LinkedHashMap<>();
    map.put("one", 45L);
    map.put("two", 93L);

    BufferedJsonWriter writer = stream.bufferedWriter();
    mapAdapter.toJson(writer, map);
    String asJson = writer.result();
    assertThat(asJson).isEqualTo("{\"one\":45,\"two\":93}");

    JsonReader reader = stream.reader(asJson);
    Map<String, Long> fromJsonMap = mapAdapter.fromJson(reader);

    assertThat(fromJsonMap).containsOnlyKeys("one", "two");
  }
}
