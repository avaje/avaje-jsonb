package io.avaje.jsonb.generator.models.invalid;

import java.util.AbstractMap.SimpleImmutableEntry;

import io.avaje.jsonb.Json;

@Json
@Json.Import(SimpleImmutableEntry.class)
public class InvalidImport {
}
