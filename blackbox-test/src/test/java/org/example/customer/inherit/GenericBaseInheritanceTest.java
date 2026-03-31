package org.example.customer.inherit;

import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GenericBaseInheritanceTest {

  private final Jsonb jsonb = Jsonb.builder().build();

  @Test
  void toFromJson() {
    var config = new ConcreteWithGenericBase.InnerConfig("hello");
    var bean = new ConcreteWithGenericBase(config);

    var jsonType = jsonb.type(ConcreteWithGenericBase.class);
    String asJson = jsonType.toJson(bean);

    assertThat(asJson).isEqualTo("{\"config\":{\"value\":\"hello\"}}");

    ConcreteWithGenericBase fromJson = jsonType.fromJson(asJson);
    assertThat(fromJson.getConfig().getValue()).isEqualTo("hello");
  }
}
