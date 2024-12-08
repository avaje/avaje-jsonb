module io.avaje.jsonb {

  exports io.avaje.jsonb;
  exports io.avaje.jsonb.spi;

  uses io.avaje.jsonb.spi.JsonbExtension;

  requires transitive io.avaje.json;
  requires static io.avaje.spi;

}
