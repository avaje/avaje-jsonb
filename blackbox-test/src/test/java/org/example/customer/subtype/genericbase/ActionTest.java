package org.example.customer.subtype.genericbase;

import static org.assertj.core.api.Assertions.assertThat;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

class ActionTest {

  Jsonb jsonb = Jsonb.builder().build();
  JsonType<Action> type = jsonb.type(Action.class);

  @Test
  void roundTrip_test1() {
    Test1 test1 = new Test1(new Test1.Config("one"));
    String json = type.toJson(test1);
    assertThat(json).isEqualTo("{\"@type\":\"Test1\",\"genericConfig\":{\"a\":\"one\"}}");

    Action back = type.fromJson(json);
    assertThat(back).isInstanceOf(Test1.class);
    assertThat(((Test1) back).getGenericConfig().getA()).isEqualTo("one");
  }

  @Test
  void roundTrip_test2() {
    Test2 test2 = new Test2(new Test2.Config("two"));
    String json = type.toJson(test2);
    assertThat(json).isEqualTo("{\"@type\":\"Test2\",\"genericConfig\":{\"a\":\"two\"}}");

    Action back = type.fromJson(json);
    assertThat(back).isInstanceOf(Test2.class);
    assertThat(((Test2) back).getGenericConfig().getA()).isEqualTo("two");
  }

  @Test
  void individualAdapter_test1() {
    JsonType<Test1> test1Type = jsonb.type(Test1.class);
    Test1 test1 = new Test1(new Test1.Config("solo"));
    String json = test1Type.toJson(test1);
    Test1 back = test1Type.fromJson(json);
    assertThat(back.getGenericConfig().getA()).isEqualTo("solo");
  }
}
