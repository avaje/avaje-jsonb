package org.example.customer.value;

import io.avaje.jsonb.Jsonb;
import org.example.customer.Address;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InlineTest {

  Jsonb jsonb = Jsonb.builder().build();

  @Test
  void toJsonFromJson() {

    final var address = new Address((long) 123, "avenue");
    final var bean =
      new ValueInline(
        69,
        new ValueInline.Nested(2),
        new ValueInline.Nested2(3, "discarded"),
        new ValueInline.Nested3(new ValueInline.Nested2(2, "discarded")),
        new ValueInline.Nested4(address));

    final var asJson = jsonb.toJson(bean);
    assertThat(asJson)
      .isEqualTo(
        "{\"a\":69,\"nested\":2,\"nested2\":3,\"nested3\":2,\"complex\":{\"id\":123,\"street\":\"avenue\"}}");

    final var fromJson = jsonb.type(ValueInline.class).fromJson(asJson);

    assertThat(fromJson.nested().nestB()).isEqualTo("idk");
    assertThat(fromJson.nested2().nestB()).isEqualTo("somethin");
    var fromAddress = fromJson.complex().address();
    assertThat(fromAddress.getId()).isEqualTo(address.getId());
    assertThat(fromAddress.getStreet()).isEqualTo(address.getStreet());
  }
}
