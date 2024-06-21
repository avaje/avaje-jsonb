module blackbox.test {

  requires static io.avaje.jsonb;
  requires java.validation;
  requires static io.avaje.spi;

  provides io.avaje.jsonb.spi.JsonbExtension with org.example.customer.customtype.CustomTypeComponent, org.example.jsonb.GeneratedJsonComponent;

}