module io.avaje.jsonb.generator {

  requires java.compiler;
  requires static io.avaje.json;
  requires static io.avaje.jsonb;
  requires static io.avaje.prism;

  provides javax.annotation.processing.Processor with io.avaje.jsonb.generator.JsonbProcessor;
}
