package io.avaje.json.node.adapter;

import io.avaje.json.JsonAdapter;
import io.avaje.json.node.JsonNodeAdapter;
import io.avaje.jsonb.AdapterFactory;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.spi.JsonbComponent;

import java.lang.reflect.Type;

/**
 * Register with JsonB to support the JsonAdapters for JsonNode types.
 */
public final class JsonNodeComponent implements JsonbComponent {

  @Override
  public void register(Jsonb.Builder builder) {
    builder.add(new JsonBFactory(JsonNodeAdapter.builder().build()));
  }

  private static final class JsonBFactory implements AdapterFactory {

    private final JsonNodeAdapter nodeAdapter;

    JsonBFactory(JsonNodeAdapter nodeAdapter) {
      this.nodeAdapter = nodeAdapter;
    }

    @Override
    public JsonAdapter<?> create(Type type, Jsonb jsonb) {
      return nodeAdapter.create(type);
    }
  }
}
