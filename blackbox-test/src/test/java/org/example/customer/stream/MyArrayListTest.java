package org.example.customer.stream;

import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MyArrayListTest {

  Jsonb jsonb = Jsonb.builder().build();

  @Test
  void toJson_fromJson() {
    MyArrayList myLinked = new MyArrayList(1, new ArrayList<>(List.of("a", "b", "c")));

    String json = jsonb.toJson(myLinked);
    assertNotNull(json);
    assertThat(json).isEqualTo("{\"id\":1,\"names\":[\"a\",\"b\",\"c\"]}");

    MyArrayList fromJson = jsonb.type(MyArrayList.class).fromJson(json);
    assertEquals(myLinked, fromJson);
  }
}
