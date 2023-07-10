package org.example.customer.subtype;

import io.avaje.jsonb.Json;

@Json
@Json.SubType(type = MoveAccuracy.Unavoidable.class, name="UNAVOIDABLE")
@Json.SubType(type = MoveAccuracy.Avoidable.class, name="AVOIDABLE")
public sealed interface MoveAccuracy {
    record Unavoidable() implements MoveAccuracy {}
    record Avoidable(int percentToHit) implements MoveAccuracy {}
}