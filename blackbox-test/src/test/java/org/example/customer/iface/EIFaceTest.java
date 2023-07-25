package org.example.customer.iface;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.JsonView;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EIFaceTest {

  Jsonb jsonb = Jsonb.builder().build();

  record Foo(String one, long two) implements EIFace {
  }

  @Test
  void toJson() {
    JsonType<EIFace> type = jsonb.type(EIFace.class);

    String asJson = type.toJson(new Foo("a", 42));
    assertThat(asJson).isEqualTo("{\"one\":\"a\",\"two\":42}");
  }

  @Test
  void toJsonView() {
    JsonType<EIFace> type = jsonb.type(EIFace.class);

    JsonView<EIFace> view0 = type.view("(one)");
    String asJson = view0.toJson(new Foo("a", 42));
    assertThat(asJson).isEqualTo("{\"one\":\"a\"}");

    JsonView<EIFace> view1 = type.view("(one,two)");
    assertThat(view1.toJson(new Foo("a", 42))).isEqualTo("{\"one\":\"a\",\"two\":42}");

    JsonView<EIFace> view2 = type.view("(two)");
    assertThat(view2.toJson(new Foo("a", 42))).isEqualTo("{\"two\":42}");
  }

  @Test
  void fromJson_expect_UnsupportedOperationException() {
    JsonType<EIFace> type = jsonb.type(EIFace.class);
    assertThatThrownBy(() -> {
      type.fromJson("{\"one\":\"a\",\"two\":42}");
    }).isInstanceOf(UnsupportedOperationException.class);
  }
}
