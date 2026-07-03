# Extracting Values (JsonExtract)

`JsonExtract` is a small helper for pulling individual values out of loosely-typed
JSON (a `Map<String, Object>`) using a simple dot-notation path, without having to
define a model class for the full payload.

This is useful when you only need a couple of fields from a larger or
unpredictable JSON document — webhook payloads, third-party API responses,
config blobs — where writing a full `@Json` type isn't worth it.

## Getting a JsonExtract

### From avaje-jsonb

Parse into a `Map<String, Object>` using `Types.mapOf(Object.class)`, then wrap
it with `JsonExtract.of(...)`:

```java
import io.avaje.json.mapper.JsonExtract;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;

import java.util.Map;

String json = "{\"name\":\"Rob\",\"score\":4.5,\"whenActive\":\"2025-10-20\",\"address\":{\"street\":\"Pall Mall\"}}";

Map<String, Object> map = jsonb.type(Types.mapOf(Object.class)).fromJson(json);
JsonExtract extract = JsonExtract.of(map);
```

### From avaje-json-core (JsonMapper)

If you're using `avaje-json-core` on its own (no code generation, no `@Json`
types), [`JsonMapper`](json-mapper.md) can produce the map and the
`JsonExtract` for you:

```java
import io.avaje.json.mapper.JsonExtract;
import io.avaje.json.mapper.JsonMapper;

import java.util.Map;

static final JsonMapper jsonMapper = JsonMapper.builder().build();

Map<String, Object> mapFromJson = jsonMapper.fromJsonObject(json);
JsonExtract extract = jsonMapper.extract(mapFromJson);
```

## Extracting values

Paths are simple property names, or nested paths using dot notation:

```java
String name = extract.extract("name");
String street = extract.extract("address.street");
```

`extract(path)` throws `IllegalArgumentException` if the path is missing. Use
the default-value overloads instead when a field might be absent:

```java
String name = extract.extract("name", "Unknown");
int age = extract.extract("age", -1);
long views = extract.extract("views", 0L);
double score = extract.extract("score", -1D);
boolean active = extract.extract("active", false);
```

For numeric and boolean overloads, if the value at the path isn't a `Number` or
`Boolean` respectively, the given default value is returned rather than
throwing.

Use `extractOrEmpty(path)` to get an `Optional<String>` and map/parse it
yourself, e.g. for dates:

```java
LocalDate whenActive = extract.extractOrEmpty("whenActive")
  .map(LocalDate::parse)
  .orElseThrow();
```

## Nested paths

Dot notation walks into nested JSON objects any number of levels deep:

```java
String json = "{\"outer\":{\"a\":\"v0\",\"d\":{\"y\":42}}}";
Map<String, Object> map = jsonb.type(Types.mapOf(Object.class)).fromJson(json);

JsonExtract extract = JsonExtract.of(map);
int y = extract.extract("outer.d.y", 0);   // 42
String a = extract.extract("outer.a", "NA"); // "v0"
```

## Limitations

- Only scalar leaf values are extracted (String, numbers, boolean). There's no
  built-in support for extracting a nested object or a list as-is — read the
  full `Map`/`List` for that.
- There's no array/index syntax in the path (e.g. no `"items.0.name"`).
- `JsonExtract` operates on an already-parsed `Map<String, Object>` — it doesn't
  parse JSON text itself.

## Next Steps

- See [Basic Usage](basic-usage.md) for parsing JSON into `Map`/`List` with
  `Types.mapOf(...)` / `Types.listOf(...)`.
- See the [JsonMapper](json-mapper.md) guide for more on using
  avaje-json-core standalone.
