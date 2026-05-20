package org.cascade.custom;

import io.avaje.jsonb.Json;
import org.example.other.custom.CustomClass;

@Json
public record WithExternalCustomAdapter(String name, CustomClass custom) {}
