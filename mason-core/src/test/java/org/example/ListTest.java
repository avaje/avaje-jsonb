package org.example;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.base.Types;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ListTest {

  @Test
  void simpleList() throws IOException {

    Jsonb jsonb = Jsonb.newBuilder().build();

    ParameterizedType listOfString = Types.listOf(String.class);
    JsonType<List<String>> listOfStringType = jsonb.type(listOfString);

    List<String> data = Arrays.asList("one", "two", "three");
    String asJson = listOfStringType.toJson(data);

    assertThat(asJson).isEqualTo("[\"one\",\"two\",\"three\"]");

    List<String> fromJson = listOfStringType.fromJson(asJson);
    assertThat(fromJson).contains("one", "two", "three");
  }

  @Test
  void adapter_list() throws IOException {

    Jsonb jsonb = Jsonb.newBuilder().build();

    JsonType<String> stringType = jsonb.type(String.class);
    JsonType<List<String>> list = stringType.list();

    List<String> data = Arrays.asList("one", "two", "three");
    String asJson = list.toJson(data);

    assertThat(asJson).isEqualTo("[\"one\",\"two\",\"three\"]");

    List<String> fromJson = list.fromJson(asJson);
    assertThat(fromJson).contains("one", "two", "three");
  }
}
