package org.example;

import io.avaje.json.JsonDataException;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.example.other.MyPrimitives;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FailOnNullPrimitivesTest {

  @Test
  void default_allowsNulls_expect_defaultValues() {
    String jsonContent = "{\"a\":null,\"b\":null,\"c\":null,\"d\":null}";

    // default skips unknown
    Jsonb jsonb = Jsonb.builder().build();
    JsonType<MyPrimitives> jsonType = jsonb.type(MyPrimitives.class);

    MyPrimitives bean = jsonType.fromJson(jsonContent);
    assertThat(bean.a()).isEqualTo(false);
    assertThat(bean.b()).isEqualTo(0);
    assertThat(bean.c()).isEqualTo(0);
    assertThat(bean.d()).isEqualTo(0);
  }

  @Test
  void failOnNullPrimitives() {
    Jsonb jsonb = Jsonb.builder().failOnNullPrimitives(true).build();
    JsonType<MyPrimitives> jsonType = jsonb.type(MyPrimitives.class);

    assertThatThrownBy(() ->jsonType.fromJson("{\"a\":null,\"b\":null,\"c\":null,\"d\":null}"))
      .isInstanceOf(JsonDataException.class)
      .hasMessageContaining("Read NULL value for boolean");

    assertThatThrownBy(() ->jsonType.fromJson("{\"a\":false,\"b\":null,\"c\":null,\"d\":null}"))
      .isInstanceOf(JsonDataException.class)
      .hasMessageContaining("Read NULL value for int");

    assertThatThrownBy(() ->jsonType.fromJson("{\"a\":false,\"b\":7,\"c\":null,\"d\":null}"))
      .isInstanceOf(JsonDataException.class)
      .hasMessageContaining("Read NULL value for long");

    assertThatThrownBy(() ->jsonType.fromJson("{\"a\":false,\"b\":7,\"c\":7,\"d\":null}"))
      .isInstanceOf(JsonDataException.class)
      .hasMessageContaining("Read NULL value for double");

    MyPrimitives result = jsonType.fromJson("{\"a\":true,\"b\":3,\"c\":5,\"d\":7}");
    assertThat(result.a()).isTrue();
    assertThat(result.b()).isEqualTo(3);
    assertThat(result.c()).isEqualTo(5);
    assertThat(result.d()).isEqualTo(7);
  }

}
