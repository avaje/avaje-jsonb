package io.avaje.json.stream.core;

import io.avaje.json.stream.BufferRecycleStrategy;
import io.avaje.json.stream.JsonStream;

/**
 * Used to build the default JsonStream implementation with custom settings.
 */
public final class JsonStreamBuilder implements JsonStream.Builder {

  private BufferRecycleStrategy strategy = BufferRecycleStrategy.HYBRID_POOL;
  private boolean serializeNulls;
  private boolean serializeEmpty;
  private boolean failOnUnknown;

  /**
   * Set to true to serialize nulls. Defaults to false.
   */
  @Override
  public JsonStreamBuilder serializeNulls(boolean serializeNulls) {
    this.serializeNulls = serializeNulls;
    return this;
  }

  /**
   * Set to true to serialize empty collections. Defaults to false.
   */
  @Override
  public JsonStreamBuilder serializeEmpty(boolean serializeEmpty) {
    this.serializeEmpty = serializeEmpty;
    return this;
  }

  /**
   * Set to true to fail on unknown properties. Defaults to false.
   */
  @Override
  public JsonStreamBuilder failOnUnknown(boolean failOnUnknown) {
    this.failOnUnknown = failOnUnknown;
    return this;
  }

  /**
   * Determines how byte buffers are recycled
   */
  @Override
  public JsonStreamBuilder bufferRecycling(BufferRecycleStrategy strategy) {
    this.strategy = strategy;
    return this;
  }

  /**
   * Build and return the JsonStream.
   */
  @Override
  public JsonStream build() {
    return new CoreJsonStream(serializeNulls, serializeEmpty, failOnUnknown, strategy);
  }
}
