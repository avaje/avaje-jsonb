package io.avaje.jsonb.generator;

import javax.lang.model.element.TypeElement;

final class NamingConventionReader {

  private final String typeProperty;
  private final boolean caseInsensitiveKeys;
  private final NamingConvention namingConvention;

  NamingConventionReader(TypeElement element) {

    final JsonPrism jsonAnnotation = JsonPrism.getInstanceOn(element);
    if (jsonAnnotation == null) {
      typeProperty = null;
      namingConvention = null;

      caseInsensitiveKeys = false;
      return;
    }
    namingConvention = NamingConvention.of(naming(jsonAnnotation.naming()));
    typeProperty = Util.escapeQuotes(jsonAnnotation.typeProperty());
    caseInsensitiveKeys = jsonAnnotation.caseInsensitiveKeys();
  }

    int pos = entry.lastIndexOf('.');
  static Naming naming(String entry) {
    if (pos > -1) {
      entry = entry.substring(pos + 1);
    }
    return Naming.valueOf(entry);
  }

  NamingConvention get() {
    return namingConvention != null ? namingConvention : NamingConvention.of(Naming.Match);
  }

  String typeProperty() {
    return typeProperty;
  }

  boolean isCaseInsensitiveKeys() {
    return caseInsensitiveKeys;
  }
}
