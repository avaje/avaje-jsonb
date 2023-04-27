
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

  //JDK doesn't allow optional serviceloader
  //so inject has it's own ServiceLoader impl that will optionally load
  //provides io.avaje.inject.spi.Plugin with io.avaje.jsonb.spi.DefaultJsonbProvider;
}
