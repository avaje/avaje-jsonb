package org.example.customer.iface;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.JsonView;
import io.avaje.jsonb.Jsonb;
import org.example.customer.iface.implementation.MyDIFace;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DIFaceTest {

  Jsonb jsonb = Jsonb.builder().build();

  record Foo(String one, long two) implements DIFace {
  }

  @Test
  void toJson() {
    JsonType<DIFace> type = jsonb.type(DIFace.class);

    String asJson = type.toJson(new Foo("a", 42));
    assertThat(asJson).isEqualTo("{\"one\":\"a\",\"two\":42}");
  }

  @Test
  void toJsonView() {
    JsonType<DIFace> type = jsonb.type(DIFace.class);

    JsonView<DIFace> view0 = type.view("(one)");
    String asJson = view0.toJson(new Foo("a", 42));
    assertThat(asJson).isEqualTo("{\"one\":\"a\"}");

    JsonView<DIFace> view1 = type.view("(one,two)");
    assertThat(view1.toJson(new Foo("a", 42))).isEqualTo("{\"one\":\"a\",\"two\":42}");

    JsonView<DIFace> view2 = type.view("(two)");
    assertThat(view2.toJson(new Foo("a", 42))).isEqualTo("{\"two\":42}");
  }

  @Test
  void fromJson() {
    JsonType<DIFace> type = jsonb.type(DIFace.class);
    DIFace bean = type.fromJson("{\"one\":\"a\",\"two\":42}");

    assertThat(bean.one()).isEqualTo("a");
    assertThat(bean.two()).isEqualTo(42L);
    assertThat(bean).isInstanceOf(MyDIFace.class);
  }
}
