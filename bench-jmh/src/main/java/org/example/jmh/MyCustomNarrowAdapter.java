package org.example.jmh;

import io.avaje.jsonb.JsonAdapter;
import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.spi.MetaNames;
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
  private final MetaNames names;

  public MyCustomNarrowAdapter(Jsonb jsonb) {
    this.stringJsonAdapter = jsonb.adapter(String.class);
    names = jsonb.properties("a", "b", "c", "d", "e");

    new Random();
  }

  @Override
  public void toJson(JsonWriter writer, NarrowNamesRecord myRecord) throws IOException {
    writer.beginObject();
    writer.names(names);
    //writer.name("firstNameProperty1");
    writer.key(0);
    stringJsonAdapter.toJson(writer, myRecord.a());
    writer.key(1);
    stringJsonAdapter.toJson(writer, myRecord.b());
    writer.key(2);
    stringJsonAdapter.toJson(writer, myRecord.c());
    writer.key(3);
    stringJsonAdapter.toJson(writer, myRecord.d());
    writer.key(4);
    stringJsonAdapter.toJson(writer, myRecord.e());
    writer.endObject();
  }

  @Override
  public NarrowNamesRecord fromJson(JsonReader reader) throws IOException {
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
