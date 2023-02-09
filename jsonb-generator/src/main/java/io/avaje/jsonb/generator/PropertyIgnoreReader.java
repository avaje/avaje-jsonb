package io.avaje.jsonb.generator;

import javax.lang.model.element.Element;

final class PropertyIgnoreReader {

  private final boolean unmapped;
  private final boolean raw;
  private boolean ignoreSerialize;
  private boolean ignoreDeserialize;

  PropertyIgnoreReader(Element element) {
    unmapped = UnmappedPrism.getInstanceOn(element) != null;
    raw = RawPrism.getInstanceOn(element) != null;

    final IgnorePrism ignored = IgnorePrism.getInstanceOn(element);
    if (ignored != null) {
      ignoreDeserialize = !ignored.deserialize();
      ignoreSerialize = !ignored.serialize();
    }
  }

  boolean unmapped() {
    return unmapped;
  }

  boolean raw() {
    return raw;
  }

  boolean serialize() {
    return !ignoreSerialize;
  }

  boolean deserialize() {
    return !ignoreDeserialize;
  }
}
