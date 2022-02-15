package org.example.customer;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NestedTest {

  @Test
  void toJson()  {

    Nested.MyNest bean = new Nested.MyNest(42, "one");

    Jsonb jsonb = Jsonb.newBuilder().build();

    JsonType<Nested.MyNest> mixedAdapter = jsonb.type(Nested.MyNest.class);
    String asJson = mixedAdapter.toJson(bean);

    Nested.MyNest fromJson = mixedAdapter.fromJson(asJson);

    assertEquals(bean, fromJson);
  }
}
