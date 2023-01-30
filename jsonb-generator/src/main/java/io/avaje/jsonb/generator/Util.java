package io.avaje.jsonb.generator;

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

  /**
   * Return the common parent package.
   */
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

  static String trimQuotes(String value) {
    return value.substring(1, value.length() - 1);
  }

  /**
   * Trim off the .class suffix.
   */
  static String trimClassSuffix(String nameWithSuffix) {
    return nameWithSuffix.substring(0, nameWithSuffix.length() - 6);
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

  /**
   * Return the base type given the JsonAdapter type.
   * Remove the "jsonb" sub-package and the "JsonAdapter" suffix.
   */
  static String baseTypeOfAdapter(String adapterFullName) {
    int posLast = adapterFullName.lastIndexOf('.');
    int posPrior = adapterFullName.lastIndexOf('.', posLast - 1);
    int nameEnd = adapterFullName.length() - 11; // "JsonAdapter".length();
    if (posPrior == -1) {
      return adapterFullName.substring(posLast + 1, nameEnd);
    }

    final String className = adapterFullName.substring(0, posPrior) + adapterFullName.substring(posLast, nameEnd);
    final int $index = className.indexOf("$");
    if ($index != -1) {
      return className.substring(0, $index);
    }
    return className;
  }
}
