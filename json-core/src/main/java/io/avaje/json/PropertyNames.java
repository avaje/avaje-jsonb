package io.avaje.json;

/**
 * Marker interface for IOAdapter specific property names.
 * <p>
 * This can provide support for use of names by index position where
 * the names have already been escaped and encoded.
 *
 * @see JsonWriter#allNames(PropertyNames)
 * @see JsonWriter#name(int)
 */
public interface PropertyNames {

  /** Return the (unescaped) name at the given position. */
  default String name(int position) {
    throw new UnsupportedOperationException();
  }
}
