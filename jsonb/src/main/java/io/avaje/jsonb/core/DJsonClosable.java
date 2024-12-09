package io.avaje.jsonb.core;

import io.avaje.json.JsonReader;

/**
 * Adapter that supports closing the JsonReader when returned object (aka Stream) is closed.
 */
interface DJsonClosable<T> {

  /**
   * Read from the reader additionally closing the reader when done.
   * <p>
   * Used when returning a Stream that should close the JsonReader when the
   * Stream is closed.
   */
  T fromJsonWithClose(JsonReader reader);
}
