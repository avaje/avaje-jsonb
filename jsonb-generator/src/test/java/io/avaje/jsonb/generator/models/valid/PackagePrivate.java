package io.avaje.jsonb.generator.models.valid;

import io.avaje.jsonb.Json;

@Json
public class PackagePrivate {

  Long id;
  String street;
  String suburb;
  String city;
  String funky;

  PackagePrivate(Long id, String street, String suburb, String city) {
    this.id = id;
    this.street = street;
    this.suburb = suburb;
    this.city = city;
  }
}
