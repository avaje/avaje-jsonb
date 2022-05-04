package org.example.customer;

import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SomePrimitiveTypesTest {

  @Test
  void toJsonFromJson_primitives() {

    SomePrimitiveTypes bean = new SomePrimitiveTypes(12, 13L, true, 14D, 'x', (byte) 43, (short) 15);

    Jsonb jsonb = Jsonb.builder().build();
    var jsonType = jsonb.type(SomePrimitiveTypes.class);
    String asJson = jsonType.toJson(bean);
    assertEquals("{\"f0\":12,\"f1\":13,\"f2\":true,\"f3\":14.0,\"f4\":\"x\",\"f5\":43,\"f6\":15}",asJson);

    SomePrimitiveTypes beanFromJson = jsonType.fromJson(asJson);

    assertEquals(12, beanFromJson.getF0());
    assertEquals(13L, beanFromJson.getF1());
    assertTrue(beanFromJson.isF2());
    assertEquals(14D, beanFromJson.getF3());
    assertEquals('x', beanFromJson.getF4());
    assertEquals((byte)43, beanFromJson.getF5());
    assertEquals((short)15, beanFromJson.getF6());

  }
}
