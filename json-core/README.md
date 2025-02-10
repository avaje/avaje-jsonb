# avaje-json-core

Provides the core API including JsonAdapter, JsonReader, JsonWriter, JsonStream API.

## Dependency

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-json-core</artifactId>
  <version>3.0</version>
</dependency>
```

## JsonMapper

If you only have simple use cases you can use avaje-json
without avaje-jsonb.

For use cases that only want to map to the following types:
- String
- Boolean
- Integer
- Long
- Double
- Map<String, Object>
- List<Object>

### Create a JsonMapper

static final JsonMapper mapper = JsonMapper.builder().build()


### Map example

var map = Map.of("key", "some value", "otherKey", 42).

String asJson = mapper.toJson(map);

// read json into a Map
Map<String,Object> asMap = mapper.fromJsonObject(asJson);


### List example

var map0 = Map.of("key", "a", "otherKey", 42);
var map1 = Map.of("key", "b", "otherKey", 99);

var list = List.of(map0, map1).

String asJson = mapper.toJson(list);

// read json into a List
List<Object> asList = mapper.fromJsonArray(asJson);
