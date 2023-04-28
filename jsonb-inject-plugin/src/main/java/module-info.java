module io.avaje.jsonb.plugin {

  requires io.avaje.jsonb;
  requires io.avaje.inject;

  provides io.avaje.inject.spi.Plugin with io.avaje.jsonb.inject.DefaultJsonbProvider;
}
