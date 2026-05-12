package org.example.customer.setter;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PropertySetterTest {

  Jsonb jsonb = Jsonb.builder().build();
  JsonType<PropertySetter> jsonType = jsonb.type(PropertySetter.class);

  @Test
  void toJson_usesRenamedProperty() {
    PropertySetter bean = new PropertySetter().id(1).name("Rob");

    String asJson = jsonType.toJson(bean);

    assertThat(asJson).isEqualTo("{\"id\":1,\"full_name\":\"Rob\"}");
  }

  @Test
  void fromJson_usesRenamedProperty() {
    PropertySetter bean = jsonType.fromJson("{\"id\":1,\"full_name\":\"Rob\"}");

    assertThat(bean.id()).isEqualTo(1L);
    assertThat(bean.name()).isEqualTo("Rob");
  }
}
