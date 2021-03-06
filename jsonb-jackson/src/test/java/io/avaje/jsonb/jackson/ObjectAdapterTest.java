package io.avaje.jsonb.jackson;

import io.avaje.jsonb.JsonAdapter;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.io.IOException;
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
  void fromJson_readingMap() throws IOException {

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
  void fromJson_readingListOfMap() throws IOException {

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
}
