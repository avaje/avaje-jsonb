package io.avaje.jsonb.jackson;

import com.fasterxml.jackson.core.io.SerializedString;

import java.util.concurrent.ConcurrentHashMap;

class NameCache {

  private final ConcurrentHashMap<String, SerializedString> keys = new ConcurrentHashMap<>();

  SerializedString get(String name) {
    return keys.computeIfAbsent(name, _name -> {
      SerializedString val = new SerializedString(_name);
      val.asQuotedUTF8();
      return val;
    });
  }
}
