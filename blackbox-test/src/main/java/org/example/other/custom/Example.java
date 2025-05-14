package org.example.other.custom;

import java.util.List;

import io.avaje.jsonb.Json;

@Json
public class Example {
  private int code;
  private WrapMap map;
  private List<WrapMap2> map2;

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public WrapMap getMap() {
    return map;
  }

  public void setMap(WrapMap map) {
    this.map = map;
  }

  public List<WrapMap2> getMap2() {
    return map2;
  }

  public void setMap2(List<WrapMap2> map2) {
    this.map2 = map2;
  }
}
