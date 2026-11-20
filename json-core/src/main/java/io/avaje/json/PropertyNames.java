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

  /**
   * Return the (unescaped) name at the given position.
   *
   * @param position The index position of the name
   * @return The name at the given position
   */
  default String name(int position) {
    throw new UnsupportedOperationException();
  }

  /**
   * Return the hash of the name when it is one of these names, otherwise a value that does not
   * match the hash of any of these names.
   *
   * @param name The property name
   * @return The property name hash, or an unmapped hash when colliding
   */
  default int knownHash(String name) {
    return JsonReader.nameHash(name);
  }
}
