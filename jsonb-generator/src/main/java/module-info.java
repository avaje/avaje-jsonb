module io.avaje.jsonb.generator {

  requires java.compiler;

  requires static io.avaje.json;
  requires static io.avaje.jsonb;
  requires static io.avaje.prism;

  // (optional support for other annotations)
  requires static com.fasterxml.jackson.annotation;
  requires static com.google.gson;
  requires static jakarta.json.bind;

  provides javax.annotation.processing.Processor with io.avaje.jsonb.generator.JsonbProcessor;
}
