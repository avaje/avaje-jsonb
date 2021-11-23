module io.avaje.jsonb.generator {

  requires java.compiler;
  requires io.avaje.jsonb;

  provides javax.annotation.processing.Processor with io.avaje.jsonb.generator.Processor;
}
