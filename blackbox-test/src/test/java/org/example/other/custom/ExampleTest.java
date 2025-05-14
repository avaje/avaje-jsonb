package org.example.other.custom;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExampleTest {

  Jsonb jsonb = Jsonb.builder().build();
  JsonType<Example> jsonType = jsonb.type(Example.class);

  @Test
  void toFromJson() {
    Example bean = new Example();
    bean.setCode(34);
    bean.setMap(new WrapMap(Map.of("a", "b")));
    bean.setMap2(List.of(new WrapMap2(Map.of("2a", "z")), new WrapMap2(Map.of("2b", "x"))));

    final String asJson = jsonType.toJson(bean);
    assertThat(asJson).isEqualTo("{\"code\":34,\"map\":{\"a\":\"b\"},\"map2\":[{\"2a\":\"z\"},{\"2b\":\"x\"}]}");

    final var fromJson = jsonType.fromJson(asJson);
    assertThat(fromJson.getCode()).isEqualTo(34);
    assertThat(fromJson.getMap()).containsEntry("a", "b");
    assertThat(fromJson.getMap2()).hasSize(2);
    assertThat(fromJson.getMap2().getFirst()).containsEntry("2a", "z");
    assertThat(fromJson.getMap2().getLast()).containsEntry("2b", "x");
  }
}
