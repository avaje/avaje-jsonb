package org.example.customer.inline;

import static org.assertj.core.api.Assertions.assertThat;

import org.example.customer.Address;
import org.example.customer.mixin.CrewMate;
import org.junit.jupiter.api.Test;

import io.avaje.jsonb.Jsonb;

class InlineTest {

  Jsonb jsonb = Jsonb.builder().build();

  @Test
  void toJsonFromJson() {

    final var bean =
        new ValueInline(
            69,
            new ValueInline.Nested(2),
            new ValueInline.Nested2(3, "discarded"),
            new ValueInline.Nested3(new ValueInline.Nested2(2, "discarded")),
            new ValueInline.Nested4(new Address((long) 123, "avenue")));

    final var asJson = jsonb.toJson(bean);
    assertThat(asJson).isEqualTo("{\"a\":69,\"b\":\"hmm\",\"nested\":2,\"nested2\":3}");

    final var fromJson = jsonb.type(ValueInline.class).fromJson(asJson);

    assertThat(fromJson.nested().nestB()).isEqualTo("idk");
    assertThat(fromJson.nested2().nestB()).isEqualTo("somethin");
  }
}
