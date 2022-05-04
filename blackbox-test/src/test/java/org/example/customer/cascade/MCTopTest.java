package org.example.customer.cascade;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class MCTopTest {

  Jsonb jsonb = Jsonb.builder().build();

  @Test
  void toJson() {
    JsonType<MCTop> type = jsonb.type(MCTop.class);

    String asJson = type.toJson(new MCTop(42, new MCOther("foo"), Collections.emptyList()));
    assertThat(asJson).isEqualTo("{\"id\":42,\"other\":{\"other\":\"foo\"},\"childMap\":{}}");

    String asJson2 = type.toJson(new MCTop(42, new MCOther("foo"), List.of(new MCChild(23), new MCChild(24))));
    assertThat(asJson2).isEqualTo("{\"id\":42,\"other\":{\"other\":\"foo\"},\"children\":[{\"myValue\":23},{\"myValue\":24}],\"childMap\":{}}");

    String asJsonWithMap = type.toJson(new MCTop(42, new MCOther("foo"), Collections.emptyList(), new TreeMap<>(Map.of("k0", new MCChild2(98), "k1", new MCChild2(99)))));
    assertThat(asJsonWithMap).isEqualTo("{\"id\":42,\"other\":{\"other\":\"foo\"},\"childMap\":{\"k0\":{\"myValue\":98},\"k1\":{\"myValue\":99}}}");
  }
}
