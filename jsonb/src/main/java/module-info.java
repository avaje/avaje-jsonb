
module io.avaje.jsonb {

  exports io.avaje.jsonb;
  exports io.avaje.jsonb.stream;
  exports io.avaje.jsonb.spi;

  uses io.avaje.jsonb.spi.AdapterFactory;
  uses io.avaje.jsonb.spi.Bootstrap;
  uses io.avaje.jsonb.JsonbComponent;
  uses io.avaje.jsonb.Jsonb.GeneratedComponent;

  requires static io.avaje.inject;
  requires static io.helidon.nima.webserver;

  provides io.avaje.inject.spi.Plugin with io.avaje.jsonb.spi.DefaultJsonbProvider;
}
