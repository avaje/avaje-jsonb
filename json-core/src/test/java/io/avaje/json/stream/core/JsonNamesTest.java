package io.avaje.json.stream.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.json.JsonReader;

class JsonNamesTest {

  @Test
  void of() {

    JsonNames names = JsonNames.of("one", "two", "three");

    assertThat(names.lookup(23)).isNull();
    assertThat(names.lookup(-23)).isNull();

    assertThat(names.lookup(JsonReader.nameHash("one"))).isEqualTo("one");
    assertThat(names.lookup(JsonReader.nameHash("two"))).isEqualTo("two");
    assertThat(names.lookup(JsonReader.nameHash("three"))).isEqualTo("three");
  }

  @Test
  void moreNames() {
    JsonNames names = JsonNames.of("statusCode", "isBase64Encoded", "headers", "body");

    check(names, "statusCode");
    check(names, "isBase64Encoded");
    check(names, "headers");
    check(names, "body");
  }

  private static void check(JsonNames names, String key) {
    String statusCode = names.lookup(JsonReader.nameHash(key));
    assertThat(statusCode).isEqualTo(key);
  }

}
