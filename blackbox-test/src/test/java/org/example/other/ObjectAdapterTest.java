package org.example.other;

import io.avaje.jsonb.JsonAdapter;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectAdapterTest {

  Jsonb jsonb = Jsonb.builder().build();

  JsonAdapter<Object> objectAdapter = jsonb.adapter(Object.class);

  JsonType<Object> objectType = jsonb.type(Object.class);

  @SuppressWarnings("unchecked")
  @Test
  void fromJson_readingMap() {

    Object value = objectAdapter.fromJson(jsonb.reader("{\"id\":42,\"name\":\"rob\"}"));

    assertThat(value).isInstanceOf(Map.class);
    Map<String,Object> asMap = (Map<String, Object>) value;
    assertThat(asMap.get("id")).isEqualTo(42D);
    assertThat(asMap.get("name")).isEqualTo("rob");

    Object fromJsonViaType = objectType.fromJson("{\"id\":42,\"name\":\"rob\"}");
    assertThat(fromJsonViaType).isEqualTo(asMap);
  }

  @SuppressWarnings("unchecked")
  @Test
  void fromJson_readingListOfMap() {

    Object value = objectAdapter.fromJson(jsonb.reader("[{\"id\":42,\"name\":\"rob\"},{\"id\":43,\"name\":\"bob\"}]"));

    assertThat(value).isInstanceOf(List.class);

    List<Map<String,Object>> asListOfMap = (List<Map<String,Object>>) value;
    assertThat(asListOfMap).hasSize(2);
    assertThat(asListOfMap.get(0).get("id")).isEqualTo(42D);
    assertThat(asListOfMap.get(0).get("name")).isEqualTo("rob");

    StringWriter sw = new StringWriter();
    JsonWriter jsonWriter = jsonb.writer(sw);
    objectAdapter.toJson(jsonWriter, value);
    jsonWriter.close();
    String asJson = sw.toString();

    assertThat(asJson).isEqualTo("[{\"id\":42.0,\"name\":\"rob\"},{\"id\":43.0,\"name\":\"bob\"}]");


    // a bit easier using JsonType compared to JsonAdapter
    String asJson2 = objectType.toJson(value);
    assertThat(asJson2).isEqualTo("[{\"id\":42.0,\"name\":\"rob\"},{\"id\":43.0,\"name\":\"bob\"}]");
  }

  @SuppressWarnings("unchecked")
  @Test
  void readingNested() {

    JsonType<Object> type = jsonb.type(Object.class);
    Object result = type.fromJson("{\"errors\":[{\"path\":42,\"property\":\"foo\",\"message\":\"must not be blank\"}]}");
    assertThat(result).isInstanceOf(Map.class);

    var map = (Map<String,Object>)result;
    assertThat(map).hasSize(1);
    Object errors = map.get("errors");
    assertThat(errors).isInstanceOf(List.class);
    var list = (List<?>)errors;
    assertThat(list).hasSize(1);
    var entry = (Map<String,Object>)list.get(0);

    assertThat(entry).hasSize(3);
    assertThat(entry.get("path")).isEqualTo(42.0D);
    assertThat(entry.get("property")).isEqualTo("foo");
    assertThat(entry.get("message")).isEqualTo("must not be blank");
  }
}
