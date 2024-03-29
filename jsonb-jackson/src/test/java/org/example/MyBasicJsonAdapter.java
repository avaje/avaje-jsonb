package org.example;

import io.avaje.jsonb.JsonAdapter;
import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.spi.Generated;
import io.avaje.jsonb.spi.PropertyNames;
import io.avaje.jsonb.spi.ViewBuilder;
import io.avaje.jsonb.spi.ViewBuilderAware;

import java.lang.invoke.MethodHandle;

@Generated
public final class MyBasicJsonAdapter implements JsonAdapter<StreamBasicTest.MyBasic>, ViewBuilderAware {

  // naming convention Match
  // id [int] name:id constructor
  // name [java.lang.String] name:name constructor

  private final JsonAdapter<Integer> pintJsonAdapter;
  private final JsonAdapter<String> stringJsonAdapter;
  private final PropertyNames names;

  public MyBasicJsonAdapter(Jsonb jsonb) {
    this.pintJsonAdapter = jsonb.adapter(Integer.TYPE);
    this.stringJsonAdapter = jsonb.adapter(String.class);
    this.names = jsonb.properties("id", "name");
  }

  @Override
  public boolean isViewBuilderAware() {
    return true;
  }

  @Override
  public ViewBuilderAware viewBuild() {
    return this;
  }

  @Override
  public void build(ViewBuilder builder, String name, MethodHandle handle) {
    builder.beginObject(name, handle);
    builder.add("id", pintJsonAdapter, builder.method(StreamBasicTest.MyBasic.class, "id", int.class));
    builder.add("name", stringJsonAdapter, builder.method(StreamBasicTest.MyBasic.class, "name", String.class));
    builder.endObject();
  }

  @Override
  public void toJson(JsonWriter writer, StreamBasicTest.MyBasic myBasic) {
    writer.beginObject(names);
    writer.name(0);
    pintJsonAdapter.toJson(writer, myBasic.id);
    writer.name(1);
    stringJsonAdapter.toJson(writer, myBasic.name);
    writer.endObject();
  }

  @Override
  public StreamBasicTest.MyBasic fromJson(JsonReader reader) {
    // variables to read json values into, constructor params don't need _set$ flags
    int        _val$id = 0;
    String     _val$name = null;

    // read json
    reader.beginObject(names);
    while (reader.hasNextField()) {
      String fieldName = reader.nextField();
      switch (fieldName) {
        case "id": {
          _val$id = pintJsonAdapter.fromJson(reader); break;
        }
        case "name": {
          _val$name = stringJsonAdapter.fromJson(reader); break;
        }
        default: {
          reader.unmappedField(fieldName);
          reader.skipValue();
        }
      }
    }
    reader.endObject();

    // build and return MyBasic
    StreamBasicTest.MyBasic _$myBasic = new StreamBasicTest.MyBasic(_val$id, _val$name);
    return _$myBasic;
  }
}
