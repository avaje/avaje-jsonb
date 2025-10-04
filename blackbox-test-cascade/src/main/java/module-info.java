module blackbox.test.cascade {

  requires static io.avaje.jsonb;
  requires static io.avaje.spi;
  requires blackbox.test;

  provides io.avaje.jsonb.spi.JsonbExtension with org.example.cascade.jsonb.GeneratedJsonComponent;
}
