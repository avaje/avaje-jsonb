package org.example.customer.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumMap;
import java.util.Map;

import org.example.customer.enums.EnumExample.Thing;
import org.junit.jupiter.api.Test;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

class EnumExampleTest {

  final JsonType<EnumExample> jsonb = Jsonb.builder().build().type(EnumExample.class);

  @Test
  void to_From_Json() {
    final EnumMap<Thing, String> map = new EnumMap<>(EnumExample.Thing.class);
    map.put(EnumExample.Thing.ONE, "two");
    final var bean = new EnumExample("name", map);
    final var str = jsonb.toJson(bean);
    assertThat(str).isEqualTo("{\"name\":\"name\",\"thingMap\":{\"ONE\":\"two\"}}");

    final var staff = jsonb.fromJson(str);
    assertThat(bean).isEqualTo(staff);
  }
}
