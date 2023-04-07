package org.example.customer.customtype;


import io.avaje.jsonb.*;

/**
 * Register via service loading.
 */
public class CustomTypeComponent implements JsonbComponent {

  @Override
  public void register(Jsonb.Builder builder) {
    builder.add(MyCustomScalarType.class, new CustomTypeAdapterWithStar().nullSafe());
  }

  static class CustomTypeAdapterWithStar implements JsonAdapter<MyCustomScalarType> {

    @Override
    public void toJson(JsonWriter writer, MyCustomScalarType value) {
      writer.value("*** " + value.toString());
    }

    @Override
    public MyCustomScalarType fromJson(JsonReader reader) {
      String encoded = reader.readString();
      return MyCustomScalarType.of(encoded.substring(4)); // trim those stars
    }
  }

}

