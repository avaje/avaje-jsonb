package org.example.customer.enums;

import java.util.Objects;

import io.avaje.jsonb.Json;

@Json
public class Staff {

  private String name;
  private StaffStatus status;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public StaffStatus getStatus() {
    return status;
  }

  public void setStatus(StaffStatus status) {
    this.status = status;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, status);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if ((obj == null) || (getClass() != obj.getClass())) return false;
    final Staff other = (Staff) obj;
    return Objects.equals(name, other.name) && status == other.status;
  }
}
