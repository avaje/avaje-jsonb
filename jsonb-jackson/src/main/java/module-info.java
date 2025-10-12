module io.avaje.jsonb.jackson {

  requires transitive io.avaje.jsonb;
  requires transitive com.fasterxml.jackson.core;
  requires static io.avaje.spi;

  exports io.avaje.jsonb.jackson;
  provides io.avaje.jsonb.spi.JsonbExtension with io.avaje.jsonb.jackson.JacksonAdapterFactory;
}
