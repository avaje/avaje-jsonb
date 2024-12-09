package io.avaje.jsonb.core;

import io.avaje.json.core.CoreTypes;
import io.avaje.jsonb.AdapterFactory;

final class CoreAdapters {

  static final AdapterFactory FACTORY = (type, jsonb) -> CoreTypes.create(type);

}
