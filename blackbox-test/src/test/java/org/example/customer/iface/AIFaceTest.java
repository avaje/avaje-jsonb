package org.example.customer.iface;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.JsonView;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AIFaceTest {

  Jsonb jsonb = Jsonb.builder().build();

  record Foo(String one, long two) implements AIFace {
  }

  @Test
  void toJson() {
    JsonType<AIFace> type = jsonb.type(AIFace.class);

    String asJson = type.toJson(new Foo("a", 42));
    assertThat(asJson).isEqualTo("{\"one\":\"a\",\"two\":42}");
  }

  @Test
  void toJsonView() {
    JsonType<AIFace> type = jsonb.type(AIFace.class);

    JsonView<AIFace> view0 = type.view("(one)");
    String asJson = view0.toJson(new Foo("a", 42));
    assertThat(asJson).isEqualTo("{\"one\":\"a\"}");

    JsonView<AIFace> view1 = type.view("(one,two)");
    assertThat(view1.toJson(new Foo("a", 42))).isEqualTo("{\"one\":\"a\",\"two\":42}");

    JsonView<AIFace> view2 = type.view("(two)");
    assertThat(view2.toJson(new Foo("a", 42))).isEqualTo("{\"two\":42}");
  }

}
