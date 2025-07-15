package io.avaje.jsonb.generator.models.valid;

import io.avaje.jsonb.Json;

@Json
public class GenericCascade {
  private GenericType<String> list;

  public GenericType<String> getList() {
    return list;
  }

  public void setList(GenericType<String> list) {
    this.list = list;
  }

  static class GenericType<T> {

    public T field;
  }
}
