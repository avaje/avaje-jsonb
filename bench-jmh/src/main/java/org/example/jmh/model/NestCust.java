package org.example.jmh.model;

import io.avaje.jsonb.Json;

@Json
public record NestCust(
  long id,
  String name,
  String whenCreated,
  String whenModified,
  String notes,
  NestAddress billingAddress,
  NestAddress shippingAddress
) {
}
