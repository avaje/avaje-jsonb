package org.example.customer.enums;

import io.avaje.jsonb.Json;

/**
 * A plain enum annotated with {@code @Json} (ignored by the generator).
 */
@Json
public enum PlainJsonEnum {
  ONE,
  TWO
}
