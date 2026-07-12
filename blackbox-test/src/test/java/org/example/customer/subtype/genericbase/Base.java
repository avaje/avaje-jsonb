package org.example.customer.subtype.genericbase;

public abstract class Base<T> implements Action {

  private T genericConfig;

  protected Base(T genericConfig) {
    this.genericConfig = genericConfig;
  }

  public T getGenericConfig() {
    return genericConfig;
  }

  public void setGenericConfig(T genericConfig) {
    this.genericConfig = genericConfig;
  }
}
