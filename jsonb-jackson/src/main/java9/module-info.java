
module io.avaje.jsonb.jackson {

  requires transitive io.avaje.jsonb;
  requires transitive com.fasterxml.jackson.core;

  exports io.avaje.jsonb.jackson;

  provides io.avaje.jsonb.spi.IOAdapterFactory with io.avaje.jsonb.jackson.JacksonFactory;
}
