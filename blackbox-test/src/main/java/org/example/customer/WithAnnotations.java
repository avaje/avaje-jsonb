package org.example.customer;

import io.avaje.jsonb.Json;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Json
public class WithAnnotations {

  @Size(max = 50)
  final String one;

  @NotNull @Size(min = 5, max = 10)
  final String two;

  public WithAnnotations(String one, String two) {
    this.one = one;
    this.two = two;
  }

  public String one() {
    return one;
  }

  public String two() {
    return two;
  }
}
