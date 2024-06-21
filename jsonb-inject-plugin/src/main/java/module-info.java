module io.avaje.jsonb.plugin {

  requires transitive io.avaje.jsonb;
  requires transitive io.avaje.inject;
  requires static io.avaje.spi;

  provides io.avaje.inject.spi.InjectExtension with io.avaje.jsonb.inject.DefaultJsonbProvider;
}
