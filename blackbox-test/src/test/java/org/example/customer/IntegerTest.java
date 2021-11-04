package org.example.customer;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IntegerTest {

  private final Jsonb jsonb = Jsonb.newBuilder().build();

  @Test
  void fromObject() throws IOException {

    JsonType<Integer> intJson = jsonb.type(Integer.TYPE);

    assertThat(intJson.fromObject("42")).isEqualTo(42);
    assertThat(intJson.fromObject(42)).isEqualTo(42);
    assertThat(intJson.fromObject(42L)).isEqualTo(42);
  }

  @Test
  void list_fromObject() throws IOException {

    JsonType<Integer> integerJson = jsonb.type(Integer.class);
    JsonType<List<Integer>> integerListJson = integerJson.list();

    List<Integer> integers = integerListJson.fromObject(Arrays.asList(41, 42));
    assertThat(integers).containsExactly(41, 42);
  }
}
