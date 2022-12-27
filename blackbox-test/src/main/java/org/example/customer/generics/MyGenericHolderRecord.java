package org.example.customer.generics;

import io.avaje.jsonb.Json;

@Json
public record MyGenericHolderRecord<T>(String title, String author, T document) {}
