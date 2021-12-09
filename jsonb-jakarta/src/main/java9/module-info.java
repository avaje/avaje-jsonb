
module io.avaje.jsonb.jakarta {

  requires transitive io.avaje.jsonb;
  requires transitive jakarta.json;

  exports io.avaje.jsonb.jakarta;

  provides io.avaje.jsonb.spi.IOAdapterFactory with io.avaje.jsonb.jakarta.JakartaAdapterFactory;
}
