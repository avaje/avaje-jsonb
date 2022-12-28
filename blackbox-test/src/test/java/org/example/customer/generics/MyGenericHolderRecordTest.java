package org.example.customer.generics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;

import org.example.customer.Address;
import org.example.customer.generics.MyGenericHolder.MyGenericHolderRecord;
import org.junit.jupiter.api.Test;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;

class MyGenericHolderRecordTest {

  Jsonb jsonb = Jsonb.builder().build();

  private static MyGenericHolderRecord<String, String, Address> createTestData() {
    return new MyGenericHolderRecord<>("hello", "art", new Address(90L, "one"));
  }

  @SuppressWarnings({"rawtypes"})
  @Test
  void toJson() {
    final var bean = createTestData();

    final var type = jsonb.type(MyGenericHolderRecord.class);

    final var asJson = type.toJson(bean);
    assertThat(asJson)
        .isEqualTo(
            "{\"title\":\"hello\",\"author\":\"art\",\"document\":{\"id\":90,\"street\":\"one\"}}");
    assertThat(jsonb.toJson(bean)).isEqualTo(asJson);

    final var pageResult = type.fromJson(asJson);
    final var document = pageResult.document();
    // reading via Object means the list contains LinkedHashMap
    assertThat(document).isInstanceOf(LinkedHashMap.class);
    final var asMap = (LinkedHashMap) document;
    assertThat(asMap.get("street")).isEqualTo("one");

    final var view = type.view("author,document(id)");
    final var partialJson2 = view.toJson(bean);
    // not supporting partial on the generic object (output includes street)
    assertThat(partialJson2)
        .isEqualTo("{\"author\":\"art\",\"document\":{\"id\":90,\"street\":\"one\"}}");
  }

  @Test
  void toJson_withGenericParam() {
    final var bean = createTestData();

    final var jsonb = Jsonb.builder().build();
    final JsonType<MyGenericHolderRecord<String, String, Address>> type =
        jsonb.type(
            Types.newParameterizedType(
                MyGenericHolderRecord.class, String.class, String.class, Address.class));

    final var asJson = type.toJson(bean);
    assertThat(asJson)
        .isEqualTo(
            "{\"title\":\"hello\",\"author\":\"art\",\"document\":{\"id\":90,\"street\":\"one\"}}");
    assertThat(jsonb.toJson(bean)).isEqualTo(asJson);

    final var genericResult = type.fromJson(asJson);
    final var document = genericResult.document();

    assertThat(document.getId()).isEqualTo(90L);
    assertThat(document.getStreet()).isEqualTo("one");

    final var partial = type.view("author,document(*)");
    final var partialJson = partial.toJson(bean);
    assertThat(partialJson)
        .isEqualTo("{\"author\":\"art\",\"document\":{\"id\":90,\"street\":\"one\"}}");

    final var partial2 = type.view("author,document(id)");
    final var partialJson2 = partial2.toJson(bean);
    assertThat(partialJson2).isEqualTo("{\"author\":\"art\",\"document\":{\"id\":90}}");
  }
}
