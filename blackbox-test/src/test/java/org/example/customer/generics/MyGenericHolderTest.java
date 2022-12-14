package org.example.customer.generics;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.JsonView;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;
import org.example.customer.Address;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class MyGenericHolderTest {

  Jsonb jsonb = Jsonb.builder().build();

  private static MyGenericHolder<Address> createTestData() {
    var bean = new MyGenericHolder<Address>();
    bean.setTitle("hello").setAuthor("art").setDocument(new Address(90L, "one"));
    return bean;
  }

  @SuppressWarnings({"rawtypes"})
  @Test
  void toJson() {
    MyGenericHolder<Address> bean = createTestData();

    var type = jsonb.type(MyGenericHolder.class);

    String asJson = type.toJson(bean);
    assertThat(asJson).isEqualTo("{\"title\":\"hello\",\"author\":\"art\",\"document\":{\"id\":90,\"street\":\"one\"}}");
    assertThat(jsonb.toJson(bean)).isEqualTo(asJson);

    MyGenericHolder pageResult = type.fromJson(asJson);
    Object document = pageResult.getDocument();
    // reading via Object means the list contains LinkedHashMap
    assertThat(document).isInstanceOf(LinkedHashMap.class);
    LinkedHashMap asMap = (LinkedHashMap)document;
    assertThat(asMap.get("street")).isEqualTo("one");

    JsonView<MyGenericHolder> view = type.view("author,document(id)");
    String partialJson2 = view.toJson(bean);
    // not supporting partial on the generic object (output includes street)
    assertThat(partialJson2).isEqualTo("{\"author\":\"art\",\"document\":{\"id\":90,\"street\":\"one\"}}");
  }


  @Test
  void toJson_withGenericParam() {
    MyGenericHolder<Address> bean = createTestData();

    Jsonb jsonb = Jsonb.builder().build();
    JsonType<MyGenericHolder<Address>> type = jsonb.type(Types.newParameterizedType(MyGenericHolder.class, Address.class));

    String asJson = type.toJson(bean);
    assertThat(asJson).isEqualTo("{\"title\":\"hello\",\"author\":\"art\",\"document\":{\"id\":90,\"street\":\"one\"}}");
    assertThat(jsonb.toJson(bean)).isEqualTo(asJson);

    MyGenericHolder<Address> genericResult = type.fromJson(asJson);
    Address document = genericResult.getDocument();

    assertThat(document.getId()).isEqualTo(90L);
    assertThat(document.getStreet()).isEqualTo("one");


    JsonView<MyGenericHolder<Address>> partial = type.view("author,document(*)");
    String partialJson = partial.toJson(bean);
    assertThat(partialJson).isEqualTo("{\"author\":\"art\",\"document\":{\"id\":90,\"street\":\"one\"}}");

    JsonView<MyGenericHolder<Address>> partial2 = type.view("author,document(id)");
    String partialJson2 = partial2.toJson(bean);
    assertThat(partialJson2).isEqualTo("{\"author\":\"art\",\"document\":{\"id\":90}}");
  }
}
