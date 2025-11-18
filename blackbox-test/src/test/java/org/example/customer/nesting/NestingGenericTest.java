package org.example.customer.nesting;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

class NestingGenericTest {

  JsonType<TestMapMap> jsonb = Jsonb.instance().type(TestMapMap.class);

  @Test
  void asd() {
    var res = new TestMapMap(Map.of("test", Map.of("123", new TestMapMap.Entity("name", "value"))));
    var to = jsonb.toJson(res);
    var from = jsonb.fromJson(to);
    assertThat(from).isEqualTo(res);
  }
}
