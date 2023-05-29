package io.avaje.jsonb.generator;

import static io.avaje.jsonb.generator.ProcessingContext.element;

final class Util {

  static boolean validImportType(String type) {
    return type.indexOf('.') > 0;
  }

  static String packageOf(String cls) {
    int pos = cls.lastIndexOf('.');
    return (pos == -1) ? "" : cls.substring(0, pos);
  }

  static String shortName(String fullType) {
    int p = fullType.lastIndexOf('.');
    if (p == -1) {
      return fullType;
    } else {
      return fullType.substring(p + 1);
    }
  }

  static String shortType(String fullType) {
    int p = fullType.lastIndexOf('.');
    if (p == -1) {
      return fullType;
    } else if (fullType.startsWith("java")) {
      return fullType.substring(p + 1);
    } else {
      var result = "";
      var foundClass = false;
      for (final String part : fullType.split("\\.")) {
        if (foundClass || Character.isUpperCase(part.charAt(0))) {
          foundClass = true;
          result += (result.isEmpty() ? "" : ".") + part;
        }
      }
      return result;
    }
  }

  static String trimAnnotations(String type) {
    final int pos = type.indexOf("@");
    if (pos == -1) {
      return type;
    }
    return type.substring(0, pos) + type.substring(type.lastIndexOf(' ') + 1);
  }
  /** Return the common parent package. */
  static String commonParent(String currentTop, String aPackage) {
    if (aPackage == null) return currentTop;
    if (currentTop == null) return packageOf(aPackage);
    if (aPackage.startsWith(currentTop)) {
      return currentTop;
    }
    int next;
    do {
      next = currentTop.lastIndexOf('.');
      if (next > -1) {
        currentTop = currentTop.substring(0, next);
        if (aPackage.startsWith(currentTop)) {
          return currentTop;
        }
      }
    } while (next > -1);

    return currentTop;
  }

  static String initCap(String input) {
    if (input.length() < 2) {
      return input.toUpperCase();
    } else {
      return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }
  }

  static String escapeQuotes(String input) {
    return input.replaceAll("^\"|\"$", "\\\\\"");
  }

  static String initLower(String name) {
    StringBuilder sb = new StringBuilder(name.length());
    boolean toLower = true;
    for (char ch : name.toCharArray()) {
      if (Character.isUpperCase(ch)) {
        if (toLower) {
          sb.append(Character.toLowerCase(ch));
        } else {
          sb.append(ch);
        }
      } else {
        sb.append(ch);
        toLower = false;
      }
    }
    return sb.toString();
  }

  /** Return the base type given the JsonAdapter type. */
  static String baseTypeOfAdapter(String adapterFullName) {
    return element(adapterFullName).getInterfaces().stream()
        .filter(t -> t.toString().contains("io.avaje.jsonb.JsonAdapter"))
        .findFirst()
        .map(Object::toString)
        .map(GenericType::parse)
        .map(GenericType::firstParamType)
        .map(Util::extractTypeWithNest)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Adapter: " + adapterFullName + " does not directly implement JsonAdapter"));
  }

  static String extractTypeWithNest(String fullType) {
    final int p = fullType.lastIndexOf('.');
    if (p == -1 || fullType.startsWith("java")) {
      return fullType;
    } else {
      final StringBuilder result = new StringBuilder();
      var foundClass = false;
      var firstClass = true;
      for (final String part : fullType.split("\\.")) {

        if (Character.isUpperCase(part.charAt(0))) {
          foundClass = true;
        }
        result.append(foundClass && !firstClass ? "$" : ".").append(part);
        if (foundClass) {
          firstClass = false;
        }
      }
      if (result.charAt(0) == '.') {
        result.deleteCharAt(0);
      }
      return result.toString();
    }
  }
}
