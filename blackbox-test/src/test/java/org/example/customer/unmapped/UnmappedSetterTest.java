package org.example.customer.unmapped;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UnmappedSetterTest {

  Jsonb jsonb = Jsonb.builder().build();
  JsonType<UnmappedSetter> jsonType = jsonb.type(UnmappedSetter.class);

  @Test
  void toJson_unmappedSetter_includedInJson() {
    Map<String, Object> extra = new LinkedHashMap<>();
    extra.put("xone", 57);
    extra.put("xtwo", "hello");

    UnmappedSetter bean = new UnmappedSetter().id(42).name("foo").extra(extra);

    String asJson = jsonType.toJson(bean);

    assertThat(asJson).isEqualTo("{\"id\":42,\"name\":\"foo\",\"xone\":57,\"xtwo\":\"hello\"}");
  }

  @Test
  void fromJson_unmappedSetter_unknownFieldsCollected() {
    String jsonContent = "{\"id\":42,\"name\":\"foo\",\"xone\":57,\"xtwo\":\"hello\"}";

    UnmappedSetter bean = jsonType.fromJson(jsonContent);

    assertThat(bean.id()).isEqualTo(42L);
    assertThat(bean.name()).isEqualTo("foo");
    assertThat(bean.extra()).containsKeys("xone", "xtwo");
    assertThat(bean.extra()).containsEntry("xone", 57L);
    assertThat(bean.extra()).containsEntry("xtwo", "hello");
  }
}
