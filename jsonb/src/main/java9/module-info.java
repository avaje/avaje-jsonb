
module io.avaje.jsonb {

  exports io.avaje.jsonb;
  exports io.avaje.jsonb.spi;

  requires com.fasterxml.jackson.core;

  uses io.avaje.jsonb.spi.Bootstrap;
  uses io.avaje.jsonb.Jsonb.Component;

}
