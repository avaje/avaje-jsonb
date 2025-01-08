package io.avaje.json.simple;


import io.avaje.json.JsonReader;
import io.avaje.json.core.UtilList;
import io.avaje.json.core.UtilMap;
import io.avaje.json.stream.BufferedJsonWriter;
import io.avaje.json.stream.JsonStream;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleMapperTest {

  static final SimpleMapper simpleMapper = SimpleMapper.builder().build();

  @Test
  void mapToJsonFromJson() {

    Map<String, Long> map = new LinkedHashMap<>();
    map.put("one", 45L);
    map.put("two", 93L);

    String asJson = simpleMapper.toJson(map);
    assertThat(asJson).isEqualTo("{\"one\":45,\"two\":93}");

    Map<String, Object> mapFromJson = simpleMapper.fromJsonObject(asJson);

    assertThat(mapFromJson).containsKeys("one", "two");
    assertThat(mapFromJson.toString()).isEqualTo("{one=45, two=93}");

    Map<String, Object> mapFromJson2 = simpleMapper.map().fromJson(asJson);
    assertThat(mapFromJson2).isEqualTo(mapFromJson);

    JsonStream jsonStream = JsonStream.builder().build();
    try (JsonReader reader = jsonStream.reader(asJson)) {
      Map<String, Object> mapFromJson3 = simpleMapper.fromJsonObject(reader);
      assertThat(mapFromJson3).isEqualTo(mapFromJson);
    }
  }

  @Test
  void toJsonWriter_scalar() {
    JsonStream jsonStream = JsonStream.builder().build();
    BufferedJsonWriter writer0 = jsonStream.bufferedWriter();
    simpleMapper.toJson("hi", writer0);
    assertThat(writer0.result()).isEqualTo("\"hi\"");
  }

  @Test
  void toJsonWriter_map() {
    JsonStream jsonStream = JsonStream.builder().build();
    BufferedJsonWriter writer0 = jsonStream.bufferedWriter();
    simpleMapper.toJson(UtilMap.of("key", 0), writer0);
    assertThat(writer0.result()).isEqualTo("{\"key\":0}");
  }

  @Test
  void toJsonWriter_list() {
    JsonStream jsonStream = JsonStream.builder().build();
    BufferedJsonWriter writer0 = jsonStream.bufferedWriter();
    simpleMapper.toJson(UtilList.of("a", 0), writer0);
    assertThat(writer0.result()).isEqualTo("[\"a\",0]");
  }

  @Test
  void nullDirectly() {
    Object mapFromJson = simpleMapper.fromJson("null");
    assertThat(mapFromJson).isNull();
  }

  @Test
  void objectJsonReader() {
    try (JsonReader reader = JsonStream.builder().build().reader("\"hi\"")) {
      Object fromJson = simpleMapper.fromJson(reader);
      assertThat(fromJson).isEqualTo("hi");
    }
  }

  @Test
  void mapWithNull() {
    Map<String, Object> mapFromJson = simpleMapper.fromJsonObject("{\"one\":1,\"two\":null,\"three\":3}");

    assertThat(mapFromJson).containsKeys("one", "two", "three");
    assertThat(mapFromJson.toString()).isEqualTo("{one=1, two=null, three=3}");

    assertThat(simpleMapper.toJson(mapFromJson)).isEqualTo("{\"one\":1,\"three\":3}");
  }

  @Test
  void listWithNull() {
    List<Object> listFromJson = simpleMapper.fromJsonArray("[1,null,3]");

    assertThat(listFromJson).hasSize(3);
    assertThat(listFromJson.get(1)).isNull();

    assertThat(simpleMapper.toJson(listFromJson)).isEqualTo("[1,3]");
  }

  @Test
  void listWithReader() {
    try (JsonReader reader = JsonStream.builder().build().reader("[1,2]")) {
      List<Object> listFromJson = simpleMapper.fromJsonArray(reader);

      assertThat(listFromJson).hasSize(2);
      assertThat(simpleMapper.toJson(listFromJson)).isEqualTo("[1,2]");
    }
  }

  @Test
  void arrayToJsonFromJson() {

    Map<String, Long> map0 = new LinkedHashMap<>();
    map0.put("one", 45L);
    map0.put("two", 93L);

    Map<String, Long> map1 = new LinkedHashMap<>();
    map1.put("one", 27L);

    List<Map<String, Long>> list = UtilList.of(map0, map1);

    String asJson = simpleMapper.toJson(list);
    assertThat(asJson).isEqualTo("[{\"one\":45,\"two\":93},{\"one\":27}]");

    List<Object> listFromJson = simpleMapper.fromJsonArray(asJson);

    assertThat(listFromJson).hasSize(2);
    assertThat(listFromJson.toString()).isEqualTo("[{one=45, two=93}, {one=27}]");

    List<Object> list2 = simpleMapper.list().fromJson(asJson);
    assertThat(list2).isEqualTo(listFromJson);
  }
}
