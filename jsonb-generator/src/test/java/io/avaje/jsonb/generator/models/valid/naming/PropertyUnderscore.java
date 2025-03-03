package io.avaje.jsonb.generator.models.valid.naming;

import io.avaje.jsonb.Json;
import io.avaje.jsonb.Json.Naming;

@Json(naming = Naming.LowerUnderscore)
public class PropertyUnderscore {

  @Json.Property("name")
  private final String _name;

  @Json.Property("email")
  private final String _email;

  public PropertyUnderscore(String name, String email) {
    this._name = name;
    this._email = email;
  }

  public String getName() {
    return _name;
  }

  public String getEmail() {
    return _email;
  }
}
