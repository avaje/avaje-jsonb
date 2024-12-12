package io.avaje.json.node.adapter;

import io.avaje.json.JsonAdapter;
import io.avaje.json.node.JsonNodeMapper;
import io.avaje.json.node.JsonNumber;
import io.avaje.json.stream.JsonStream;

/**
 * Builder for JsonNodeMapper.
 */
public final class NodeAdapterBuilder implements JsonNodeMapper.Builder {

  private JsonStream jsonStream;
  private JsonAdapter<JsonNumber> numberAdapter;

  @Override
  public JsonNodeMapper.Builder jsonStream(JsonStream jsonStream) {
    this.jsonStream = jsonStream;
    return this;
  }

  @Override
  public JsonNodeMapper.Builder numberAdapter(JsonAdapter<JsonNumber> numberAdapter) {
    this.numberAdapter = numberAdapter;
    return this;
  }

  @Override
  public JsonNodeMapper build() {
    final var stream = jsonStream != null ? jsonStream : JsonStream.builder().build();
    final var number = numberAdapter != null ? numberAdapter : DJsonNodeMapper.NUMBER_ADAPTER;
    final var nodeAdapter = new NodeAdapter(number);
    final var objectAdapter = nodeAdapter.objectAdapter();
    final var arrayAdapter = nodeAdapter.arrayAdapter();

    return new DJsonNodeMapper(stream, nodeAdapter, objectAdapter, arrayAdapter);
  }
}
