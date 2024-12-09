package io.avaje.jsonb;

import java.lang.reflect.Type;
import io.avaje.json.JsonAdapter;

/**
 * Factory for creating a JsonAdapter.
 */
public interface AdapterFactory {

  /**
   * Create and return a JsonAdapter given the type and annotations or return null.
   * <p>
   * Returning null means that the adapter could be created by another factory.
   */
  JsonAdapter<?> create(Type type, Jsonb jsonb);
}
