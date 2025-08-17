package org.example.customer.stream;

import io.avaje.jsonb.Json;

import java.util.ArrayList;

@Json
public record MyArrayList(int id, ArrayList<String> names) {
}
