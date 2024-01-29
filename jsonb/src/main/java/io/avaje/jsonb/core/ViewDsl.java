package io.avaje.jsonb.core;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Set;

final class ViewDsl {

  private final Deque<Entry> stack = new ArrayDeque<>();
  private Entry current;

  static ViewDsl parse(String dsl) {
    return new ViewDslParser().parse(dsl);
  }

  ViewDsl(Entry top) {
    this.current = top;
  }

  boolean contains(String id) {
    return current.contains(id);
  }

  void push(String key) {
    stack.push(current);
    Entry nested = current.nested(key);
    if (nested == null) {
      throw new IllegalStateException("nest property "+key+" not found?");
    }
    current = nested;
  }

  void pop() {
    current = stack.pop();
  }

  static final class Entry {

    private final Set<String> tokens;
    private final Map<String, Entry> children;
    private final boolean wildcard;

    Entry(Set<String> tokens, Map<String, Entry> children) {
      this.tokens = tokens;
      this.wildcard = tokens.contains("*");
      this.children = children;
    }

    boolean contains(String key) {
      return wildcard || tokens.contains(key);
    }

    Entry nested(String key) {
      return children.get(key);
    }
  }
}
