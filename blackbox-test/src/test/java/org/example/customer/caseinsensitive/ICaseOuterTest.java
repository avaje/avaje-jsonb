package org.example.customer.caseinsensitive;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

class ICaseOuterTest {

  private final JsonType<ICaseOuter> type = Jsonb.builder().build().type(ICaseOuter.class);

  private final ICaseOuter bean = new ICaseOuter("outer",
    new ICaseOuter.HashInner("inner", Map.of("name", "tag", "after", "x"), new ICaseContact(42, "fn", "ln"), "innerAfter"),
    "outerAfter");

  private static final String JSON = "{\"NAME\":\"outer\",\"inner\":{\"name\":\"inner\",\"tags\":{\"name\":\"tag\",\"after\":\"x\"},"
    + "\"contact\":{\"ID\":42,\"FirstName\":\"fn\",\"lastname\":\"ln\"},\"after\":\"innerAfter\"},\"After\":\"outerAfter\"}";

  @Test
  void nestedNamesAndHashModes() {
    assertThat(type.fromJson(JSON)).isEqualTo(bean);
  }

  @Test
  void nestedNamesAndHashModes_stream() {
    assertThat(type.fromJson(new ByteArrayInputStream(JSON.getBytes(StandardCharsets.UTF_8)))).isEqualTo(bean);
  }

  @Test
  void roundTrip() {
    assertThat(type.fromJson(type.toJson(bean))).isEqualTo(bean);
  }
}
