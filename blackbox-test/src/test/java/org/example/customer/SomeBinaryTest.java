package org.example.customer;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SomeBinaryTest {

  Jsonb jsonb = Jsonb.builder().build();

  @Test
  void toJsonFromJson_byteArray() {
    byte[] content = "hello".getBytes(StandardCharsets.UTF_8);
    SomeBinary bean = new SomeBinary(12, content);

    JsonType<SomeBinary> type = jsonb.type(SomeBinary.class);
    String json = type.toJson(bean);
    assertThat(json).isEqualTo("{\"id\":12,\"content\":\"aGVsbG8=\"}");

    SomeBinary fromJson = type.fromJson(json);
    String contentString = new String(fromJson.content(), StandardCharsets.UTF_8);
    assertThat(contentString).isEqualTo("hello");
    assertThat(fromJson.content()).isEqualTo(bean.content());
  }

  @SuppressWarnings("unchecked")
  @Test
  void jsonValue() {
    byte[] content = "hello".getBytes(StandardCharsets.UTF_8);

    Map<String, Object> object = new LinkedHashMap<>();
    object.put("content", content);

    String asJson = jsonb.toJson(object);
    assertThat(asJson).isEqualTo("{\"content\":\"aGVsbG8=\"}");

    JsonType<Object> type = jsonb.type(Types.mapOf(byte[].class));
    Map<String, Object> fromJson = (Map<String, Object>) type.fromJson(asJson);
    byte[] fromContent = (byte[]) fromJson.get("content");

    assertThat(fromContent).isEqualTo(content);
    String contentString = new String(fromContent, StandardCharsets.UTF_8);
    assertThat(contentString).isEqualTo("hello");
  }
}
