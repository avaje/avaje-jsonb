package org.example.customer.jaxb;

import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MyJaxbTypeTest {
  Jsonb jsonb = Jsonb.builder().build();

  @Test
  void toJsonFromJson() {
    final var bean = new MyJaxbType();
    bean.setName("red");
    bean.getTags().add("a");
    bean.getTags().add("b");
    bean.getTags2().add("i");
    bean.getTags2().add("j");
    bean.getTags3().add(50L);
    bean.getTags3().add(51L);
    bean.getTags3().add(52L);

    final var asJson = jsonb.toJson(bean);
    assertThat(asJson).isEqualTo("{\"name\":\"red\",\"tags\":[\"a\",\"b\"],\"tags2\":[\"i\",\"j\"],\"tags3\":[50,51,52]}");

    final var fromJson = jsonb.type(MyJaxbType.class).fromJson(asJson);
    assertThat(fromJson.name()).isEqualTo("red");
    assertThat(fromJson.getTags()).containsOnly("a", "b");
    assertThat(fromJson.getTags2()).containsOnly("i", "j");
    assertThat(fromJson.getTags3()).containsOnly(50L, 51L, 52L);
  }

  @Test
  void emptyList() {
    final var bean = new MyJaxbType();
    final var asJson = jsonb.toJson(bean);
    assertThat(asJson).isEqualTo("{\"tags\":[],\"tags2\":[],\"tags3\":[]}");

    final var fromJson = jsonb.type(MyJaxbType.class).fromJson("{}");
    assertThat(fromJson.name()).isNull();
    assertThat(fromJson.getTags()).isEmpty();
    assertThat(fromJson.getTags2()).isEmpty();
    assertThat(fromJson.getTags3()).isEmpty();


    final var fromJson2 = jsonb.type(MyJaxbType.class).fromJson("{\"tags\":[],\"tags2\":[],\"tags3\":[]}");
    assertThat(fromJson2.name()).isNull();
    assertThat(fromJson2.getTags()).isEmpty();
    assertThat(fromJson2.getTags2()).isEmpty();
    assertThat(fromJson2.getTags3()).isEmpty();
  }

  @Test
  void nullList() {
    final var fromJson = jsonb.type(MyJaxbType.class).fromJson("{\"tags\":null,\"tags2\":[],\"name\":\"red\"}");
    assertThat(fromJson.name()).isEqualTo("red");
    assertThat(fromJson.getTags()).isEmpty();
    assertThat(fromJson.getTags2()).isEmpty();
    assertThat(fromJson.getTags3()).isEmpty();
  }
}
