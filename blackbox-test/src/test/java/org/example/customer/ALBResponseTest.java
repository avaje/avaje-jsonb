package org.example.customer;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ALBResponseTest {

  private final Jsonb jsonb = Jsonb.instance();

  @Test
  void toJson() {
    ALBResponse response = new ALBResponse(200, false, Map.of("foo", "bar"), "content");

    JsonType<ALBResponse> type = jsonb.type(ALBResponse.class);
    String asJson = type.toJson(response);

    ALBResponse response1 = type.fromJson(asJson);
    assertThat(response1.getStatusCode()).isEqualTo(response1.getStatusCode());
    assertThat(response1.getBody()).isEqualTo("content");
    assertThat(response1.isBase64Encoded()).isFalse();
    assertThat(response1.getHeaders()).hasSize(1);
    assertThat(response1.getHeaders()).containsEntry("foo", "bar");
  }

  @Test
  void toJsonTwoScalarMaps() {
    ALBResponse2 response = new ALBResponse2(10,  Map.of("foo", "bar"), 20, Map.of("baz", "waz"), "content");

    JsonType<ALBResponse2> type = jsonb.type(ALBResponse2.class);
    String asJson = type.toJson(response);

    ALBResponse2 response1 = type.fromJson(asJson);
    assertThat(response1).isEqualTo(response);
  }
}
