package org.example.other.custom;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

public class WrapMap extends AbstractMap<String, String> {
  private final Map<String, String> delegate;

  public WrapMap(Map<String, String> map) {
    delegate = map;
  }

  @Override
  public Set<Entry<String, String>> entrySet() {
    return delegate.entrySet();
  }
}
