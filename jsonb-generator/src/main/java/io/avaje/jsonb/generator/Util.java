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

}
