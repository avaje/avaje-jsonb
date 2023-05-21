package io.avaje.jsonb.generator.models.valid;

import java.util.ArrayList;

import io.avaje.jsonb.Json;

@Json
public class ArrayListCollection {
  private ArrayList<String> list;

  public ArrayList<String> getList() {
    return list;
  }

  public void setList(ArrayList<String> list) {
    this.list = list;
  }
}
