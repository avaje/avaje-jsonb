
module io.avaje.jsonb {

  exports io.avaje.jsonb;
  exports io.avaje.jsonb.stream;
  exports io.avaje.jsonb.spi;

  uses io.avaje.jsonb.spi.AdapterFactory;
  uses io.avaje.jsonb.spi.Bootstrap;
  uses io.avaje.jsonb.JsonbComponent;
  uses io.avaje.jsonb.Jsonb.GeneratedComponent;

  requires static io.helidon.webserver;

}
