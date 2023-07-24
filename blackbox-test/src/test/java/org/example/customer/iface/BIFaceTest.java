package org.example.customer.iface;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.JsonView;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BIFaceTest {

  Jsonb jsonb = Jsonb.builder().build();

  record Foo(String one, boolean two, String three, String four) implements BIFace {
    @Override
    public String getOne() {
      return one;
    }

    @Override
    public boolean isTwo() {
      return two;
    }

    @Override
    public String getThree() {
      return three;
    }
  }

  @Test
  void toJson() {
    JsonType<BIFace> type = jsonb.type(BIFace.class);

    String asJson = type.toJson(new Foo("a", true, "b", "c"));
    assertThat(asJson).isEqualTo("{\"one\":\"a\",\"two\":true,\"three\":\"b\",\"four\":\"c\"}");
  }

  @Test
  void toJsonView() {
    JsonType<BIFace> type = jsonb.type(BIFace.class);

    JsonView<BIFace> view0 = type.view("(one)");
    String asJson = view0.toJson(new Foo("a", true, "b", "c"));
    assertThat(asJson).isEqualTo("{\"one\":\"a\"}");

    JsonView<BIFace> view1 = type.view("(one,two,four)");
    assertThat(view1.toJson(new Foo("a", true, "b", "c"))).isEqualTo("{\"one\":\"a\",\"two\":true,\"four\":\"c\"}");

    JsonView<BIFace> view2 = type.view("(two)");
    assertThat(view2.toJson(new Foo("a", true, "b", "c"))).isEqualTo("{\"two\":true}");
  }

}
