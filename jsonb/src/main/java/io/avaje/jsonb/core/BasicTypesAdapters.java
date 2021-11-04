package io.avaje.jsonb.core;

import io.avaje.jsonb.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import static java.util.Objects.requireNonNull;

final class BasicTypesAdapters {

  public static final JsonAdapter.Factory FACTORY = new JsonAdapter.Factory() {

    @SuppressWarnings({"unchecked","rawtypes"})
    @Override
    public JsonAdapter<?> create(Type type, Set<? extends Annotation> annotations, Jsonb jsonb) {
      if (!annotations.isEmpty()) return null;
      if (type == Boolean.TYPE) return BOOLEAN_JSON_ADAPTER;
      if (type == Byte.TYPE) return BYTE_JSON_ADAPTER;
      if (type == Character.TYPE) return CHARACTER_JSON_ADAPTER;
      if (type == Double.TYPE) return DOUBLE_JSON_ADAPTER;
      if (type == Float.TYPE) return FLOAT_JSON_ADAPTER;
      if (type == Integer.TYPE) return INTEGER_JSON_ADAPTER;
      if (type == Long.TYPE) return LONG_JSON_ADAPTER;
      if (type == Short.TYPE) return SHORT_JSON_ADAPTER;
      if (type == Boolean.class) return BOOLEAN_JSON_ADAPTER.nullSafe();
      if (type == Byte.class) return BYTE_JSON_ADAPTER.nullSafe();
      if (type == Character.class) return CHARACTER_JSON_ADAPTER.nullSafe();
      if (type == Double.class) return DOUBLE_JSON_ADAPTER.nullSafe();
      if (type == Float.class) return FLOAT_JSON_ADAPTER.nullSafe();
      if (type == Integer.class) return INTEGER_JSON_ADAPTER.nullSafe();
      if (type == Long.class) return LONG_JSON_ADAPTER.nullSafe();
      if (type == Short.class) return SHORT_JSON_ADAPTER.nullSafe();
      if (type == String.class) return STRING_JSON_ADAPTER.nullSafe();

//            } else if (type == Object.class) {
//                return (new StandardJsonAdapters.ObjectJsonAdapter(moshi)).nullSafe();

      Class<?> rawType = Util.rawType(type);
      if (rawType.isEnum()) {
        return new EnumJsonAdapter(rawType).nullSafe();
      }
      return null;
    }
  };

  static final JsonAdapter<Boolean> BOOLEAN_JSON_ADAPTER = new JsonAdapter<Boolean>() {
    @Override
    public Boolean fromJson(JsonReader reader) throws IOException {
      return reader.nextBoolean();
    }

    @Override
    public void toJson(JsonWriter writer, Boolean value) throws IOException {
      writer.value(value);
    }

    @Override
    public String toString() {
      return "JsonAdapter(Boolean)";
    }
  };
  static final JsonAdapter<Byte> BYTE_JSON_ADAPTER = new JsonAdapter<Byte>() {
    @Override
    public Byte fromJson(JsonReader reader) throws IOException {
      return (byte) rangeCheckNextInt(reader, "a byte", -128, 255);
    }

    @Override
    public void toJson(JsonWriter writer, Byte value) throws IOException {
      writer.value((long) (value.intValue() & 255));
    }

    @Override
    public String toString() {
      return "JsonAdapter(Byte)";
    }
  };
  static final JsonAdapter<Character> CHARACTER_JSON_ADAPTER = new JsonAdapter<Character>() {
    @Override
    public Character fromJson(JsonReader reader) throws IOException {
      String value = reader.nextString();
      if (value.length() > 1) {
        throw new JsonDataException(String.format("Expected %s but was %s at path %s", "a char", '"' + value + '"', reader.path()));
      } else {
        return value.charAt(0);
      }
    }

    @Override
    public void toJson(JsonWriter writer, Character value) throws IOException {
      writer.value(value.toString());
    }

    @Override
    public String toString() {
      return "JsonAdapter(Character)";
    }
  };
  static final JsonAdapter<Double> DOUBLE_JSON_ADAPTER = new JsonAdapter<Double>() {
    @Override
    public Double fromJson(JsonReader reader) throws IOException {
      return reader.nextDouble();
    }

    @Override
    public void toJson(JsonWriter writer, Double value) throws IOException {
      writer.value(value);
    }

    @Override
    public String toString() {
      return "JsonAdapter(Double)";
    }
  };
  static final JsonAdapter<Float> FLOAT_JSON_ADAPTER = new JsonAdapter<Float>() {
    @Override
    public Float fromJson(JsonReader reader) throws IOException {
      float value = (float) reader.nextDouble();
      if (Float.isInfinite(value)) { // !reader.isLenient() &&
        throw new JsonDataException("JSON forbids NaN and infinities: " + value + " at path " + reader.path());
      } else {
        return value;
      }
    }

    @Override
    public void toJson(JsonWriter writer, Float value) throws IOException {
      requireNonNull(value);
      writer.value(value);
    }

    @Override
    public String toString() {
      return "JsonAdapter(Float)";
    }
  };
  static final JsonAdapter<Integer> INTEGER_JSON_ADAPTER = new JsonAdapter<Integer>() {
    @Override
    public Integer fromJson(JsonReader reader) throws IOException {
      return reader.nextInt();
    }

    @Override
    public void toJson(JsonWriter writer, Integer value) throws IOException {
      writer.value(value);
    }

    @Override
    public String toString() {
      return "JsonAdapter(Integer)";
    }
  };
  static final JsonAdapter<Long> LONG_JSON_ADAPTER = new JsonAdapter<Long>() {
    @Override
    public Long fromJson(JsonReader reader) throws IOException {
      return reader.nextLong();
    }

    @Override
    public void toJson(JsonWriter writer, Long value) throws IOException {
      writer.value(value);
    }

    @Override
    public String toString() {
      return "JsonAdapter(Long)";
    }
  };
  static final JsonAdapter<Short> SHORT_JSON_ADAPTER = new JsonAdapter<Short>() {
    @Override
    public Short fromJson(JsonReader reader) throws IOException {
      return (short) rangeCheckNextInt(reader, "a short", -32768, 32767);
    }

    @Override
    public void toJson(JsonWriter writer, Short value) throws IOException {
      writer.value((long) value.intValue());
    }

    @Override
    public String toString() {
      return "JsonAdapter(Short)";
    }
  };
  static final JsonAdapter<String> STRING_JSON_ADAPTER = new JsonAdapter<String>() {
    @Override
    public String fromJson(JsonReader reader) throws IOException {
      return reader.nextString();
    }

    @Override
    public void toJson(JsonWriter writer, String value) throws IOException {
      writer.value(value);
    }

    @Override
    public String toString() {
      return "JsonAdapter(String)";
    }
  };

  private BasicTypesAdapters() {
  }

  static int rangeCheckNextInt(JsonReader reader, String typeMessage, int min, int max) throws IOException {
    int value = reader.nextInt();
    if (value >= min && value <= max) {
      return value;
    } else {
      throw new JsonDataException(String.format("Expected %s but was %s at path %s", typeMessage, value, reader.path()));
    }
  }

//    static final class ObjectJsonAdapter extends JAdapter<Object> {
//        private final Mason moshi;
//        private final JAdapter<List> listJsonAdapter;
//        private final JAdapter<Map> mapAdapter;
//        private final JAdapter<String> stringAdapter;
//        private final JAdapter<Double> doubleAdapter;
//        private final JAdapter<Boolean> booleanAdapter;
//
//        ObjectJsonAdapter(Mason moshi) {
//            this.moshi = moshi;
//            this.listJsonAdapter = moshi.adapter(List.class);
//            this.mapAdapter = moshi.adapter(Map.class);
//            this.stringAdapter = moshi.adapter(String.class);
//            this.doubleAdapter = moshi.adapter(Double.class);
//            this.booleanAdapter = moshi.adapter(Boolean.class);
//        }
//
//        public Object fromJson(JsonReader reader) throws IOException {
////            switch(reader.peek()) {
////                case BEGIN_ARRAY:
////                    return this.listJsonAdapter.fromJson(reader);
////                case BEGIN_OBJECT:
////                    return this.mapAdapter.fromJson(reader);
////                case STRING:
////                    return this.stringAdapter.fromJson(reader);
////                case NUMBER:
////                    return this.doubleAdapter.fromJson(reader);
////                case BOOLEAN:
////                    return this.booleanAdapter.fromJson(reader);
////                case NULL:
////                    return reader.nextNull();
////                default:
////                    throw new IllegalStateException("Expected a value but was " + reader.peek() + " at path " + reader.getPath());
////            }
//        }
//
//        public void toJson(JsonWriter writer, Object value) throws IOException {
//            Class<?> valueClass = value.getClass();
//            if (valueClass == Object.class) {
//                writer.beginObject();
//                writer.endObject();
//            } else {
//                this.moshi.adapter(this.toJsonType(valueClass), Util.NO_ANNOTATIONS).toJson(writer, value);
//            }
//
//        }
//
//        private Class<?> toJsonType(Class<?> valueClass) {
//            if (Map.class.isAssignableFrom(valueClass)) {
//                return Map.class;
//            } else {
//                return Collection.class.isAssignableFrom(valueClass) ? Collection.class : valueClass;
//            }
//        }
//
//        public String toString() {
//            return "JsonAdapter(Object)";
//        }
//    }

  static final class EnumJsonAdapter<T extends Enum<T>> extends JsonAdapter<T> {
    private final Class<T> enumType;

    EnumJsonAdapter(Class<T> enumType) {
      this.enumType = enumType;
    }

    @Override
    public T fromJson(JsonReader reader) throws IOException {
      String value = reader.nextString();
      return Enum.valueOf(enumType, value);
    }

    @Override
    public void toJson(JsonWriter writer, T value) throws IOException {
      writer.value(value.name());
    }

    @Override
    public String toString() {
      return "JsonAdapter(" + this.enumType.getName() + ")";
    }
  }
}
