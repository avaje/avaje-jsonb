package io.avaje.json.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.stream.BufferedJsonWriter;
import io.avaje.json.stream.JsonStream;

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

  @Test
  void nullMap() {
    JsonAdapter<Long> longAdapter = CoreTypes.create(Long.class);
    JsonAdapter<Map<String, Long>> mapAdapter = CoreTypes.createMap(longAdapter);

    BufferedJsonWriter writer = stream.bufferedWriter();
    mapAdapter.toJson(writer, null);
    String asJson = writer.result();
    assertThat(asJson).isEmpty();
  }

  @Test
  void listOfScalar() {
    JsonAdapter<Long> longAdapter = CoreTypes.create(Long.class);
    JsonAdapter<List<Long>> listAdapter = CoreTypes.createList(longAdapter);

    List<Long> vals = List.of(54L, 21L, 63L);

    BufferedJsonWriter writer = stream.bufferedWriter();
    listAdapter.toJson(writer, vals);
    String asJson = writer.result();
    assertThat(asJson).isEqualTo("[54,21,63]");

    JsonReader reader = stream.reader(asJson);
    List<Long> fromJsnoList = listAdapter.fromJson(reader);

    assertThat(fromJsnoList).containsExactly(54L, 21L, 63L);
  }

  @Test
  void nullList() {
    JsonAdapter<Long> longAdapter = CoreTypes.create(Long.class);
    JsonAdapter<List<Long>> listAdapter = CoreTypes.createList(longAdapter);

    BufferedJsonWriter writer = stream.bufferedWriter();
    listAdapter.toJson(writer, null);
    String asJson = writer.result();
    assertThat(asJson).isEmpty();
  }

  @SuppressWarnings("unchecked")
  @Test
  void createCoreAdapters() {
    CoreTypes.CoreAdapters coreAdapters = CoreTypes.createCoreAdapters();
    JsonAdapter<Object> basicObject = coreAdapters.objectAdapter();

    Map<String, Object> inner = new LinkedHashMap<>();
    inner.put("nm", "r");
    inner.put("va", 56L);

    Map<String, Object> map = new LinkedHashMap<>();
    map.put("one", 45L);
    map.put("two", List.of(45, 46));
    map.put("three", inner);


    BufferedJsonWriter writer = stream.bufferedWriter();
    basicObject.toJson(writer, map);
    String asJson = writer.result();
    assertThat(asJson).isEqualTo("{\"one\":45,\"two\":[45,46],\"three\":{\"nm\":\"r\",\"va\":56}}");

    JsonReader reader = stream.reader(asJson);
    Map<String, Object> resultMap = (Map<String, Object>)basicObject.fromJson(reader);
    assertThat(resultMap.get("one")).isEqualTo(45L);
    assertThat(resultMap.get("two")).isEqualTo(List.of(45L, 46L));
    assertThat(resultMap.get("three")).isInstanceOf(Map.class);
    assertThat(resultMap.get("three")).isEqualTo(inner);
  }
}
