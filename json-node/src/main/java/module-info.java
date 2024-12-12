module io.avaje.json.node {

  exports io.avaje.json.node;

  requires transitive org.jspecify;
  requires transitive io.avaje.json;

  requires static io.avaje.jsonb;
  exports io.avaje.json.node.adapter to io.avaje.jsonb;
  provides io.avaje.jsonb.spi.JsonbComponent with io.avaje.json.node.adapter.JsonNodeComponent;
}
