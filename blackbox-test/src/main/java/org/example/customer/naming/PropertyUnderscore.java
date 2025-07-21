package org.example.customer.naming;

import java.util.Objects;

import io.avaje.jsonb.Json;
import io.avaje.jsonb.Json.Naming;

@Json(naming = Naming.LowerUnderscore)
public class PropertyUnderscore {

  @Json.Property("name")
  private final String _name;

  @Json.Property("email")
  private final String _email;

  @Json.Property("setter")
  private String _setter;

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

  public String getSetter() {
    return _setter;
  }

  public void setSetter(String setter) {
    this._setter = setter;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_email, _name, _setter);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if ((obj == null) || (getClass() != obj.getClass())) return false;
    PropertyUnderscore other = (PropertyUnderscore) obj;
    return Objects.equals(_email, other._email)
        && Objects.equals(_name, other._name)
        && Objects.equals(_setter, other._setter);
  }
}
