package org.example.customer.generics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;

import org.example.customer.Address;
import org.example.customer.generics.GMS.Weaver.Hornet;
import org.junit.jupiter.api.Test;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;

class NestedClassGenericTest {

  Jsonb jsonb = Jsonb.instance();

  private static Hornet<Address> createTestData() {
    var bean = new Hornet<Address>();
    bean.value = new Address(90L, "one");
    return bean;
  }

  @SuppressWarnings({"rawtypes"})
  @Test
  void toJson() {
    var bean = createTestData();

    var type = jsonb.type(Hornet.class);

    String asJson = type.toJson(bean);
    assertThat(asJson).isEqualTo("{\"value\":{\"id\":90,\"street\":\"one\"}}");
    assertThat(jsonb.toJson(bean)).isEqualTo(asJson);

    var pageResult = type.fromJson(asJson);
    Object document = pageResult.value;
    // reading via Object means the list contains LinkedHashMap
    assertThat(document).isInstanceOf(LinkedHashMap.class);
    LinkedHashMap asMap = (LinkedHashMap) document;
    assertThat(asMap.get("street")).isEqualTo("one");
  }

  @Test
  void toJson_withGenericParam() {
    var bean = createTestData();

    JsonType<Hornet<Address>> type =
        jsonb.type(Types.newParameterizedType(Hornet.class, Address.class));

    String asJson = type.toJson(bean);
    assertThat(asJson).isEqualTo("{\"value\":{\"id\":90,\"street\":\"one\"}}");
    assertThat(jsonb.toJson(bean)).isEqualTo(asJson);

    var genericResult = type.fromJson(asJson);
    Address document = genericResult.value;

    assertThat(document.getId()).isEqualTo(90L);
    assertThat(document.getStreet()).isEqualTo("one");
  }
}
