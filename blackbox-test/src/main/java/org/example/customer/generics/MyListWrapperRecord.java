package org.example.customer.generics;

import java.util.List;

import io.avaje.jsonb.Json;

@Json
public record MyListWrapperRecord<T>(List<T> list) {}
