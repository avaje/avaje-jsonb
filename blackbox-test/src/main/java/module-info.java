module blackbox.test {

  requires static io.avaje.jsonb;
  requires java.validation;

  provides io.avaje.jsonb.Jsonb.GeneratedComponent with org.example.jsonb.GeneratedJsonComponent;
}
