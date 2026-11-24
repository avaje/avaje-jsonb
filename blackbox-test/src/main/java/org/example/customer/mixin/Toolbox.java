package org.example.customer.mixin;

import java.util.List;

import io.avaje.jsonb.Json;

@Json
public record Toolbox(Tool main, List<Tool> others) {}
