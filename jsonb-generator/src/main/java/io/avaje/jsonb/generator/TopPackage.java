package io.avaje.jsonb.generator;

import java.util.Collection;

final class TopPackage {

  private String topPackage = "";

  static String of(Collection<String> values) {
    return new TopPackage(values).value();
  }

  private String value() {
    return topPackage;
  }

  private TopPackage(Collection<String> values) {
    for (String pkg : values) {
      topPackage = Util.commonParent(topPackage, pkg);
    }
  }
}
