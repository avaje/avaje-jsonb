package org.example.customer.creator;

import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LinkedGetterTest {

  final Jsonb jsonb = Jsonb.builder().build();

  @Test
  void toJson_withOtherFieldAfter() {
    var bean = new LinkedGetter("abbreviated", "someOther");

    String json = jsonb.toJson(bean);

    assertThat(json).contains("\"abbr\":\"abbreviated\"");
    assertThat(json).contains("\"other\":\"someOther\"");
  }
}
