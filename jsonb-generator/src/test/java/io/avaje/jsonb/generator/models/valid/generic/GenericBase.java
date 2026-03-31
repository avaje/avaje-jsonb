package io.avaje.jsonb.generator.models.valid.generic;

public abstract class GenericBase<T> {

  private T config;

  public GenericBase(T config) {
    this.config = config;
  }

  public T getConfig() {
    return config;
  }

  public void setConfig(T config) {
    this.config = config;
  }
}
