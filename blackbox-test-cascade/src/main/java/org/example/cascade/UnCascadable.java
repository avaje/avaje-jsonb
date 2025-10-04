package org.example.cascade;

import org.example.customer.cascade.CascadeNonAccessible;
import org.example.customer.cascade.MCTop;

import io.avaje.jsonb.Json;

@Json
public record UnCascadable(MCTop top, CascadeNonAccessible access) {}
