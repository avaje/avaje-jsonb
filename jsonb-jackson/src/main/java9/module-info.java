
module io.avaje.jsonb.jackson {

  requires transitive io.avaje.jsonb;
  requires transitive com.fasterxml.jackson.core;

  exports io.avaje.jsonb.jackson;

  provides io.avaje.jsonb.spi.AdapterFactory with io.avaje.jsonb.jackson.JacksonAdapterFactory;
}
