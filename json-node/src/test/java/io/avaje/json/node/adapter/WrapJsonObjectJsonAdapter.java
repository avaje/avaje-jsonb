package io.avaje.json.node.adapter;

import java.lang.invoke.MethodHandle;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.json.PropertyNames;
import io.avaje.json.node.JsonObject;
import io.avaje.json.node.adapter.NullJsonObjectTest.WrapJsonObject;
import io.avaje.json.view.ViewBuilder;
import io.avaje.json.view.ViewBuilderAware;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.spi.Generated;

@Generated("io.avaje.jsonb.generator")
public final class WrapJsonObjectJsonAdapter implements JsonAdapter<WrapJsonObject>, ViewBuilderAware {

  // naming convention Match
  // json [io.avaje.json.node.JsonObject] name:json publicField

  private final JsonAdapter<JsonObject> jsonObjectJsonAdapter;
  private final PropertyNames names;

  public WrapJsonObjectJsonAdapter(Jsonb jsonb) {
    this.jsonObjectJsonAdapter = jsonb.adapter(JsonObject.class);
    this.names = jsonb.properties("json");
  }

  @Override
  public void toJson(JsonWriter writer, WrapJsonObject _wrapJsonObject) {
    writer.beginObject(names);
    writer.name(0);
    jsonObjectJsonAdapter.toJson(writer, _wrapJsonObject.json);
    writer.endObject();
  }

  @Override
  public WrapJsonObject fromJson(JsonReader reader) {
    // variables to read json values into, constructor params don't need _set$ flags
    JsonObject _val$json = null; boolean _set$json = false;

    // read json
    reader.beginObject(names);
    while (reader.hasNextField()) {
      final String fieldName = reader.nextField();
      switch (fieldName) {
        case "json":
          _val$json = jsonObjectJsonAdapter.fromJson(reader);
          _set$json = true;
          break;

        default:
          reader.unmappedField(fieldName);
          reader.skipValue();
      }
    }
    reader.endObject();

    // build and return WrapJsonObject
    WrapJsonObject _$wrapJsonObject = new WrapJsonObject();
    if (_set$json) _$wrapJsonObject.json = _val$json;
    return _$wrapJsonObject;
  }

  @SuppressWarnings("unchecked")
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
    builder.add("json", jsonObjectJsonAdapter, builder.field(WrapJsonObject.class, "json"));
    builder.endObject();
  }
}
