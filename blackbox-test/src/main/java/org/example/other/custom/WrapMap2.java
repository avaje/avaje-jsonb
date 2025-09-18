package org.example.other.custom;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

public class WrapMap2 extends AbstractMap<String, String> {
  private final Map<String, String> delegate;

  public WrapMap2(Map<String, String> map) {
    delegate = map;
  }

  @Override
  public Set<Entry<String, String>> entrySet() {
    return delegate.entrySet();
  }
}
