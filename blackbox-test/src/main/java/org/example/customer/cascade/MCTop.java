package org.example.customer.cascade;

import io.avaje.jsonb.Json;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Json
public record MCTop(int id, MCOther other, List<MCChild> children, Map<String,MCChild2> childMap) {

  public MCTop(int id, MCOther foo, List<MCChild> children) {
    this(id, foo, children, Collections.emptyMap());
  }
}
