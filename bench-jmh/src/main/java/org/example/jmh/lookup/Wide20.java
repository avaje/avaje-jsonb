package org.example.jmh.lookup;

import io.avaje.jsonb.Json;

@Json
public record Wide20(
  String id,
  String firstName,
  String lastName,
  String emailAddress,
  String phoneNumber,
  String streetAddress,
  String city,
  String region,
  String postalCode,
  String country,
  String companyName,
  String jobTitle,
  String department,
  String managerId,
  String status,
  String createdBy,
  String createdAt,
  String notes,
  int score,
  boolean active
) {
}
