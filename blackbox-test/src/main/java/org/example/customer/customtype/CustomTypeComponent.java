package org.example.customer.customtype;


import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.spi.JsonbComponent;
import io.avaje.spi.ServiceProvider;

/**
 * Register via service loading.
 */
@ServiceProvider
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

