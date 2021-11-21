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
  // firstNameProperty1 [java.lang.String] name:firstNameProperty1 setter:setProp1
  // lastNameProperty2 [java.lang.String] name:lastNameProperty2 setter:setProp2
  // anotherSimilarProperty3 [java.lang.String] name:anotherSimilarProperty3 setter:setProp3
  // moreOrLessProperty4 [java.lang.String] name:moreOrLessProperty4 setter:setProp4
  // lastButNotLeastProperty5 [java.lang.String] name:lastButNotLeastProperty5 setter:setProp5
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
    builder.add("firstNameProperty1", stringJsonAdapter, builder.method(SomePropertyData.class, "getProp1", String.class));
    builder.add("lastNameProperty2", stringJsonAdapter, builder.method(SomePropertyData.class, "getProp2", String.class));
    builder.add("anotherSimilarProperty3", stringJsonAdapter, builder.method(SomePropertyData.class, "getProp3", String.class));
    builder.add("moreOrLessProperty4", stringJsonAdapter, builder.method(SomePropertyData.class, "getProp4", String.class));
    builder.add("lastButNotLeastProperty5", stringJsonAdapter, builder.method(SomePropertyData.class, "getProp5", String.class));
    builder.add("prop6", stringJsonAdapter, builder.method(SomePropertyData.class, "getProp6", String.class));
    builder.add("prop7", stringJsonAdapter, builder.method(SomePropertyData.class, "getProp7", String.class));
    builder.add("prop8", stringJsonAdapter, builder.method(SomePropertyData.class, "getProp8", String.class));
    builder.add("prop9", stringJsonAdapter, builder.method(SomePropertyData.class, "getProp9", String.class));
    builder.add("prop10", stringJsonAdapter, builder.method(SomePropertyData.class, "getProp10", String.class));
    builder.endObject();
  }

  @Override
  public void toJson(JsonWriter writer, SomePropertyData somePropertyData) {
    writer.beginObject();
    writer.name("firstNameProperty1");
    stringJsonAdapter.toJson(writer, somePropertyData.getProp1());
    writer.name("lastNameProperty2");
    stringJsonAdapter.toJson(writer, somePropertyData.getProp2());
    writer.name("anotherSimilarProperty3");
    stringJsonAdapter.toJson(writer, somePropertyData.getProp3());
    writer.name("moreOrLessProperty4");
    stringJsonAdapter.toJson(writer, somePropertyData.getProp4());
    writer.name("lastButNotLeastProperty5");
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
  public SomePropertyData fromJson(JsonReader reader) {
    SomePropertyData _$somePropertyData = new SomePropertyData();

    // read json
    reader.beginObject();
    while (reader.hasNextField()) {
      String fieldName = reader.nextField();
      switch (fieldName) {
        case "firstNameProperty1": {
          _$somePropertyData.setProp1(stringJsonAdapter.fromJson(reader)); break;
        }
        case "lastNameProperty2": {
          _$somePropertyData.setProp2(stringJsonAdapter.fromJson(reader)); break;
        }
        case "anotherSimilarProperty3": {
          _$somePropertyData.setProp3(stringJsonAdapter.fromJson(reader)); break;
        }
        case "moreOrLessProperty4": {
          _$somePropertyData.setProp4(stringJsonAdapter.fromJson(reader)); break;
        }
        case "lastButNotLeastProperty5": {
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
