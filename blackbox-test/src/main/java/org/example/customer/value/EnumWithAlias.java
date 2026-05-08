package org.example.customer.value;

import io.avaje.jsonb.Json;

@Json
public enum EnumWithAlias {
  @Json.Alias({"kAlternateName", "kOldName"})
  kNewName,
  kOther;

  @Json.Value
  @Override
  public String toString() {
    return super.toString();
  }
}
