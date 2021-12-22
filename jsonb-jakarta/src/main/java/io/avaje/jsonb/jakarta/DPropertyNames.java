package io.avaje.jsonb.jakarta;

import io.avaje.jsonb.spi.PropertyNames;
import jakarta.json.stream.JsonGenerator;

/**
 * Ideally this would be able to "prepare" names/keys to escaped and encoded raw bytes as a
 * performance optimisation. Currently, we don't get that optimisation here with jakarta json.
 */
final class DPropertyNames implements PropertyNames {

  final JsonGenerator.Key[] names;

  DPropertyNames(JsonGenerator.Key[] names) {
    this.names = names;
  }

  JsonGenerator.Key key(int position) {
    return names[position];
  }
}
