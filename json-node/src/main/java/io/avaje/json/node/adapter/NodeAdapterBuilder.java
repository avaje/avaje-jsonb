package io.avaje.json.node.adapter;

import io.avaje.json.JsonAdapter;
import io.avaje.json.node.JsonNodeAdapter;
import io.avaje.json.node.JsonNumber;
import io.avaje.json.stream.JsonStream;

/**
 * Builder for JsonNodeAdapter.
 */
public final class NodeAdapterBuilder implements JsonNodeAdapter.Builder {

  private JsonStream jsonStream;
  private JsonAdapter<JsonNumber> numberAdapter;

  @Override
  public JsonNodeAdapter.Builder jsonStream(JsonStream jsonStream) {
    this.jsonStream = jsonStream;
    return this;
  }

  @Override
  public JsonNodeAdapter.Builder numberAdapter(JsonAdapter<JsonNumber> numberAdapter) {
    this.numberAdapter = numberAdapter;
    return this;
  }

  @Override
  public JsonNodeAdapter build() {
    final var stream = jsonStream != null ? jsonStream : JsonStream.builder().build();
    final var number = numberAdapter != null ? numberAdapter : DJsonNodeAdapter.NUMBER_ADAPTER;
    final var nodeAdapter = new NodeAdapter(number);
    final var objectAdapter = nodeAdapter.objectAdapter();
    final var arrayAdapter = nodeAdapter.arrayAdapter();

    return new DJsonNodeAdapter(stream, nodeAdapter, objectAdapter, arrayAdapter);
  }
}
