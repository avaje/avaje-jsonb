package io.avaje.jsonb.core;

import io.avaje.jsonb.JsonAdapter;
import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.JsonWriter;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

/**
 * Adds support for java time types.
 */
final class JavaTimeAdapters {

  static final JsonAdapter.Factory FACTORY = (type, jsonb) -> {
    if (type == Instant.class) return JavaTimeAdapters.INSTANT_ADAPTER.nullSafe();
    if (type == UUID.class) return JavaTimeAdapters.UUID_ADAPTER.nullSafe();
    return null;
  };

  /**
   * Using ISO-8601
   */
  private static final JsonAdapter<Instant> INSTANT_ADAPTER = new JsonAdapter<Instant>() {
    @Override
    public Instant fromJson(JsonReader reader) throws IOException {
      return Instant.parse(reader.nextString());
    }

    @Override
    public void toJson(JsonWriter writer, Instant value) throws IOException {
      writer.value(value.toString());
    }

    @Override
    public String toString() {
      return "JsonAdapter(Instant)";
    }
  };

  private static final JsonAdapter<UUID> UUID_ADAPTER = new JsonAdapter<UUID>() {
    @Override
    public UUID fromJson(JsonReader reader) throws IOException {
      return UUID.fromString(reader.nextString());
    }

    @Override
    public void toJson(JsonWriter writer, UUID value) throws IOException {
      writer.value(value.toString());
    }

    @Override
    public String toString() {
      return "JsonAdapter(UUID)";
    }
  };
}
