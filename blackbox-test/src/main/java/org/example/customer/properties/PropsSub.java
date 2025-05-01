package org.example.customer.properties;

import io.avaje.jsonb.Json;

import java.util.Properties;

@Json
public record PropsSub(MyProperties props) {}
