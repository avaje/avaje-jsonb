module io.avaje.json {

  exports io.avaje.json;
  exports io.avaje.json.simple;
  exports io.avaje.json.stream;
  exports io.avaje.json.view;
  exports io.avaje.json.core to io.avaje.jsonb, io.avaje.json.node;

  requires static io.helidon.webserver;
}
