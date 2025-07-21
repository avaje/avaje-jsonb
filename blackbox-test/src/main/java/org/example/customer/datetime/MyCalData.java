package org.example.customer.datetime;

import io.avaje.jsonb.Json;

import java.util.Calendar;

@Json
public record MyCalData(Calendar cal) {
}
