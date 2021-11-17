package io.avaje.jsonb.core;

import java.util.*;

final class ViewDslParser {

  private final Context top;
  private StringBuilder sb = new StringBuilder();
  private Context current;

  ViewDslParser() {
    top = new Context(null);
    current = top;
  }

  ViewDsl parse(String dsl) {
    dsl = initialTrim(dsl);
    char[] chars = dsl.toCharArray();
    for (char ch : chars) {
      switch (ch) {
        case ',':
          tokenSplit();
          break;
        case '(':
          tokenBegin();
          break;
        case ')':
          tokenEnd();
        default:
          sb.append(ch);
      }
    }
    addToken();
    return new ViewDsl(top.build());
  }

  private String initialTrim(String dsl) {
    dsl = dsl.trim();
    if (dsl.startsWith("(") && dsl.endsWith(")")) {
      dsl = dsl.substring(1, dsl.length() - 1).trim();
    }
    return dsl;
  }

  private void tokenEnd() {
    addToken();
    current = current.pop();
  }

  private void tokenBegin() {
    addToken();
    current = current.push(current.last());
  }

  private void tokenSplit() {
    addToken();
  }

  private void addToken() {
    if (sb.length() > 0) {
      current.add(sb.toString().trim());
      sb = new StringBuilder();
    }
  }

  static class Context {

    private final Context parent;
    private final Set<String> tokens = new LinkedHashSet<>();
    private final Map<String, Context> nested = new LinkedHashMap<>();
    private String last;

    Context(Context parent) {
      this.parent = parent;
    }

    void add(String token) {
      last = token;
      tokens.add(token);
    }

    String last() {
      return last;
    }

    Context push(String last) {
      Context child = new Context(this);
      nested.put(last, child);
      return child;
    }

    Context pop() {
      return parent;
    }

    ViewDsl.Entry build() {
      Map<String, ViewDsl.Entry> children = new LinkedHashMap<>();
      for (Map.Entry<String, Context> entry : nested.entrySet()) {
        children.put(entry.getKey(), entry.getValue().build());
      }
      return new ViewDsl.Entry(tokens, children);
    }
  }


}
