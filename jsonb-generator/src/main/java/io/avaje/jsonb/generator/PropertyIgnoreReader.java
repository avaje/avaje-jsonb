package io.avaje.jsonb.generator;

import javax.lang.model.element.Element;

final class PropertyIgnoreReader {

  private boolean ignoreSerialize;
  private boolean ignoreDeserialize;

  PropertyIgnoreReader(Element element) {

    final IgnorePrism ignored = IgnorePrism.getInstanceOn(element);
    if (ignored != null) {
      ignoreDeserialize = !ignored.deserialize();
      ignoreSerialize = !ignored.serialize();
    }
  }

  boolean serialize() {
    return !ignoreSerialize;
  }

  boolean deserialize() {
    return !ignoreDeserialize;
  }
}
