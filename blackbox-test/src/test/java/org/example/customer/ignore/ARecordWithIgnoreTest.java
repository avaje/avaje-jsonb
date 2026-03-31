package org.example.customer.ignore;

import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ARecordWithIgnoreTest {

  Jsonb jsonb = Jsonb.builder().build();

  @Test
  void toFromJson() {
    var record = new ARecordWithIgnore("one", "two");
    String json = jsonb.toJson(record);
    assertThat(json).isEqualTo("{\"one\":\"one\"}");

    var fromJson = jsonb.type(ARecordWithIgnore.class)
      .fromJson("{\"one\":\"oneRead\",\"two\":\"twoIgnored\"}");
    assertThat(fromJson.one()).isEqualTo("oneRead");
    assertThat(fromJson.two()).isNull();
  }

  @Test
  void toFromJsonWithPrimitives() {
    var record = new ARecordWithPrimitives("one", true, 1, 1, 1, 1, (short) 1, "foo", "end");
    String json = jsonb.toJson(record);
    assertThat(json).isEqualTo("{\"one\":\"one\",\"end\":\"end\"}");

    var fromJson = jsonb.type(ARecordWithPrimitives.class)
      .fromJson("{\"one\":\"oneRead\",\"bool\":true,\"myInt\":1,\"myLong\":1,\"myDouble\":1,\"myFloat\":1,\"myShort\":1,\"anything\":\"foo\",\"end\":\"theEnd\"}");
    assertThat(fromJson.one()).isEqualTo("oneRead");
    assertThat(fromJson.end()).isEqualTo("theEnd");
    assertThat(fromJson.bool()).isFalse();
    assertThat(fromJson.myInt()).isEqualTo(0);
    assertThat(fromJson.myLong()).isEqualTo(0);
    assertThat(fromJson.myDouble()).isEqualTo(0);
    assertThat(fromJson.myFloat()).isEqualTo(0);
    assertThat(fromJson.myShort()).isEqualTo((short) 0);
    assertThat(fromJson.anything()).isNull();
  }
}
