package org.example.other.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.example.other.place.MyOtherClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

class CustomJsonAdapterTest {

  Jsonb jsonb = Jsonb.builder().build();
  JsonType<Entry> jsonType = jsonb.type(Entry.class);

  @Test
  void toFromJson() {

    final var bean = new SimpleImmutableEntry<>("master", "sword");

    final String asJson = jsonType.toJson(bean);

    final var fromJson = jsonType.fromJson(asJson);

    assertThat(fromJson.getKey()).isEqualTo(bean.getKey());
    assertThat(fromJson.getValue()).isEqualTo(bean.getValue());
  }
}
