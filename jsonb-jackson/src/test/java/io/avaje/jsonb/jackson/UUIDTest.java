package io.avaje.jsonb.jackson;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UUIDTest {

  Jsonb jsonb = Jsonb.newBuilder().build();

  @Test
  void toJson_fromJson() {

    UUID val = UUID.randomUUID();
    JsonType<UUID> type = jsonb.type(UUID.class);
    String asJson = type.toJson(val);
    assertThat(asJson).isEqualTo("\"" + val + "\"");

    UUID fromJson = type.list().fromJson("[\"" + val + "\"]").get(0);
    assertThat(fromJson).isEqualTo(val);
  }

  @Test
  void asMap_toFromJson() {

    UUID v0 = UUID.randomUUID();
    UUID v1 = UUID.randomUUID();
    Map<String, UUID> map = new LinkedHashMap<>();
    map.put("k0", v0);
    map.put("k1", v1);

    Jsonb jsonb = Jsonb.newBuilder().build();

    JsonType<Map<String,UUID>> mapUidType = jsonb.type(Types.mapOf(UUID.class));
    String asJson = mapUidType.toJson(map);

    Map<String, UUID> fromJson = mapUidType.fromJson(asJson);

    assertThat(fromJson).containsKeys("k0", "k1");
    assertThat(fromJson.get("k0")).isEqualTo(v0);
    assertThat(fromJson.get("k1")).isEqualTo(v1);
  }

}
