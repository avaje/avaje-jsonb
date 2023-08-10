package org.example.customer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.Jsonb;

class PrettyTest {

  Jsonb jsonb = Jsonb.builder().serializeEmpty(false).build();

  @Test
  void anyToJsonPretty() {
    final var customer = new PrettyExample("test", List.of("one", "two"), 2);
    final String asJson = jsonb.toJsonPretty(customer);
    assertThat(asJson).isEqualTo("""
    {
      "name": "test",
      "values": [
        "one",
        "two"
      ],
      "count": 2
    }""");
  }
}
