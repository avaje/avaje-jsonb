package org.example.customer;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
//import io.avaje.jsonb.jackson.JacksonIOAdapter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BasicPubFieldsTest {

  Jsonb jsonb = Jsonb.builder().build();
  JsonType<BasicPubFields> type = jsonb.type(BasicPubFields.class);

  @Test
  void toJson()  {

    BasicPubFields b = new BasicPubFields();
    b.one = "oneValue";
    b.two = 43L;
    b.three = "threeValue";
    b.four = "fourValue";

    String asJson = type.toJson(b);
    assertThat(asJson).isEqualTo("{\"one\":\"oneValue\",\"two\":43,\"three\":\"threeValue\",\"four\":\"fourValue\"}");

    var view = type.view("(two, four)");
    String viewJson = view.toJson(b);
    assertThat(viewJson).isEqualTo("{\"two\":43,\"four\":\"fourValue\"}");

    String viewJsonPretty = view.toJsonPretty(b).replace("\" : ", "\": ");
    assertThat(viewJsonPretty).isEqualTo("{\n  \"two\": 43,\n  \"four\": \"fourValue\"\n}");
  }

  @Test
  void fromJson()  {

    BasicPubFields fromJson = type.fromJson("{\"one\":\"oneValue\",\"two\":43,\"three\":\"threeValue\",\"four\":\"fourValue\"}");

    assertThat(fromJson.one).isEqualTo("oneValue");
    assertThat(fromJson.two).isEqualTo(43L);
    assertThat(fromJson.three).isEqualTo("threeValue");
    assertThat(fromJson.four).isEqualTo("fourValue");
  }
}
