package org.example.customer.jaxb;

import io.avaje.jsonb.Json;

import java.util.*;

@Json
public class MyJaxbType {

  private String name;

  private List<String> tags;
  private Set<String> tags2;
  private Collection<Long> tags3;


  public String name() {
    return name;
  }

  public MyJaxbType setName(String name) {
    this.name = name;
    return this;
  }

  public List<String> getTags() {
    if (tags == null) {
      tags = new ArrayList<>();
    }
    return tags;
  }

  public Set<String> getTags2() {
    if (tags2 == null) {
      tags2 = new LinkedHashSet<>();
    }
    return tags2;
  }

  public Collection<Long> getTags3() {
    if (tags3 == null) {
      tags3 = new TreeSet<>();
    }
    return tags3;
  }
}
