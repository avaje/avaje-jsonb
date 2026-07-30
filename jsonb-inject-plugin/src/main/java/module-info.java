module io.avaje.jsonb.plugin {

  requires transitive io.avaje.jsonb;
  requires static io.avaje.inject;

  provides io.avaje.inject.spi.InjectExtension with io.avaje.jsonb.inject.DefaultJsonbProvider;
}
