package org.example.customer.properties;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

class PropsTest {

  Jsonb jsonb = Jsonb.builder().build();
  JsonType<Props> jsonType = jsonb.type(Props.class);

  @Test
  void toJson_fromJson() {

    Properties properties = new Properties();

    properties.setProperty("hi", "hey");
    Props props = new Props(properties);

    String asJson = jsonType.toJson(props);
    Props fromJson = jsonType.fromJson(asJson);

    assertThat(fromJson.props()).containsEntry("hi", "hey");
  }
}
