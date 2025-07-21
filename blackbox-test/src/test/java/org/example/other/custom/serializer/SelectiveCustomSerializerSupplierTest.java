package org.example.other.custom.serializer;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** Uses MoneySerializer2 which is registered via Supplier */
class SelectiveCustomSerializerSupplierTest {

  Jsonb jsonb = Jsonb.builder().build();
  JsonType<CustomExample2> jsonType = jsonb.type(CustomExample2.class);

  @Test
  void toFromJson() {
    final var bean = new CustomExample2(new BigDecimal("100.95630"), new BigDecimal("100.95630"));

    final String asJson = jsonType.toJson(bean);
    assertThat(asJson).isEqualTo("{\"amountOwed\":100.95,\"somethingElse\":100.95630}");

    final var fromJson = jsonType.fromJson(asJson);
    assertThat(fromJson.amountOwed()).isEqualTo(new BigDecimal("100.95"));
    assertThat(fromJson.somethingElse()).isEqualTo(new BigDecimal("100.95630"));
    assertThat(fromJson).isNotEqualTo(bean);
  }

  @Test
  void toFromJson_with_null() {
    final var bean = new CustomExample2(null, null);

    final String asJson = jsonType.toJson(bean);
    assertThat(asJson).isEqualTo("{}");

    final var fromJson = jsonType.fromJson(asJson);
    assertThat(fromJson.amountOwed()).isNull();
    assertThat(fromJson.somethingElse()).isNull();
    assertThat(fromJson).isEqualTo(bean);
  }

}
