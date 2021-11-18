package org.example.jmh;

import io.avaje.jsonb.*;
import java.io.IOException;
import java.util.Random;

import io.avaje.jsonb.spi.MetaNames;
import org.example.jmh.model.WideNamesRecord;

public class MyCustomWideAdapter extends JsonAdapter<WideNamesRecord> {

  // naming convention Match
  // firstNameProperty1 [java.lang.String] name:firstNameProperty1 constructor
  // lastNameProperty2 [java.lang.String] name:lastNameProperty2 constructor
  // anotherSimilarProperty3 [java.lang.String] name:anotherSimilarProperty3 constructor
  // moreOrLessProperty4 [java.lang.String] name:moreOrLessProperty4 constructor
  // lastButNotLeastProperty5 [java.lang.String] name:lastButNotLeastProperty5 constructor

  private final JsonAdapter<String> stringJsonAdapter;
  private final MetaNames names;

  public MyCustomWideAdapter(Jsonb jsonb) {
    this.stringJsonAdapter = jsonb.adapter(String.class);
    names = jsonb.properties("firstNameProperty1", "lastNameProperty2", "anotherSimilarProperty3", "moreOrLessProperty4", "lastButNotLeastProperty5");

    new Random();
  }

  @Override
  public void toJson(JsonWriter writer, WideNamesRecord myRecord) throws IOException {
    writer.beginObject();
    //writer.names(names);
    //writer.name(0);
    writer.name("firstNameProperty1");
    stringJsonAdapter.toJson(writer, myRecord.firstNameProperty1());
    writer.name("lastNameProperty2");
    stringJsonAdapter.toJson(writer, myRecord.lastNameProperty2());
    writer.name("anotherSimilarProperty3");
    stringJsonAdapter.toJson(writer, myRecord.anotherSimilarProperty3());
    writer.name("moreOrLessProperty4");
    stringJsonAdapter.toJson(writer, myRecord.moreOrLessProperty4());
    writer.name("lastButNotLeastProperty5");
    stringJsonAdapter.toJson(writer, myRecord.lastButNotLeastProperty5());
    writer.endObject();
  }

  @Override
  public WideNamesRecord fromJson(JsonReader reader) throws IOException {
    // variables to read json values into, constructor params don't need _set$ flags
    String     _val$prop1 = null;
    String     _val$prop2 = null;
    String     _val$prop3 = null;
    String     _val$prop4 = null;
    String     _val$prop5 = null;

    // read json
    reader.beginObject();
    while (reader.hasNextField()) {
      String fieldName = reader.nextField();
      switch (fieldName) {
        case "firstNameProperty1": {
          _val$prop1 = stringJsonAdapter.fromJson(reader); break;
        }
        case "lastNameProperty2": {
          _val$prop2 = stringJsonAdapter.fromJson(reader); break;
        }
        case "anotherSimilarProperty3": {
          _val$prop3 = stringJsonAdapter.fromJson(reader); break;
        }
        case "moreOrLessProperty4": {
          _val$prop4 = stringJsonAdapter.fromJson(reader); break;
        }
        case "lastButNotLeastProperty5": {
          _val$prop5 = stringJsonAdapter.fromJson(reader); break;
        }
        default: {
          reader.unmappedField(fieldName);
          reader.skipValue();
        }
      }
    }
    reader.endObject();

    // build and return MyRecord
    WideNamesRecord _$myRecord = new WideNamesRecord(_val$prop1, _val$prop2, _val$prop3, _val$prop4, _val$prop5);
    return _$myRecord;
  }
}
