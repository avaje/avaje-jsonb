package io.avaje.json.node.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import io.avaje.json.node.JsonObject;
import io.avaje.jsonb.Jsonb;

class NullJsonObjectTest {
  static class WrapJsonObject {
    public JsonObject json;
  }

  @Test
  void test() {
    var b = Jsonb.builder();
    new JsonNodeComponent().register(b);
    var type =
        b.add(WrapJsonObject.class, WrapJsonObjectJsonAdapter::new)
            .build()
            .type(WrapJsonObject.class);
    WrapJsonObject fromJson = type.fromJson("{\"json\":null}");
    assertNull(fromJson.json);
    assertEquals("{}", type.toJson(fromJson));
  }
}
