package org.example.other.custom;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;

class CustomEntryJsonAdapterTest {

  Jsonb jsonb = Jsonb.builder().build();
  JsonType<Entry<String, String>> jsonType =
      jsonb.type(Types.newParameterizedType(Entry.class, String.class, String.class));

  @Test
  void toFromJson() {
    final var bean = new SimpleImmutableEntry<>("master", "sword");

    final String asJson = jsonType.toJson(bean);
    assertThat(asJson).isEqualTo("{\"key\":\"master\",\"val\":\"sword\"}");

    final var fromJson = jsonType.fromJson(asJson);
    assertThat(fromJson.getKey()).isEqualTo(bean.getKey());
    assertThat(fromJson.getValue()).isEqualTo(bean.getValue());
  }
}
