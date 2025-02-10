package io.avaje.json.mapper;

import io.avaje.json.core.CoreTypes;
import io.avaje.json.stream.JsonStream;

final class DJsonMapperBuilder implements JsonMapper.Builder {

  private JsonStream jsonStream;

  @Override
  public JsonMapper.Builder jsonStream(JsonStream jsonStream) {
    this.jsonStream = jsonStream;
    return this;
  }

  @Override
  public JsonMapper build() {
    final var stream = jsonStream != null ? jsonStream : JsonStream.builder().build();
    final var coreAdapters = CoreTypes.createCoreAdapters();
    return new DJsonMapper(stream, coreAdapters);
  }
}
