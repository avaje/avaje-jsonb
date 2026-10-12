package org.example.customer.mapkey;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.Json;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

public class MapKeyStreamTest {

  @Json
  public record MapHolder(Map<String, Integer> m, String after) {}

  final Jsonb jsonb = Jsonb.builder().build();

  @Test
  void fromJson_nonAsciiKey_mapInsideRecord_keyCrossesParserBuffer() {
    JsonType<MapHolder> type = jsonb.type(MapHolder.class);
    // padding so that the key "café" crosses the end of the default 4KB parser buffer
    for (int pad = 3950; pad < 4150; pad++) {
      String json = "{\"m\":{\"" + "p".repeat(pad) + "\":0,\"café\":1},\"after\":\"x\"}";

      MapHolder fromStream = type.fromJson(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
      assertThat(fromStream.m().get("café")).as("InputStream, pad %d", pad).isEqualTo(1);
      assertThat(fromStream.m()).hasSize(2);
      assertThat(fromStream.after()).isEqualTo("x");

      MapHolder fromReader = type.fromJson(new StringReader(json));
      assertThat(fromReader.m().get("café")).as("Reader, pad %d", pad).isEqualTo(1);
      assertThat(fromReader.m()).hasSize(2);
      assertThat(fromReader.after()).isEqualTo("x");
    }
  }
}
