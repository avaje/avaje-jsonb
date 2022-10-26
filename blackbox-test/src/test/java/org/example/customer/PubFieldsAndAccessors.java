package org.example.customer;

import io.avaje.jsonb.Json;

@Json
public class PubFieldsAndAccessors {

  public String name;
  public int score;

  // match the setter for isActive to setActive()
  private boolean isActive;

  // match the getter for isRegistered to getRegistered()
  private Boolean isRegistered;

  public String getName() {
    return name;
  }

  public PubFieldsAndAccessors setName(String name) {
    this.name = name;
    return this;
  }

  public int getScore() {
    return score;
  }

  public PubFieldsAndAccessors setScore(int score) {
    this.score = score;
    return this;
  }

  public boolean isActive() {
    return isActive;
  }

  public PubFieldsAndAccessors setActive(boolean active) {
    isActive = active;
    return this;
  }

  public Boolean getRegistered() {
    return isRegistered;
  }

  public void setRegistered(Boolean registered) {
    isRegistered = registered;
  }
}
