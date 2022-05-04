package org.example.customer.nesting;

import io.avaje.jsonb.Jsonb;
import org.example.customer.nesting.Nesting.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NestingTest {

  Jsonb jsonb = Jsonb.builder().build();

  @Test
  void asd() {

    var one = new One("hi",
      List.of(new Two(44, new Four("f1"), "g1"), new Two(45, new Four("f2"), "g2")),
      89,
      new Three("ot"), 909L);

    String asJson = jsonb.toJson(one);

    assertThat(asJson).isEqualTo("{\"a\":\"hi\",\"twos\":[{\"f\":44,\"four\":{\"four\":\"f1\"},\"g\":\"g1\"},{\"f\":45,\"four\":{\"four\":\"f2\"},\"g\":\"g2\"}],\"b\":89,\"three\":{\"other\":\"ot\"},\"c\":909}");


    One fromJson = jsonb.type(One.class).fromJson(asJson);

    assertThat(fromJson).isEqualTo(one);
  }
}
