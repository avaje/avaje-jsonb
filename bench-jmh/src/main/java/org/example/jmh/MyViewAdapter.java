package org.example.jmh;

import io.avaje.jsonb.*;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.time.Instant;
import java.util.List;

import io.avaje.jsonb.spi.ViewBuilder;
import io.avaje.jsonb.spi.ViewBuilderAware;
import org.example.jmh.model.SomePropertyData;

public class MyViewAdapter extends JsonAdapter<SomePropertyData> implements ViewBuilderAware {

  // naming convention Match
  // prop1 [java.lang.String] name:prop1 setter:setProp1
  // prop2 [java.lang.String] name:prop2 setter:setProp2
  // prop3 [java.lang.String] name:prop3 setter:setProp3
  // prop4 [java.lang.String] name:prop4 setter:setProp4
  // prop5 [java.lang.String] name:prop5 setter:setProp5
  // prop6 [java.lang.String] name:prop6 setter:setProp6
  // prop7 [java.lang.String] name:prop7 setter:setProp7
  // prop8 [java.lang.String] name:prop8 setter:setProp8
  // prop9 [java.lang.String] name:prop9 setter:setProp9
  // prop10 [java.lang.String] name:prop10 setter:setProp10

  private final JsonAdapter<String> stringJsonAdapter;

  public MyViewAdapter(Jsonb jsonb) {
    this.stringJsonAdapter = jsonb.adapter(String.class);
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
  public void build(ViewBuilder builder, String name, MethodHandle handle) throws NoSuchMethodException, IllegalAccessException {
    builder.beginObject(name, handle);
    builder.add("prop1", stringJsonAdapter, builder.method(SomePropertyData.class, "getProp1", String.class));
    builder.add("prop2", stringJsonAdapter, builder.method(SomePropertyData.class, "getProp2", String.class));
    builder.add("prop3", stringJsonAdapter, builder.method(SomePropertyData.class, "getProp3", String.class));
    builder.add("prop4", stringJsonAdapter, builder.method(SomePropertyData.class, "getProp4", String.class));
    builder.add("prop5", stringJsonAdapter, builder.method(SomePropertyData.class, "getProp5", String.class));
    builder.add("prop6", stringJsonAdapter, builder.method(SomePropertyData.class, "getProp6", String.class));
    builder.add("prop7", stringJsonAdapter, builder.method(SomePropertyData.class, "getProp7", String.class));
    builder.add("prop8", stringJsonAdapter, builder.method(SomePropertyData.class, "getProp8", String.class));
    builder.add("prop9", stringJsonAdapter, builder.method(SomePropertyData.class, "getProp9", String.class));
    builder.add("prop10", stringJsonAdapter, builder.method(SomePropertyData.class, "getProp10", String.class));
    builder.endObject();
  }

  @Override
  public void toJson(JsonWriter writer, SomePropertyData somePropertyData) throws IOException {
    writer.beginObject();
    writer.name("prop1");
    stringJsonAdapter.toJson(writer, somePropertyData.getProp1());
    writer.name("prop2");
    stringJsonAdapter.toJson(writer, somePropertyData.getProp2());
    writer.name("prop3");
    stringJsonAdapter.toJson(writer, somePropertyData.getProp3());
    writer.name("prop4");
    stringJsonAdapter.toJson(writer, somePropertyData.getProp4());
    writer.name("prop5");
    stringJsonAdapter.toJson(writer, somePropertyData.getProp5());
    writer.name("prop6");
    stringJsonAdapter.toJson(writer, somePropertyData.getProp6());
    writer.name("prop7");
    stringJsonAdapter.toJson(writer, somePropertyData.getProp7());
    writer.name("prop8");
    stringJsonAdapter.toJson(writer, somePropertyData.getProp8());
    writer.name("prop9");
    stringJsonAdapter.toJson(writer, somePropertyData.getProp9());
    writer.name("prop10");
    stringJsonAdapter.toJson(writer, somePropertyData.getProp10());
    writer.endObject();
  }

  @Override
  public SomePropertyData fromJson(JsonReader reader) throws IOException {
    SomePropertyData _$somePropertyData = new SomePropertyData();

    // read json
    reader.beginObject();
    while (reader.hasNextField()) {
      String fieldName = reader.nextField();
      switch (fieldName) {
        case "prop1": {
          _$somePropertyData.setProp1(stringJsonAdapter.fromJson(reader)); break;
        }
        case "prop2": {
          _$somePropertyData.setProp2(stringJsonAdapter.fromJson(reader)); break;
        }
        case "prop3": {
          _$somePropertyData.setProp3(stringJsonAdapter.fromJson(reader)); break;
        }
        case "prop4": {
          _$somePropertyData.setProp4(stringJsonAdapter.fromJson(reader)); break;
        }
        case "prop5": {
          _$somePropertyData.setProp5(stringJsonAdapter.fromJson(reader)); break;
        }
        case "prop6": {
          _$somePropertyData.setProp6(stringJsonAdapter.fromJson(reader)); break;
        }
        case "prop7": {
          _$somePropertyData.setProp7(stringJsonAdapter.fromJson(reader)); break;
        }
        case "prop8": {
          _$somePropertyData.setProp8(stringJsonAdapter.fromJson(reader)); break;
        }
        case "prop9": {
          _$somePropertyData.setProp9(stringJsonAdapter.fromJson(reader)); break;
        }
        case "prop10": {
          _$somePropertyData.setProp10(stringJsonAdapter.fromJson(reader)); break;
        }
        default: {
          reader.unmappedField(fieldName);
          reader.skipValue();
        }
      }
    }
    reader.endObject();

    return _$somePropertyData;
  }
}
