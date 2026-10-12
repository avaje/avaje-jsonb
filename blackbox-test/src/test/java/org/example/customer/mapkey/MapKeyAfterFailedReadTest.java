package org.example.customer.mapkey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.avaje.json.JsonException;
import io.avaje.jsonb.Json;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;

public class MapKeyAfterFailedReadTest {

  @Json
  public record MapHolder(Map<String, Integer> m, String after) {}

  final Jsonb jsonb = Jsonb.builder().build();

  @Test
  void fromJson_escapedKeys_topLevelMap_afterReadFailedInsideRecord() {
    JsonType<MapHolder> holderType = jsonb.type(MapHolder.class);
    JsonType<Map<String, Integer>> mapType = jsonb.type(Types.mapOf(Integer.class));

    String escapedKeys = "{\"b\\\"c\":1,\"d\\\\e\":2}";
    assertThat(mapType.fromJson(escapedKeys)).containsOnlyKeys("b\"c", "d\\e");

    assertThatThrownBy(() -> holderType.fromJson("{\"m\":{\"a\":1"))
      .isInstanceOf(JsonException.class);

    // same thread, so the same recycled parser
    assertThat(mapType.fromJson(escapedKeys)).containsOnlyKeys("b\"c", "d\\e");
  }
}
