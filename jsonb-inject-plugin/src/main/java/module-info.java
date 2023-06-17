module io.avaje.jsonb.plugin {

  requires transitive io.avaje.jsonb;
  requires transitive io.avaje.inject;

  provides io.avaje.inject.spi.Plugin with io.avaje.jsonb.inject.DefaultJsonbProvider;
}
