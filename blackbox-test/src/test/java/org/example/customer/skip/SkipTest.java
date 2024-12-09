package org.example.customer.skip;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.avaje.json.stream.JsonStream;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SkipTest {

  Jsonb jsonb = Jsonb.builder().adapter(JsonStream.builder().build()).build();
  JsonType<MyCustomer> type = jsonb.type(MyCustomer.class);

  @Test
  void skipValue() {
    String extraField = "{\"id\":42,\"extra\":\"junk\",\"name\":\"rob\",\"notes\":\"foo\"}";

    MyCustomer fromJson = type.fromJson(extraField);

    assertThat(fromJson.id()).isEqualTo(42);
    assertThat(fromJson.name()).isEqualTo("rob");
    assertThat(fromJson.notes()).isEqualTo("foo");
  }

  @Test
  void skipObject() {
    String extraObject = "{\"id\":42,\"extra\":{\"moreId\":998},\"name\":\"rob\",\"notes\":\"foo\"}";

    MyCustomer fromJson = type.fromJson(extraObject);

    assertThat(fromJson.id()).isEqualTo(42);
    assertThat(fromJson.name()).isEqualTo("rob");
    assertThat(fromJson.notes()).isEqualTo("foo");
  }

  @Test
  void skipArray() {
    String extraArray = "{\"id\":42,\"extra\":[34,234,998],\"name\":\"rob\",\"notes\":\"foo\"}";

    MyCustomer fromJson = type.fromJson(extraArray);

    assertThat(fromJson.id()).isEqualTo(42);
    assertThat(fromJson.name()).isEqualTo("rob");
    assertThat(fromJson.notes()).isEqualTo("foo");
  }
}
