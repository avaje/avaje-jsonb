package org.example.customer.hash;

import java.util.LinkedHashMap;
import java.util.Map;

import io.avaje.jsonb.Json;

public interface Collide {

  @Json
  record AdminRecord(String name, boolean admin) {}

  @Json
  record AdminAlias(String name, @Json.Alias("isAdmin") boolean admin) {}

  @Json
  class AdminUnmapped {
    public String name;
    public boolean admin;
    @Json.Unmapped
    public Map<String, Object> unmapped = new LinkedHashMap<>();
  }

  @Json
  class AdminBean {
    private String name;
    private boolean admin;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public boolean isAdmin() {
      return admin;
    }

    public void setAdmin(boolean admin) {
      this.admin = admin;
    }
  }
}
