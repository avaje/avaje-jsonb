package org.example.other.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;

class CustomClashJsonAdapterTest {

  @Test
  void test() {
    assertThat(Jsonb.instance().adapter(CustomClashJsonAdapter.NestedGeneric.class))
        .isInstanceOf(CustomClashJsonAdapter.class);

    assertThat(
            Jsonb.instance()
                .adapter(Types.newParameterizedType(CustomClashJsonAdapter.class, String.class)))
        .isInstanceOf(CustomClashJsonAdapter.class);
  }
}
