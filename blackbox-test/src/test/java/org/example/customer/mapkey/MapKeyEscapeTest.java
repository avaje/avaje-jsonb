package org.example.customer.mapkey;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.Json;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;
import org.example.customer.unmapped.UnmappedOne;

public class MapKeyEscapeTest {

  @Json
  public record MapHolder(Map<String, Integer> m, String after) {}

  static final String ESCAPED_KEYS = "{\"b\\\"c\":1,\"d\\\\e\":2,\"x\\ny\":3,\"caf\\u00e9\":4}";

  final Jsonb jsonb = Jsonb.builder().build();

  @Test
  void fromJson_escapedKeys_topLevelMap() {
    JsonType<Map<String, Integer>> type = jsonb.type(Types.mapOf(Integer.class));
    assertThat(type.fromJson(ESCAPED_KEYS)).containsOnlyKeys("b\"c", "d\\e", "x\ny", "café");
  }

  @Test
  void fromJson_escapedKeys_mapInsideRecord() {
    JsonType<MapHolder> type = jsonb.type(MapHolder.class);
    MapHolder holder = type.fromJson("{\"m\":" + ESCAPED_KEYS + ",\"after\":\"x\"}");
    assertThat(holder.m()).containsOnlyKeys("b\"c", "d\\e", "x\ny", "café");
    assertThat(holder.after()).isEqualTo("x");
  }

  @Test
  void toJsonFromJson_escapedKeys_mapInsideRecord() {
    JsonType<MapHolder> type = jsonb.type(MapHolder.class);
    MapHolder holder = new MapHolder(Map.of("b\"c", 1, "d\\e", 2, "x\ny", 3), "x");
    assertThat(type.fromJson(type.toJson(holder))).isEqualTo(holder);
  }

  @Test
  void fromJson_escapedUnknownPropertyName_unmapped() {
    UnmappedOne bean = jsonb.type(UnmappedOne.class).fromJson("{\"id\":1,\"b\\\"c\":\"x\",\"name\":\"n\"}");
    assertThat(bean.unmapped()).containsOnlyKeys("b\"c");
    assertThat(bean.name()).isEqualTo("n");
  }
}
