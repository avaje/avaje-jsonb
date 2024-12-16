package io.avaje.json.simple;


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
  }

  @Test
  void nullDirectly() {
   var mapFromJson = simpleMapper.fromJson("null");
    assertThat(mapFromJson).isNull();
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
  void arrayToJsonFromJson() {

    Map<String, Long> map0 = new LinkedHashMap<>();
    map0.put("one", 45L);
    map0.put("two", 93L);

    Map<String, Long> map1 = new LinkedHashMap<>();
    map1.put("one", 27L);

    List<Map<String, Long>> list = List.of(map0, map1);

    String asJson = simpleMapper.toJson(list);
    assertThat(asJson).isEqualTo("[{\"one\":45,\"two\":93},{\"one\":27}]");

    List<Object> listFromJson = simpleMapper.fromJsonArray(asJson);

    assertThat(listFromJson).hasSize(2);
    assertThat(listFromJson.toString()).isEqualTo("[{one=45, two=93}, {one=27}]");

    List<Object> list2 = simpleMapper.list().fromJson(asJson);
    assertThat(list2).isEqualTo(listFromJson);
  }
}
