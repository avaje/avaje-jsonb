package org.example.customer;

import io.avaje.jsonb.Json;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class FromObjectNestedTest {

  @Json
  public record Inner(String v) {}

  @Json
  public record InnerThenAfter(Inner first, String after) {}

  @Json
  public record MapThenAfter(Map<String, Inner> m, String after) {}

  private final Jsonb jsonb = Jsonb.builder().build();

  @Test
  void listOfLists_fromObject() {
    JsonType<List<List<Integer>>> type = jsonb.type(Types.listOf(Types.listOf(Integer.class)));

    List<List<Integer>> value = List.of(List.of(1, 2), List.of(3), List.of(4));
    assertThat(type.fromObject(value)).isEqualTo(value);
  }

  @Test
  void listOfLists_emptyInnerList_fromObject() {
    JsonType<List<List<Integer>>> type = jsonb.type(Types.listOf(Types.listOf(Integer.class)));

    List<List<Integer>> value = List.of(List.of(1), List.of(), List.of(2));
    assertThat(type.fromObject(value)).isEqualTo(value);
  }

  @Test
  void mapOfMaps_emptyInnerMap_fromObject() {
    JsonType<Map<String, Map<String, Integer>>> type = jsonb.type(Types.mapOf(Types.mapOf(Integer.class)));

    Map<String, Map<String, Integer>> value = new LinkedHashMap<>();
    value.put("a", Map.of());
    value.put("b", Map.of("x", 1));
    assertThat(type.fromObject(value)).isEqualTo(value);
  }

  @Test
  void mapOfMaps_fromObject() {
    JsonType<Map<String, Map<String, Integer>>> type = jsonb.type(Types.mapOf(Types.mapOf(Integer.class)));

    Map<String, Map<String, Integer>> value = new LinkedHashMap<>();
    value.put("a", Map.of("x", 1));
    value.put("b", Map.of("y", 2));
    assertThat(type.fromObject(value)).isEqualTo(value);
  }

  @Test
  void listOfMaps_and_mapOfLists_fromObject() {
    JsonType<List<Map<String, Integer>>> listOfMaps = jsonb.type(Types.listOf(Types.mapOf(Integer.class)));
    List<Map<String, Integer>> list = List.of(Map.of("a", 1), Map.of("b", 2));
    assertThat(listOfMaps.fromObject(list)).isEqualTo(list);

    JsonType<Map<String, List<Integer>>> mapOfLists = jsonb.type(Types.mapOf(Types.listOf(Integer.class)));
    Map<String, List<Integer>> map = new LinkedHashMap<>();
    map.put("a", List.of(1, 2));
    map.put("b", List.of(3));
    assertThat(mapOfLists.fromObject(map)).isEqualTo(map);
  }

  @Test
  void recordThenProperty_fromObject() {
    JsonType<InnerThenAfter> type = jsonb.type(InnerThenAfter.class);

    Map<String, Object> value = new LinkedHashMap<>();
    value.put("first", Map.of("v", "1"));
    value.put("after", "x");
    assertThat(type.fromObject(value)).isEqualTo(new InnerThenAfter(new Inner("1"), "x"));
  }

  @Test
  void mapThenProperty_fromObject() {
    JsonType<MapThenAfter> type = jsonb.type(MapThenAfter.class);

    Map<String, Object> m = new LinkedHashMap<>();
    m.put("a", Map.of("v", "1"));
    m.put("b", Map.of("v", "2"));
    Map<String, Object> value = new LinkedHashMap<>();
    value.put("m", m);
    value.put("after", "x");
    assertThat(type.fromObject(value))
      .isEqualTo(new MapThenAfter(Map.of("a", new Inner("1"), "b", new Inner("2")), "x"));
  }

  @Test
  void fromObject_sameAsFromJson() {
    JsonType<MapThenAfter> type = jsonb.type(MapThenAfter.class);
    MapThenAfter value = new MapThenAfter(Map.of("a", new Inner("1"), "b", new Inner("2")), "x");

    Object tree = jsonb.type(Object.class).fromJson(type.toJson(value));
    assertThat(type.fromObject(tree)).isEqualTo(value);
  }
}
