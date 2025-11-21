package org.example.customer;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.Json;
import io.avaje.jsonb.Jsonb;

public class PrettyNestedTest {
  @Json
  public record DataContainer(String id, Instant createdAt, List<App> apps) {
    public record App(
        String id, Instant createdAt, Instant updatedAt, String name, List<String> roles) {}
  }

  @Test
  void testNested() {
    final String pretty =
        """
  	{
  	  "id": "f11177d2-ec63-3995-bb4a-c628e0d782df",
  	  "createdAt": "1970-01-01T00:00:01Z",
  	  "apps": [
  	    {
  	      "id": "ea4959eb-64a1-309b-a580-d950964f3843",
  	      "createdAt": "1970-01-01T00:00:01Z",
  	      "updatedAt": "1970-01-01T00:00:01Z",
  	      "name": "Name",
  	      "roles": [
  	        "admin"
  	      ]
  	    },
  	    {
  	      "id": "ea4959eb-64a1-309b-a580-d950964f3843",
  	      "createdAt": "1970-01-01T00:00:01Z",
  	      "updatedAt": "1970-01-01T00:00:01Z",
  	      "name": "Name",
  	      "roles": [
  	        "admin"
  	      ]
  	    }
  	  ]
  	}""";

    var type = Jsonb.instance().type(DataContainer.class);
   String jsonPretty = type.toJsonPretty(type.fromJson(pretty));
   assertThat(jsonPretty).isEqualTo(pretty);
  }
}
