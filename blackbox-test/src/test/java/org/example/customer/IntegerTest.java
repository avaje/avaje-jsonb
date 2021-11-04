package org.example.customer;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

  @Test
  void set_toFromJson() throws IOException {

    JsonType<Integer> integerJson = jsonb.type(Integer.class);
    JsonType<Set<Integer>> integerSetJson = integerJson.set();

    Set<Integer> integers = new LinkedHashSet<>();
    integers.add(52);
    integers.add(55);

    String asJson = integerSetJson.toJson(integers);
    assertThat(asJson).isEqualTo("[52,55]");

    Set<Integer> fromJson = integerSetJson.fromJson(asJson);
    assertThat(fromJson).containsExactly(52, 55);
  }

  @Test
  void set_fromObject() throws IOException {

    JsonType<Set<Integer>> integerSetJson = jsonb.type(Integer.class).set();

    Set<Integer> integers = integerSetJson.fromObject(Arrays.asList(41, 42));
    assertThat(integers).containsExactly(41, 42);
  }

  @Test
  void set_viaTypes() throws IOException {

    JsonType<Set<Integer>> integerSetJson = jsonb.type(Types.setOf(Integer.class));

    Set<Integer> integers = integerSetJson.fromObject(Arrays.asList(41, 42));
    assertThat(integers).containsExactly(41, 42);
  }
}
