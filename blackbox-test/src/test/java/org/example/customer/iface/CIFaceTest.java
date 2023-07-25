package org.example.customer.iface;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.JsonView;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CIFaceTest {

  Jsonb jsonb = Jsonb.builder().build();

  record Foo(String oneToBe, long twoNotHere, String hi) implements CIFace {
  }

  @Test
  void toJson() {
    JsonType<CIFace> type = jsonb.type(CIFace.class);

    String asJson = type.toJson(new Foo("a", 42, "b"));
    assertThat(asJson).isEqualTo("{\"one-to-be\":\"a\",\"two-not-here\":42,\"hi\":\"b\"}");
  }

  @Test
  void toJsonView() {
    JsonType<CIFace> type = jsonb.type(CIFace.class);

    JsonView<CIFace> view0 = type.view("(one-to-be)");
    String asJson = view0.toJson(new Foo("a", 42, "b"));
    assertThat(asJson).isEqualTo("{\"one-to-be\":\"a\"}");

    JsonView<CIFace> view1 = type.view("(one-to-be,two-not-here)");
    assertThat(view1.toJson(new Foo("a", 42, "b"))).isEqualTo("{\"one-to-be\":\"a\",\"two-not-here\":42}");

    JsonView<CIFace> view2 = type.view("(two-not-here)");
    assertThat(view2.toJson(new Foo("a", 42, "b"))).isEqualTo("{\"two-not-here\":42}");
  }

  @Test
  void fromJson_expect_UnsupportedOperationException() {
    JsonType<CIFace> type = jsonb.type(CIFace.class);
    assertThatThrownBy(() -> {
      type.fromJson("{\"one\":\"a\",\"two\":42}");
    }).isInstanceOf(UnsupportedOperationException.class);
  }

}
