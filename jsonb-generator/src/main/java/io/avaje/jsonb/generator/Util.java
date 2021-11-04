package io.avaje.jsonb.generator;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

class Util {

  private static final String NULLABLE = "Nullable";

  static boolean validImportType(String type) {
    return type.indexOf('.') > 0;
  }

  static String trimGenerics(String type) {
    int i = type.indexOf('<');
    if (i == -1) {
      return type;
    }
    return type.substring(0, i);
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
    if (currentTop == null) return aPackage;
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


  /**
   * Return true if the element has a Nullable annotation.
   */
  public static boolean isNullable(Element p) {
    for (AnnotationMirror mirror : p.getAnnotationMirrors()) {
      if (NULLABLE.equals(shortName(mirror.getAnnotationType().toString()))) {
        return true;
      }
    }
    return false;
  }

  static String initLower(String name) {
    StringBuilder sb = new StringBuilder(name.length());
    boolean upper = true;
    for (char ch : name.toCharArray()) {
      if (upper && Character.isUpperCase(ch)) {
        sb.append(Character.toLowerCase(ch));
      } else {
        upper = false;
        sb.append(ch);
      }
    }
    return sb.toString();
  }

  public static String initcap(String input) {
    if (input.length() < 2) {
      return input.toUpperCase();
    } else {
      return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }
  }

  /**
   * Trim off the .class suffix.
   */
  static String trimClassSuffix(String nameWithSuffix) {
    return nameWithSuffix.substring(0, nameWithSuffix.length() - 6);
  }
}
