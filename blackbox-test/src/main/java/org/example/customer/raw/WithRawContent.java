package org.example.customer.raw;

import io.avaje.jsonb.Json;

@Json
public class WithRawContent {

  final long id;

  final String name;

  @Json.Raw
  String content;

  public WithRawContent(long id, String name) {
    this.id = id;
    this.name = name;
  }

  public long id() {
    return id;
  }

  public String name() {
    return name;
  }

  public String content() {
    return content;
  }

  public WithRawContent content(String content) {
    this.content = content;
    return this;
  }
}
