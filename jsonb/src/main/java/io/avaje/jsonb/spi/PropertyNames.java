package io.avaje.jsonb.spi;

import io.avaje.jsonb.JsonWriter;

/**
 * Marker interface for IOAdapter specific property names.
 * <p>
 * This can provide support for use of names by index position where
 * the names have already been escaped and encoded.
 *
 * @see JsonWriter#names(PropertyNames)
 * @see JsonWriter#name(int)
 */
public interface PropertyNames {
}
