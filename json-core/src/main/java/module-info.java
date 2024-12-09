module io.avaje.json {

  exports io.avaje.json;
  exports io.avaje.json.core to io.avaje.jsonb;
  exports io.avaje.json.stream;
  exports io.avaje.json.stream.core;
  exports io.avaje.json.view;

  requires static io.helidon.webserver;
}
