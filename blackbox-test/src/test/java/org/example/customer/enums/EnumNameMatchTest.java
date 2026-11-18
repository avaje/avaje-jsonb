package org.example.customer.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

class EnumNameMatchTest {

  enum Odd {
    ONE,
    CAFÉ,
    glbvs, // same FNV-1a hash as "yacxa"
    $DOLLAR
  }

  final Jsonb jsonb = Jsonb.instance();
  final JsonType<Odd> type = jsonb.type(Odd.class);

  @Test
  void toJson() {
    assertThat(type.list().toJson(List.of(Odd.values())))
      .isEqualTo("[\"ONE\",\"CAFÉ\",\"glbvs\",\"$DOLLAR\"]");
  }

  @Test
  void fromJson() {
    for (Odd value : Odd.values()) {
      assertThat(type.fromJson("\"" + value.name() + "\"")).isSameAs(value);
    }
    assertThat(type.fromJson("\"CAF\\u00c9\"")).isSameAs(Odd.CAFÉ);
    assertThat(type.fromJson("null")).isNull();
  }

  @Test
  void fromJson_stream() {
    String json = jsonb.type(Odd.class).list().toJson(List.of(Odd.ONE, Odd.CAFÉ, Odd.glbvs));
    var stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
    assertThat(type.list().fromJson(stream)).containsExactly(Odd.ONE, Odd.CAFÉ, Odd.glbvs);
  }

  @Test
  void fromJson_unknown() {
    assertThatThrownBy(() -> type.fromJson("\"TWO\""))
      .isInstanceOf(IllegalArgumentException.class);
    // hash collision with glbvs must not match it
    assertThatThrownBy(() -> type.fromJson("\"yacxa\""))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("yacxa");
  }
}
