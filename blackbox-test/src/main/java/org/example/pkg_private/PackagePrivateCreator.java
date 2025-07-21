package org.example.pkg_private;

import io.avaje.jsonb.Json;
import io.avaje.jsonb.Json.Creator;

@Json
public class PackagePrivateCreator {

  long id;

  @Creator
  PackagePrivateCreator(long id) {
    this.id = id;
  }
}
