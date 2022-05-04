package org.example.customer;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class IntegerTest {

  private final Jsonb jsonb = Jsonb.builder().build();

  @Test
  void fromObject()  {

    JsonType<Integer> intJson = jsonb.type(Integer.TYPE);

    assertThat(intJson.fromObject("42")).isEqualTo(42);
    assertThat(intJson.fromObject(42)).isEqualTo(42);
    assertThat(intJson.fromObject(42L)).isEqualTo(42);
  }

  @Test
  void list_fromObject()  {

    JsonType<Integer> integerJson = jsonb.type(Integer.class);
    JsonType<List<Integer>> integerListJson = integerJson.list();

    List<Integer> integers = integerListJson.fromObject(Arrays.asList(41, 42));
    assertThat(integers).containsExactly(41, 42);
  }

  @Test
  void set_toFromJson()  {

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
  void set_fromObject()  {

    JsonType<Set<Integer>> integerSetJson = jsonb.type(Integer.class).set();

    Set<Integer> integers = integerSetJson.fromObject(Arrays.asList(41, 42));
    assertThat(integers).containsExactly(41, 42);
  }

  @Test
  void set_viaTypes()  {

    JsonType<Set<Integer>> integerSetJson = jsonb.type(Types.setOf(Integer.class));

    Set<Integer> integers = integerSetJson.fromObject(Arrays.asList(41, 42));
    assertThat(integers).containsExactly(41, 42);
  }


  @Test
  void array_viaTypes()  {

    var integerArrayJson = jsonb.type(Types.arrayOf(Integer.class));

    String asJson = integerArrayJson.toJson(new Integer[]{41, 42});
    Integer[] fromJson = (Integer[]) integerArrayJson.fromJson(asJson);
    assertThat(fromJson).containsExactly(41, 42);
  }

  @Test
  void toJson_viaTypeObject()  {

    Set<Integer> integers = new LinkedHashSet<>();
    integers.add(52);
    integers.add(55);

    String asJson = jsonb.type(Object.class).toJson(integers);
    assertThat(asJson).isEqualTo("[52,55]");
  }
}
