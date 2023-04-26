module io.avaje.jsonb.plugin {

  requires static io.avaje.jsonb;

  requires static io.avaje.inject;

  provides io.avaje.inject.spi.Plugin with io.avaje.jsonb.inject.DefaultJsonbProvider;
}
