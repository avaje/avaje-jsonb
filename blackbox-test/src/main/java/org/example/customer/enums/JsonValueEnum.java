package org.example.customer.enums;

import io.avaje.jsonb.Json;

/**
 * An enum annotated with both {@code @Json} and {@code @Json.Value}.
 *
 * <p>{@code @Json.Value} is what drives generation of a custom adapter here
 * - {@code @Json} on the type itself has no additional effect.
 */
@Json
public enum JsonValueEnum {
  ONE("one-code"),
  TWO("two-code");

  public final String code;

  JsonValueEnum(String code) {
    this.code = code;
  }

  @Json.Value
  public String getCode() {
    return code;
  }
}
