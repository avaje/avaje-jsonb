package org.example.customer.creator;

import io.avaje.jsonb.Json;

@Json
public class LinkedGetter {
  private String abbr;
  public String other;

  public LinkedGetter(String abbr, String other) {
    this.abbr = abbr;
    this.other = other;
  }

  @Json.Property("abbr")
  public String getAbbreviated() {
    return abbr;
  }
}
