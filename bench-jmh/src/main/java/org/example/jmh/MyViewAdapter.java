package org.example.jmh;

import io.avaje.jsonb.*;
import java.io.IOException;
import java.util.function.UnaryOperator;
import java.time.Instant;
import java.util.List;

import io.avaje.jsonb.spi.ViewBuilder;
import io.avaje.jsonb.spi.ViewBuilderAware;
import org.example.jmh.model.SomePropertyData;

public class MyViewAdapter implements JsonAdapter<SomePropertyData>, ViewBuilderAware {

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
  public void build(ViewBuilder builder, String name, UnaryOperator<Object> accessor) {
    builder.beginObject(name, accessor);
    builder.add("firstNameProperty1", stringJsonAdapter, o -> ((SomePropertyData) o).getProp1());
    builder.add("lastNameProperty2", stringJsonAdapter, o -> ((SomePropertyData) o).getProp2());
    builder.add("anotherSimilarProperty3", stringJsonAdapter, o -> ((SomePropertyData) o).getProp3());
    builder.add("moreOrLessProperty4", stringJsonAdapter, o -> ((SomePropertyData) o).getProp4());
    builder.add("lastButNotLeastProperty5", stringJsonAdapter, o -> ((SomePropertyData) o).getProp5());
    builder.add("prop6", stringJsonAdapter, o -> ((SomePropertyData) o).getProp6());
    builder.add("prop7", stringJsonAdapter, o -> ((SomePropertyData) o).getProp7());
    builder.add("prop8", stringJsonAdapter, o -> ((SomePropertyData) o).getProp8());
    builder.add("prop9", stringJsonAdapter, o -> ((SomePropertyData) o).getProp9());
    builder.add("prop10", stringJsonAdapter, o -> ((SomePropertyData) o).getProp10());
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
