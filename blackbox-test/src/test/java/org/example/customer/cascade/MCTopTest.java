package org.example.customer.cascade;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MCTopTest {

  Jsonb jsonb = Jsonb.newBuilder().build();

  @Test
  void toJson() {
    JsonType<MCTop> type = jsonb.type(MCTop.class);

    String asJson = type.toJson(new MCTop(42, new MCOther("foo"), Collections.emptyList()));
    assertThat(asJson).isEqualTo("{\"id\":42,\"other\":{\"other\":\"foo\"}}");

    String asJson2 = type.toJson(new MCTop(42, new MCOther("foo"), List.of(new MCChild(23), new MCChild(24))));
    assertThat(asJson2).isEqualTo("{\"id\":42,\"other\":{\"other\":\"foo\"},\"children\":[{\"myValue\":23},{\"myValue\":24}]}");
  }
}
