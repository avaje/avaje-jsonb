package org.example.customer.caseinsensitive;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ICaseContactTest {

  Jsonb jsonb = Jsonb.builder().build();

  @Test
  void toJsonFromJson() {
    ICaseContact bean = new ICaseContact(42, "first", "last");
    String asJson = jsonb.toJson(bean);
    assertThat(asJson).isEqualTo("{\"id\":42,\"firstName\":\"first\",\"lastName\":\"last\"}");

    JsonType<ICaseContact> type = jsonb.type(ICaseContact.class);
    ICaseContact fromJson = type.fromJson(asJson);

    assertThat(fromJson).isEqualTo(bean);
    assertThat(type.fromJson("{\"id\":42,\"FIRSTNAME\":\"first\",\"lastname\":\"last\"}")).isEqualTo(bean);
    assertThat(type.fromJson("{\"id\":42,\"FirstName\":\"first\",\"LastName\":\"last\"}")).isEqualTo(bean);
  }
}
