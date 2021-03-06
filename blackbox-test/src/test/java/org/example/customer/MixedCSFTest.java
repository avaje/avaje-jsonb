package org.example.customer;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MixedCSFTest {

  @Test
  void toJson()  {

    MixedCSF mixed = new MixedCSF("one");
    mixed.two(42L);
    mixed.setThree(33L);
    mixed.four = 44L;

    Jsonb jsonb = Jsonb.builder()
      //.add(MixedCSF.class, MixedCSFJsonAdapter::new)
      .build();

    JsonType<MixedCSF> mixedAdapter = jsonb.type(MixedCSF.class);
    String asJson = mixedAdapter.toJson(mixed);

    MixedCSF mixedFromJson = mixedAdapter.fromJson(asJson);

    assertEquals(42L, mixedFromJson.getTwo());
  }
}
