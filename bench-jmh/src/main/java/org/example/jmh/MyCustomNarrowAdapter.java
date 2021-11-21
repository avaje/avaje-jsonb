package org.example.jmh;

import io.avaje.jsonb.JsonAdapter;
import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.spi.PropertyNames;
import org.example.jmh.model.NarrowNamesRecord;
import org.example.jmh.model.WideNamesRecord;

import java.io.IOException;
import java.util.Random;

public class MyCustomNarrowAdapter extends JsonAdapter<NarrowNamesRecord> {

  // naming convention Match
  // firstNameProperty1 [java.lang.String] name:firstNameProperty1 constructor
  // lastNameProperty2 [java.lang.String] name:lastNameProperty2 constructor
  // anotherSimilarProperty3 [java.lang.String] name:anotherSimilarProperty3 constructor
  // moreOrLessProperty4 [java.lang.String] name:moreOrLessProperty4 constructor
  // lastButNotLeastProperty5 [java.lang.String] name:lastButNotLeastProperty5 constructor

  private final JsonAdapter<String> stringJsonAdapter;
  private final PropertyNames names;

  public MyCustomNarrowAdapter(Jsonb jsonb) {
    this.stringJsonAdapter = jsonb.adapter(String.class);
    this.names = jsonb.properties("a", "b", "c", "d", "e");
  }

  @Override
  public void toJson(JsonWriter writer, NarrowNamesRecord myRecord) {
    writer.beginObject();
    //writer.names(names);
    //writer.name(0);
    writer.name("a");
    stringJsonAdapter.toJson(writer, myRecord.a());
    //writer.name(1);
    writer.name("b");
    stringJsonAdapter.toJson(writer, myRecord.b());
    //writer.name(2);
    writer.name("c");
    stringJsonAdapter.toJson(writer, myRecord.c());
    //writer.name(3);
    writer.name("d");
    stringJsonAdapter.toJson(writer, myRecord.d());
    //writer.name(4);
    writer.name("e");
    stringJsonAdapter.toJson(writer, myRecord.e());
    writer.endObject();
  }

  @Override
  public NarrowNamesRecord fromJson(JsonReader reader) {
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
        case "a": {
          _val$prop1 = stringJsonAdapter.fromJson(reader); break;
        }
        case "b": {
          _val$prop2 = stringJsonAdapter.fromJson(reader); break;
        }
        case "c": {
          _val$prop3 = stringJsonAdapter.fromJson(reader); break;
        }
        case "d": {
          _val$prop4 = stringJsonAdapter.fromJson(reader); break;
        }
        case "e": {
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
    NarrowNamesRecord _$myRecord = new NarrowNamesRecord(_val$prop1, _val$prop2, _val$prop3, _val$prop4, _val$prop5);
    return _$myRecord;
  }
}
