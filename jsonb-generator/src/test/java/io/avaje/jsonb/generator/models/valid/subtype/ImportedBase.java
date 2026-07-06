package io.avaje.jsonb.generator.models.valid.subtype;

import io.avaje.jsonb.Json;
import io.avaje.jsonb.generator.models.valid.subtype.subpackage.Imported;

@Json
@Json.SubType(type = Imported.Subtype.class)
public interface ImportedBase {
  String getName();
}
