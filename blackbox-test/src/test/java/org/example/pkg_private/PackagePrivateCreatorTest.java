package org.example.pkg_private;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

class PackagePrivateCreatorTest {

  final JsonType<PackagePrivateCreator> jsonb =
      Jsonb.builder().build().type(PackagePrivateCreator.class);

  @Test
  void to_From_Json() {
    final var bean = new PackagePrivateCreator(5);
    final var str = jsonb.toJson(bean);
    assertThat(str).isEqualTo("{\"id\":5}");

    final var from = jsonb.fromJson(str);
    assertThat(bean.id).isEqualTo(from.id);
  }
}
