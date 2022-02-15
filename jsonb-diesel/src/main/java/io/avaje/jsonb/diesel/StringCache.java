package io.avaje.jsonb.diesel;

public interface StringCache {
  String get(char[] chars, int len);
}
