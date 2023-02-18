package org.example.customer.generics;

import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;
import org.example.customer.Address;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MyListWrapperTest {

  Jsonb jsonb = Jsonb.builder().build();

  private static MyListWrapper<Address> createData() {
    return new MyListWrapper<>(List.of(new Address(90L, "one"), new Address(91L, "two")));
  }

  @SuppressWarnings("unchecked")
  @Test
  void toJson() {
    MyListWrapper<Address> bean = createData();

    var type = jsonb.type(MyListWrapper.class);

    String asJson = type.toJson(bean);
    assertThat(asJson).isEqualTo("{\"list\":[{\"id\":90,\"street\":\"one\"},{\"id\":91,\"street\":\"two\"}]}");

    var jsonOfParams = jsonb.type(Types.newParameterizedType(MyListWrapper.class, Address.class));

    MyListWrapper<Address> wrapper = (MyListWrapper<Address>)jsonOfParams.fromJson(asJson);
    assertThat(wrapper.list()).hasSize(2);
    assertThat(wrapper.list().get(0)).isInstanceOf(Address.class);
    assertThat(wrapper.list().get(1)).isInstanceOf(Address.class);
  }
}
