package io.avaje.jsonb.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SplittableRandom;
import java.util.UUID;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

class UuidAdapterTest {

  static final UuidAdapter CODEC = new UuidAdapter();
  static final JsonType<UUID> UUID_TYPE = Jsonb.builder().build().type(UUID.class);

  static final String[] EDGE = {
    "00000000-0000-0000-0000-000000000000",
    "ffffffff-ffff-ffff-ffff-ffffffffffff",
    "FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF",
    "123e4567-E89B-12d3-a456-426614174000",
    "1-2-3-4-5",
    "123e4567-e89b-12d3-a456-42661417400",
    "123e4567-e89b-12d3-a456-4266141740000",
    "123e4567-e89b-12d3-a456_426614174000",
    "123e4567e89b-12d3-a456-4266141740000",
    "g23e4567-e89b-12d3-a456-426614174000",
    "123e4567-e89b-12d3-a456-42661417400g",
    "123e4567-e89b-12d3-a4:6-426614174000",
    "123e4567-e89b-12d3-a4/6-426614174000",
    "123e4567-e89b-12d3-a4@6-426614174000",
    "123e4567-e89b-12d3-a4`6-426614174000",
    "+23e4567-e89b-12d3-a456-426614174000",
    "",
    "x",
    "------------------------------------",
  };

  @Test
  void edgeCases_matchJdk() {
    final List<String> bad = new ArrayList<>();
    for (final var s : EDGE) {
      final var buf = ("@\"_" + s + "\"@").getBytes(StandardCharsets.ISO_8859_1);
      final var expected = result(() -> UUID.fromString(s));
      final var actual = result(() -> CODEC.decode(buf, 3, 3 + s.length()));
      if (!Objects.equals(expected, actual)) {
        bad.add("'" + s + "' expected " + expected + " but got " + actual);
      }
    }
    assertThat(bad).isEmpty();
  }

  @Test
  void random_roundTrip() {
    final var r = new SplittableRandom(7);
    for (var i = 0; i < 100_000; i++) {
      final var uuid = r.nextInt(4) == 0 ? UUID.randomUUID() : new UUID(r.nextLong(), r.nextLong());
      final var json = UUID_TYPE.toJson(uuid);
      assertThat(json).isEqualTo("\"" + uuid + "\"");
      assertThat(UUID_TYPE.fromJson(json)).isEqualTo(uuid);
      final var upper = uuid.toString().toUpperCase().getBytes(StandardCharsets.ISO_8859_1);
      assertThat(CODEC.decode(upper, 0, upper.length)).isEqualTo(uuid);
    }
  }

  @Test
  void escaped_and_nonAscii_fallBackToString() {
    final var uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    assertThat(UUID_TYPE.fromJson("\"\\u0031\\u00323e4567-e89b-12d3-a456-426614174000\""))
        .isEqualTo(uuid);
    assertThat(UUID_TYPE.fromJson("null")).isNull();
    final var nonAscii = "123e4567-e89b-12d3-a456-42661417400é";
    assertThat(result(() -> UUID_TYPE.fromJson("\"" + nonAscii + "\"")))
        .isEqualTo(result(() -> UUID.fromString(nonAscii)));
  }

  @Test
  void stream_acrossBufferBoundaries() {
    final List<UUID> uuids = new ArrayList<>();
    final var r = new SplittableRandom(11);
    for (var i = 0; i < 5_000; i++) {
      uuids.add(new UUID(r.nextLong(), r.nextLong()));
    }
    final var uuidList = UUID_TYPE.list();
    final var json = uuidList.toJson(uuids);
    assertThat(uuidList.fromJson(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))))
        .isEqualTo(uuids);
  }

  @Test
  void write_pretty_and_nulls() {
    final var uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    final List<UUID> list = new ArrayList<>();
    list.add(uuid);
    list.add(null);
    list.add(uuid);
    final var type = Jsonb.builder().serializeNulls(true).build().type(UUID.class).list();
    final var json = type.toJson(list);
    assertThat(json).isEqualTo("[\"" + uuid + "\",null,\"" + uuid + "\"]");
    assertThat(type.fromJson(json)).isEqualTo(list);
    assertThat(type.toJsonPretty(list))
        .isEqualTo("[\n  \"" + uuid + "\",\n  null,\n  \"" + uuid + "\"\n]");
  }

  private static Object result(Supplier<Object> supplier) {
    try {
      return supplier.get();
    } catch (final RuntimeException e) {
      return e.getClass().getName() + ": " + e.getMessage();
    }
  }
}
