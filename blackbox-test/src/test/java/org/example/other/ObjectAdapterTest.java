package org.example.other;

import io.avaje.jsonb.JsonType;
import io.avaje.json.*;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;

import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectAdapterTest {

  Jsonb jsonb = Jsonb.builder().build();

  JsonAdapter<Object> objectAdapter = jsonb.adapter(Object.class);

  JsonType<Object> objectType = jsonb.type(Object.class);

  @Test
  void nullJson() {
    Object val = objectType.fromJson("null");
    assertThat(val).isNull();

    String asJson = objectType.toJson(null);
    assertThat(asJson).isEqualTo("");
  }

  @Test
  void nullInMap() {
    Object val = objectType.fromJson("{\"a\":1,\"b\":null,\"c\":3}");
    assertThat(val).isInstanceOf(Map.class);

    String asJson = objectType.toJson(null);
    assertThat(asJson).isEqualTo("");
  }

  @Test
  void nullInList() {
    Object val = objectType.fromJson("[1,null,3]");
    assertThat(val).isInstanceOf(List.class);

    String asJson = objectType.toJson(val);
    assertThat(asJson).isEqualTo("[1,3]");
  }

  @Test
  void booleanTrue() {
    String asJson = objectType.toJson(true);
    assertThat(asJson).isEqualTo("true");

    Object val = objectType.fromJson("true");
    assertThat(val).isEqualTo(true);
  }

  @Test
  void booleanFalse() {
    String asJson = objectType.toJson(false);
    assertThat(asJson).isEqualTo("false");

    Object val = objectType.fromJson("false");
    assertThat(val).isEqualTo(false);
  }

  @SuppressWarnings("raw")
  @Test
  void booleanArray() {
    String asJson = objectType.toJson(List.of(false, true, true, false, true));
    assertThat(asJson).isEqualTo("[false,true,true,false,true]");

    Object val = objectType.fromJson("[false,true,true,false,true]");
    assertThat(val).isInstanceOf(List.class);
    List list = (List) val;
    assertThat(list).isEqualTo(List.of(false, true, true, false, true));
  }

  @SuppressWarnings("raw")
  @Test
  void mixedArray() {
    String asJson = objectType.toJson(List.of(42, false, true, "hi", true));
    assertThat(asJson).isEqualTo("[42,false,true,\"hi\",true]");

    Object val = objectType.fromJson("[42,false,true,\"hi\",true]");
    assertThat(val).isInstanceOf(List.class);
    List list = (List) val;
    assertThat(list).isEqualTo(List.of(42L, false, true, "hi", true));
  }

  @SuppressWarnings("unchecked")
  @Test
  void fromJson_readingMap() {

    Object value = objectAdapter.fromJson(jsonb.reader("{\"id\":42,\"name\":\"rob\"}"));

    assertThat(value).isInstanceOf(Map.class);
    Map<String, Object> asMap = (Map<String, Object>) value;
    assertThat(asMap.get("id")).isEqualTo(42L);
    assertThat(asMap.get("name")).isEqualTo("rob");

    Object fromJsonViaType = objectType.fromJson("{\"id\":42,\"name\":\"rob\"}");
    assertThat(fromJsonViaType).isEqualTo(asMap);
  }

  @SuppressWarnings("unchecked")
  @Test
  void fromJson_readingListOfMap() {

    Object value = objectAdapter.fromJson(jsonb.reader("[{\"id\":42,\"name\":\"rob\"},{\"id\":43,\"name\":\"bob\"}]"));

    assertThat(value).isInstanceOf(List.class);

    List<Map<String, Object>> asListOfMap = (List<Map<String, Object>>) value;
    assertThat(asListOfMap).hasSize(2);
    assertThat(asListOfMap.get(0).get("id")).isEqualTo(42L);
    assertThat(asListOfMap.get(0).get("name")).isEqualTo("rob");

    StringWriter sw = new StringWriter();
    JsonWriter jsonWriter = jsonb.writer(sw);
    objectAdapter.toJson(jsonWriter, value);
    jsonWriter.close();
    String asJson = sw.toString();

    assertThat(asJson).isEqualTo("[{\"id\":42,\"name\":\"rob\"},{\"id\":43,\"name\":\"bob\"}]");


    // a bit easier using JsonType compared to JsonAdapter
    String asJson2 = objectType.toJson(value);
    assertThat(asJson2).isEqualTo("[{\"id\":42,\"name\":\"rob\"},{\"id\":43,\"name\":\"bob\"}]");
  }

  @SuppressWarnings("unchecked")
  @Test
  void readingNested() {

    JsonType<Object> type = jsonb.type(Object.class);
    Object result = type.fromJson("{\"errors\":[{\"path\":42,\"property\":\"foo\",\"message\":\"must not be blank\"}]}");
    assertThat(result).isInstanceOf(Map.class);

    var map = (Map<String, Object>) result;
    assertThat(map).hasSize(1);
    Object errors = map.get("errors");
    assertThat(errors).isInstanceOf(List.class);
    var list = (List<?>) errors;
    assertThat(list).hasSize(1);
    var entry = (Map<String, Object>) list.get(0);

    assertThat(entry).hasSize(3);
    assertThat(entry.get("path")).isEqualTo(42L);
    assertThat(entry.get("property")).isEqualTo("foo");
    assertThat(entry.get("message")).isEqualTo("must not be blank");
  }

  @Test
  void testIntegerParsing() {

    JsonType<List<Map<String, Object>>> listMapType =
        jsonb.type(Types.listOf(Types.mapOf(Object.class)));
    String input = "[ {\"food\": \"sushi\", \"amount\": 5}]";

    List<Map<String, Object>> l = listMapType.fromJson(input);
    assertThat("5").isEqualTo(l.get(0).get("amount").toString());
  }
}
