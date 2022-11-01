package io.avaje.jsonb.generator.models.invalid;

import java.util.ArrayList;

import io.avaje.jsonb.Json;

@Json
public class InvalidCollection {
  private ArrayList<String> list;

  public ArrayList<String> getList() {
    return list;
  }

  public void setList(ArrayList<String> list) {
    this.list = list;
  }
}
