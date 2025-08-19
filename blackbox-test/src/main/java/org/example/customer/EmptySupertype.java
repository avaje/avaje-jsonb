package org.example.customer;

import io.avaje.jsonb.Json;

@Json
@Json.SubType(type = EmptySupertype.SubtypeA.class, name = "a")
@Json.SubType(type = EmptySupertype.SubtypeB.class, name = "b")
public sealed interface EmptySupertype {
  record SubtypeA() implements EmptySupertype {}
  record SubtypeB() implements EmptySupertype {}
}
