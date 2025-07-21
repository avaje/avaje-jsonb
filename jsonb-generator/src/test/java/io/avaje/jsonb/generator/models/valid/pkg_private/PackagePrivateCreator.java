package io.avaje.jsonb.generator.models.valid.pkg_private;

import io.avaje.jsonb.Json;
import io.avaje.jsonb.Json.Creator;

@Json
public class PackagePrivateCreator {

  Long id;
  String street;
  String suburb;
  String city;
  String funky;

  @Creator
  PackagePrivateCreator(Long id, String street, String suburb, String city) {
    this.id = id;
    this.street = street;
    this.suburb = suburb;
    this.city = city;
  }
}
