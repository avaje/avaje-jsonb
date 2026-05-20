import org.example.other.custom.CustomClass;

import io.avaje.jsonb.CustomAdapter.AdaptedTypes;

@AdaptedTypes(CustomClass.class)
module blackbox.test.cascade {

  requires static io.avaje.jsonb;
  requires static io.avaje.spi;
  requires blackbox.test;

  provides io.avaje.jsonb.spi.JsonbExtension with org.cascade.jsonb.GeneratedJsonComponent;
}
