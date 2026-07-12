package org.example.customer.subtype.genericbase;

import io.avaje.jsonb.Json;

@Json
@Json.SubTypes({
    @Json.SubType(type = Test1.class, name = "Test1"),
    @Json.SubType(type = Test2.class, name = "Test2"),
    @Json.SubType(type = Test3.class, name = "Test3")
})
public interface Action {
  default void execute() {}
}
