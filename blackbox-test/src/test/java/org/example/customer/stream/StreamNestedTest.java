package org.example.customer.stream;

import io.avaje.json.*;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

class StreamNestedTest {

  @Test
  void nested() {

    String jsonContent = """
      [
      {"nest":1, "desc":"d1", "innerStream": [{"id":1,"name":"a"},{"id":2,"name":"b"}] },
      {"nest":2, "desc":"d2", "innerStream": [{"id":3,"name":"c"}] },
      {"nest":3, "desc":"d3", "innerStream": [{"id":4,"name":"d"},{"id":5,"name":"e"}] }
      ]
      """;

    Jsonb jsonb = Jsonb.builder().build();
    JsonType<Stream<MyNested>> stream = jsonb.type(MyNested.class).stream();

    try (JsonReader reader = jsonb.reader(jsonContent)) {

      //reader.

      Stream<MyNested> myNestedStream = stream.fromJson(reader);
      myNestedStream.forEach( myNested -> {
        int nest = myNested.nest();
        System.out.println(nest + " " + myNested);
      });
    }

  }
}
