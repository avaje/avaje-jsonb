package io.avaje.jsonb.jackson;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UnwrapJacksonParserTest {

  Jsonb jsonb = Jsonb.builder().build();

  @Test
  void unwrap() {
    try (JsonReader reader = jsonb.reader("{\"id\":42,\"name\":\"rob\"}")) {
      JsonParser jsonParser = reader.unwrap(JsonParser.class);
      JsonLocation location = jsonParser.currentLocation();

      assertThat(location.toString()).isEqualTo("[Source: (String)\"{\"id\":42,\"name\":\"rob\"}\"; line: 1, column: 1]");

      JsonType<Map<String, Object>> jsonMap = jsonb.type(Object.class).map();

      Map<String, Object> map = jsonMap.fromJson(reader);
      assertThat(map.get("id")).isEqualTo(42D);
      assertThat(map.get("name")).isEqualTo("rob");
    }
  }
}
