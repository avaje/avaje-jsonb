package org.example;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MyEnumTest {

  Jsonb jsonb = Jsonb.builder().add(MyEnum.class, MyEnumJsonAdapter::new).build();

  @Test
  void toJson_withJsonValue_expect_valueInJson() {

    JsonType<MyEnum> type = jsonb.type(MyEnum.class);

    assertThat(type.toJson(MyEnum.ONE)).isEqualTo("\"one val\"");
    assertThat(type.toJson(MyEnum.TWO)).isEqualTo("\"two val\"");
  }

  @Test
  void fromJson() {

    JsonType<List<MyEnum>> type = jsonb.type(MyEnum.class).list();

    List<MyEnum> myEnums = type.fromJson("[\"one val\", \"two val\"]");
    assertThat(myEnums).hasSize(2);
    assertThat(myEnums.get(0)).isEqualTo(MyEnum.ONE);
    assertThat(myEnums.get(1)).isEqualTo(MyEnum.TWO);
  }
}
