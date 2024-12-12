# avaje-json-node

Provides JsonNode types and associated JsonAdapters.

## Dependency

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-json-node</artifactId>
  <version>3.0</version>
</dependency>
```

## Examples

```java
var jsonObject = JsonObject.create()
  .add("person", JsonObject.create().add("name", "myName").add("active", true))
  .add("address", JsonObject.create().add("street", "42 some").add("city", "Bar"));
```

```java
var jsonArray = JsonArray.create()
    .add(42)
    .add("foo");
```


## Extract

One reason for using JsonNode is to ease the filtering and transformation
of json content.

We can use the `extract()` methods to help with the Stream API filtering
and mapping.

```java
JsonObject object = mapper.fromJsonObject(content);
JsonArray arrayWithNestedPerson = (JsonArray) object.get("people");

List<String> lastNames =
  arrayWithNestedPerson.stream()
    .filter(node -> "family".equals(node.extract("type")))
    .map(node -> node.extract("person.lastName"))
    .toList();
```
```java
List<JsonNode> peopleNodes =
  arrayWithNestedPerson.stream()
    .filter(node -> "family".equals(node.extract("type")))
    .map(node -> node.extractNode("person"))
    .toList();
```


### JsonNodeMapper

If you don't need avaje-jsonb then we can just use JsonNodeMapper.

```java
// create a JsonNodeMapper
static final JsonNodeMapper mapper = JsonNodeMapper.builder().build();
```

```java
JsonArray jsonArray = JsonArray.create()
  .add(42)
  .add("foo");

var asJson = mapper.toJson(jsonArray);

// read ARRAY from json
JsonArray arrayFromJson = mapper.fromJsonArray(asJson);
```

