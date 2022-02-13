package io.avaje.jsonb.diesel.read;

public interface StringCache {
  String get(char[] chars, int len);
}
