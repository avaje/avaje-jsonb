package org.example.customer;

import io.avaje.jsonb.Json;

import java.util.Map;

@Json
public record ALBResponse2 (int one, Map<String, String> map1, int two, Map<String, String> map2, String three) {

}
