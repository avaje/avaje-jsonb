package org.example.customer.cascade;

import io.avaje.jsonb.Json;

import java.util.List;

@Json
public record MCTop(int id, MCOther other, List<MCChild> children) {
}
