package org.example.customer.subtype;

import io.avaje.jsonb.Json;
import org.example.customer.subtype.subpackage.Imported;

@Json
@Json.SubType(type = Imported.Subtype.class)
public interface ImportedBase {
  String getName();
}
