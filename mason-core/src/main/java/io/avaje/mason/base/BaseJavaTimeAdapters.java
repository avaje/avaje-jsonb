package io.avaje.mason.base;

import io.avaje.mason.JsonAdapter;
import io.avaje.mason.JsonReader;
import io.avaje.mason.JsonWriter;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

final class BaseJavaTimeAdapters {

  static final JsonAdapter.Factory FACTORY = (type, annotations, jsonb) -> {
    if (!annotations.isEmpty()) {
      return null;
    }
    if (type == Instant.class) {
      return BaseJavaTimeAdapters.INSTANT_ADAPTER;
    }
    if (type == UUID.class) {
      return BaseJavaTimeAdapters.UUID_ADAPTER;
    }
    return null;
  };

  static final JsonAdapter<Instant> INSTANT_ADAPTER = new JsonAdapter<Instant>() {
    @Override
    public Instant fromJson(JsonReader reader) throws IOException {
      String value = reader.nextString();
      return Instant.parse(value);
    }

    @Override
    public void toJson(JsonWriter writer, Instant value) throws IOException {
      writer.value(value.toString());
    }
  };

  static final JsonAdapter<UUID> UUID_ADAPTER = new JsonAdapter<UUID>() {
    @Override
    public UUID fromJson(JsonReader reader) throws IOException {
      String value = reader.nextString();
      return UUID.fromString(value);
    }

    @Override
    public void toJson(JsonWriter writer, UUID value) throws IOException {
      writer.value(value.toString());
    }
  };
}
