
module io.avaje.jsonb {

  exports io.avaje.jsonb;
  exports io.avaje.jsonb.spi;

  requires static com.fasterxml.jackson.core;
  requires transitive org.slf4j;

  uses io.avaje.jsonb.spi.Bootstrap;
  uses io.avaje.jsonb.Jsonb.Component;

}
