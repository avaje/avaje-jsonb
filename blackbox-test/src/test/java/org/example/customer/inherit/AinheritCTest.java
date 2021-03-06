package org.example.customer.inherit;

import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AinheritCTest {

  private final Jsonb jsonb = Jsonb.builder().build();

  @Test
  void toFromJson()  {

    AinheritC c = new AinheritC("foo");
    c.levelA = "aval";
    c.levelB = "bval";

    var jsonType = jsonb.type(AinheritC.class);
    String asJson = jsonType.toJson(c);

    AinheritC fromJson = jsonType.fromJson(asJson);

    assertEquals(c.levelC(), fromJson.levelC());
    assertEquals(c.levelA, fromJson.levelA);
    assertEquals(c.levelB, fromJson.levelB);
  }
}
