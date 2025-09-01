package org.example.customer.generics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.example.customer.Address;
import org.junit.jupiter.api.Test;

import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;

class MyListWrapperRecordTest {

  Jsonb jsonb = Jsonb.instance();

  private static MyListWrapperRecord<Address> createData() {
    return new MyListWrapperRecord<>(List.of(new Address(90L, "one"), new Address(91L, "two")));
  }

  @SuppressWarnings("unchecked")
  @Test
  void toJson() {
    var bean = createData();

    var type = jsonb.type(MyListWrapperRecord.class);

    String asJson = type.toJson(bean);
    assertThat(asJson)
        .isEqualTo("{\"list\":[{\"id\":90,\"street\":\"one\"},{\"id\":91,\"street\":\"two\"}]}");

    var jsonOfParams =
        jsonb.type(Types.newParameterizedType(MyListWrapperRecord.class, Address.class));

    var wrapper = (MyListWrapperRecord<Address>) jsonOfParams.fromJson(asJson);
    assertThat(wrapper.list()).hasSize(2);
    assertThat(wrapper.list().get(0)).isInstanceOf(Address.class);
    assertThat(wrapper.list().get(1)).isInstanceOf(Address.class);
  }
}
