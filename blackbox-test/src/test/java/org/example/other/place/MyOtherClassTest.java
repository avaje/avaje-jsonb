package org.example.other.place;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MyOtherClassTest {

  Jsonb jsonb = Jsonb.builder().serializeEmpty(false).build();
  JsonType<MyOtherClass> jsonType = jsonb.type(MyOtherClass.class);

  @Test
  void toFromJson() {

    MyOtherClass bean = new MyOtherClass("bazz");
    bean.some(42);

    String asJson = jsonType.toJson(bean);

    MyOtherClass fromJson = jsonType.fromJson(asJson);

    assertThat(fromJson.thing()).isEqualTo(bean.thing());
    assertThat(fromJson.some()).isEqualTo(bean.some());
  }
}
