package org.example.customer.generics;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.JsonView;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;
import org.example.customer.Address;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MyGenericPageResultTest {

  Jsonb jsonb = Jsonb.builder().build();

  private static MyGenericPageResult<Address> createPageTestData() {
    var bean = new MyGenericPageResult<Address>();
    bean.setPage(42).setPageSize(10).setResults(List.of(new Address(90L, "one"), new Address(91L, "two")));
    return bean;
  }

  @SuppressWarnings({"rawtypes"})
  @Test
  void toJson() {
    MyGenericPageResult<Address> bean = createPageTestData();

    var type = jsonb.type(MyGenericPageResult.class);

    String asJson = type.toJson(bean);
    assertThat(asJson).isEqualTo("{\"page\":42,\"pageSize\":10,\"totalPageCount\":0,\"results\":[{\"id\":90,\"street\":\"one\"},{\"id\":91,\"street\":\"two\"}]}");
    assertThat(jsonb.toJson(bean)).isEqualTo(asJson);

    MyGenericPageResult pageResult = type.fromJson(asJson);
    List list = pageResult.getResults();
    assertThat(list).hasSize(2);
    // reading via Object means the list contains LinkedHashMap
    assertThat(list.get(0)).isInstanceOf(LinkedHashMap.class);

    JsonView<MyGenericPageResult> partial2 = type.view("page,results(id)");
    String partialJson2 = partial2.toJson(bean);
    // not supporting partial on the generic list of object (output includes street)
    assertThat(partialJson2).isEqualTo("{\"page\":42,\"results\":[{\"id\":90,\"street\":\"one\"},{\"id\":91,\"street\":\"two\"}]}");
  }


  @Test
  void toJson_withGenericParam() {
    MyGenericPageResult<Address> bean = createPageTestData();

    Jsonb jsonb = Jsonb.builder().build();

    JsonType<MyGenericPageResult<Address>> type = jsonb.type(Types.newParameterizedType(MyGenericPageResult.class, Address.class));

    String asJson = type.toJson(bean);
    assertThat(asJson).isEqualTo("{\"page\":42,\"pageSize\":10,\"totalPageCount\":0,\"results\":[{\"id\":90,\"street\":\"one\"},{\"id\":91,\"street\":\"two\"}]}");
    assertThat(jsonb.toJson(bean)).isEqualTo(asJson);

    MyGenericPageResult<Address> genericResult = type.fromJson(asJson);
    List<? extends Address> addresses = genericResult.getResults();

    assertThat(addresses).hasSize(2);
    assertThat(addresses.get(0)).isInstanceOf(Address.class);

    JsonView<MyGenericPageResult<Address>> partial = type.view("page,results(*)");
    String partialJson = partial.toJson(bean);
    assertThat(partialJson).isEqualTo("{\"page\":42,\"results\":[{\"id\":90,\"street\":\"one\"},{\"id\":91,\"street\":\"two\"}]}");

    JsonView<MyGenericPageResult<Address>> partial2 = type.view("page,results(id)");
    String partialJson2 = partial2.toJson(bean);
    assertThat(partialJson2).isEqualTo("{\"page\":42,\"results\":[{\"id\":90},{\"id\":91}]}");
  }
}
