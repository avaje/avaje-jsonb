package org.example.customer.nesting;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MyNestedListTest {

  JsonType<MyNestedList> jsonb = Jsonb.instance().type(MyNestedList.class);

  @Test
  void toJson_fromJson() {
    var nested = new MyNestedList(List.of(List.of(1L, 2L, 3L), List.of(8L, 9L)));
    String asJson = jsonb.toJson(nested);
    assertThat(asJson).isEqualTo("{\"nestedInts\":[[1,2,3],[8,9]]}");

    MyNestedList myNestedList = jsonb.fromJson(asJson);
    assertThat(myNestedList.nestedInts()).hasSize(2);
    assertThat(myNestedList.nestedInts().get(0)).contains(1L, 2L, 3L);
    assertThat(myNestedList.nestedInts().get(1)).contains(8L, 9L);
  }
}
