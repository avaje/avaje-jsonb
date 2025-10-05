package org.cascade.custom;

import org.cascade.custom.MagicNumber;
import org.cascade.custom.Ulid;

import io.avaje.jsonb.Json;

@Json
public record Example(MagicNumber magicNumber, Ulid ulid) {}