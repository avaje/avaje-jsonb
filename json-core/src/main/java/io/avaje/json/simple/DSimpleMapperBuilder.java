package io.avaje.json.simple;

import io.avaje.json.core.CoreTypes;
import io.avaje.json.stream.JsonStream;

final class DSimpleMapperBuilder implements SimpleMapper.Builder {

  private JsonStream jsonStream;

  @Override
  public SimpleMapper.Builder jsonStream(JsonStream jsonStream) {
    this.jsonStream = jsonStream;
    return this;
  }

  @Override
  public SimpleMapper build() {
    final var stream = jsonStream != null ? jsonStream : JsonStream.builder().build();
    final var coreAdapters = CoreTypes.createCoreAdapters();
    return new DSimpleMapper(stream, coreAdapters);
  }
}
