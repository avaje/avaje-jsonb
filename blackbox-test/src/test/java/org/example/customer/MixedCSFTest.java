package org.example.customer;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.example.customer.jsonb.MixedCSFJsonAdapter;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MixedCSFTest {

  @Test
  void toJson() throws IOException {

    MixedCSF mixed = new MixedCSF("one");
    mixed.two(42L);
    mixed.setThree(33L);
    mixed.four = 44L;

    Jsonb jsonb = Jsonb.newBuilder()
      .add(MixedCSF.class, MixedCSFJsonAdapter::new)
      .build();

    JsonType<MixedCSF> mixedAdapter = jsonb.type(MixedCSF.class);
    String asJson = mixedAdapter.toJson(mixed);

    MixedCSF mixedFromJson = mixedAdapter.fromJson(asJson);

    assertEquals(42L, mixedFromJson.getTwo());
  }
}
