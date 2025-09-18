package org.example.customer.stream;

import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MyLinkedHashSetTest {

  Jsonb jsonb = Jsonb.builder().build();

  @Test
  void toJson_fromJson() {
    MyLinked myLinked = new MyLinked(1, new LinkedHashSet<>(List.of("a", "b", "c")));

    String json = jsonb.toJson(myLinked);
    assertNotNull(json);
    assertThat(json).isEqualTo("{\"id\":1,\"names\":[\"a\",\"b\",\"c\"]}");

    MyLinked fromJson = jsonb.type(MyLinked.class).fromJson(json);
    assertEquals(myLinked, fromJson);
  }
}
