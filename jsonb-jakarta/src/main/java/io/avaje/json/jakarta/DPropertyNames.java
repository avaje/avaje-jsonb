package io.avaje.json.jakarta;

import io.avaje.jsonb.spi.PropertyNames;

/**
 * Ideally this would be able to "prepare" names/keys to escaped and encoded raw bytes as a
 * performance optimisation. Currently we don't get that optimisation here with jakarta json.
 */
final class DPropertyNames implements PropertyNames {

  final String[] names;

  DPropertyNames(String[] names) {
    this.names = names;
  }

  String key(int position) {
    return names[position];
  }
}
