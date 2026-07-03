# JsonMapper (avaje-json-core standalone)

`JsonMapper` (`io.avaje.json.mapper.JsonMapper`) is a lightweight mapper for the
basic JSON/Java types — `String`, `Boolean`, `Integer`, `Long`, `Double`, and
`Map`/`List` of these — provided by **avaje-json-core**.

> If you're using avaje-jsonb (with `@Json` generated adapters) you generally
> **won't** need `JsonMapper` — use `Jsonb` instead. `JsonMapper` is intended for
> code that wants to depend only on `avaje-json-core` (no annotation
> processing) and either works with plain Maps/Lists, or provides its own
> `JsonAdapter` implementations.

## Dependency

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-json-core</artifactId>
  <version>3.0</version>
</dependency>
```

## Creating a JsonMapper

```java
import io.avaje.json.mapper.JsonMapper;

static final JsonMapper jsonMapper = JsonMapper.builder().build();
```

`Builder` also lets you set a specific `JsonStream` (buffer sizes, parser
limits etc — see [Configuration & Tuning](configuration.md)):

```java
import io.avaje.json.stream.JsonStream;

JsonStream jsonStream = JsonStream.builder().build();
JsonMapper jsonMapper = JsonMapper.builder().jsonStream(jsonStream).build();
```

## Mapping Maps and Lists

```java
Map<String, Long> map = new LinkedHashMap<>();
map.put("one", 45L);
map.put("two", 93L);

String asJson = jsonMapper.toJson(map);
// {"one":45,"two":93}

// read json into a Map<String, Object>
Map<String, Object> asMap = jsonMapper.fromJsonObject(asJson);

List<Object> asList = jsonMapper.fromJsonArray("[1,2,3]");
```

Use `map()` / `list()` for the same thing with more reading/writing options
(`InputStream`, `Reader`, `OutputStream`, `Writer`, byte arrays, pretty
printing):

```java
JsonMapper.Type<Map<String, Object>> mapType = jsonMapper.map();
JsonMapper.Type<List<Object>> listType = jsonMapper.list();

Map<String, Object> asMap = mapType.fromJson(inputStream);
String pretty = mapType.toJsonPretty(asMap);
```

For arbitrary/unknown JSON content (could be an object, array, or scalar), use
`object()` or the untyped `fromJson`/`toJson`:

```java
Object value = jsonMapper.fromJson(json);   // could be Map, List, String, Number, Boolean, or null
String asJson = jsonMapper.toJson(value);
```

## Custom JsonAdapters

`JsonMapper` can also act as a registry/factory for your own `JsonAdapter`
implementations, giving you a `Type<T>` with the full set of read/write options:

```java
class MyAdapter implements JsonAdapter<MyCustomType> {

  private final JsonMapper.Type<Map<String, Object>> map;

  MyAdapter(JsonMapper mapper) {
    this.map = mapper.map();
  }

  @Override
  public void toJson(JsonWriter writer, MyCustomType value) {
    map.toJson(Map.of("foo", value.foo, "bar", value.bar), writer);
  }

  @Override
  public MyCustomType fromJson(JsonReader reader) {
    Map<String, Object> values = map.fromJson(reader);
    MyCustomType result = new MyCustomType();
    result.foo = (String) values.get("foo");
    result.bar = ((Number) values.get("bar")).intValue();
    return result;
  }
}

JsonMapper jsonMapper = JsonMapper.builder().build();

// via a function reference (constructor takes JsonMapper)
JsonMapper.Type<MyCustomType> type = jsonMapper.type(MyAdapter::new);

// or with an already-constructed adapter instance
JsonMapper.Type<MyCustomType> type2 = jsonMapper.type(new MyAdapter(jsonMapper));

String asJson = type.toJson(myCustomType);
MyCustomType fromJson = type.fromJson(asJson);

// list/map variants are derived from the Type as well
JsonMapper.Type<List<MyCustomType>> listType = type.list();
JsonMapper.Type<Map<String, MyCustomType>> mapType = type.map();
```

## Property Names

`properties(String... names)` returns `PropertyNames` — pre-escaped/encoded
property names that can be reused by custom adapters to speed up writing JSON
object keys, avoiding repeated escaping of the same literal names.

## Extracting values by path

`JsonMapper.extract(map)` returns a [`JsonExtract`](json-extract.md) for
reading individual scalar values out of a `Map<String, Object>` by dot-notation
path — see the [Extracting Values (JsonExtract)](json-extract.md) guide.

```java
Map<String, Object> mapFromJson = jsonMapper.fromJsonObject(json);
JsonExtract extract = jsonMapper.extract(mapFromJson);

String name = extract.extract("name");
```

## Next Steps

- [Extracting Values (JsonExtract)](json-extract.md)
- [Configuration & Tuning](configuration.md) - buffer sizes, parser limits
- [Basic Usage](basic-usage.md) - the full avaje-jsonb API (`@Json` types, `Jsonb`)
