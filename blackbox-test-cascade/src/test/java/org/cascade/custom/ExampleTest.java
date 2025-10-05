package org.cascade.custom;

import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.cascade.custom.MagicNumber;
import org.cascade.custom.Ulid;

class ExampleTest {

  @Test
  void roundTrip() {
    final var jsonb = Jsonb.builder().build();
    final var ulid = Ulid.fast();
    final var example = new Example(new MagicNumber(42), ulid);

    final var json = jsonb.toJson(example);
    final var deserialized = jsonb.type(Example.class).fromJson(json);

    assertEquals(42, deserialized.magicNumber().number());
    assertEquals(ulid, deserialized.ulid());
  }
}