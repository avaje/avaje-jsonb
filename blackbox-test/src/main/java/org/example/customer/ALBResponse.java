package org.example.customer;

import io.avaje.jsonb.Json;

import java.util.Map;

@Json
public class ALBResponse {

  private final int statusCode;
  private final boolean isBase64Encoded;
  private final Map<String, String> headers;
  private final String body;

  public ALBResponse(int statusCode, boolean isBase64Encoded, Map<String, String> headers, String body) {
    this.statusCode = statusCode;
    this.isBase64Encoded = isBase64Encoded;
    this.headers = headers;
    this.body = body;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public boolean isBase64Encoded() {
    return isBase64Encoded;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public String getBody() {
    return body;
  }
}
