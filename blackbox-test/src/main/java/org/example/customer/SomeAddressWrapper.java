package org.example.customer;

import io.avaje.jsonb.Json;

import java.util.Collections;
import java.util.List;

@Json
public record SomeAddressWrapper(Long id, Address address, List<String> tags) {

  /**
   * Additional constructor.
   */
  public SomeAddressWrapper(long id, Address address) {
    this(id, address, Collections.emptyList());
  }
}
