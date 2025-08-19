package org.example.customer;

import io.avaje.json.JsonAdapter;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmptySupertypeTest {
  Jsonb jsonb = Jsonb.builder().build();
  JsonAdapter<EmptySupertype> adapter = jsonb.adapter(EmptySupertype.class);
  JsonType<EmptySupertype> type = jsonb.type(EmptySupertype.class);

  @Test
  void nullObject() {
    var object = (EmptySupertype) null;
    var expected = "";
    var actual = type.toJson(object);
    assertEquals(expected, actual);
  }

  @Test
  void objectA() {
    var object = new EmptySupertype.SubtypeA();
    var expected = "{\"@type\":\"a\"}";
    var actual = type.toJson(object);
    assertEquals(expected, actual);
  }

  @Test
  void jsonA() {
    var json = "{\"@type\":\"a\"}";
    var expected = new EmptySupertype.SubtypeA();
    var actual = type.fromJson(json);
    assertEquals(expected, actual);
  }

  @Test
  void objectB() {
    var object = new EmptySupertype.SubtypeB();
    var expected = "{\"@type\":\"b\"}";
    var actual = type.toJson(object);
    assertEquals(expected, actual);
  }

  @Test
  void jsonB() {
    var json = "{\"@type\":\"b\"}";
    var expected = new EmptySupertype.SubtypeB();
    var actual = type.fromJson(json);
    assertEquals(expected, actual);
  }

  @Test
  void invalidJsonC() {
    var json = "{\"@type\":\"c\"}";
    var expected = new EmptySupertype.SubtypeB();
    assertThrows(IllegalStateException.class, () -> type.fromJson(json));
  }
}
