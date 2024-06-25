
module io.avaje.jsonb {

  exports io.avaje.jsonb;
  exports io.avaje.jsonb.stream;
  exports io.avaje.jsonb.spi;

  uses io.avaje.jsonb.spi.JsonbExtension;

  requires static io.helidon.webserver;
  requires static io.avaje.spi;

}
