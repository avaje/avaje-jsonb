# Basic Usage of Avaje Jsonb

How to serialize and deserialize JSON with avaje-jsonb.

## Serialization

Convert Java objects to JSON:

```java
import io.avaje.jsonb.Jsonb;

User user = new User(1, "John", "john@example.com");
String json = Jsonb.toJson(user);
// {"id":1,"name":"John","email":"john@example.com"}
```

## Deserialization

Convert JSON to Java objects:

```java
String json = "{\"id\":1,\"name\":\"John\",\"email\":\"john@example.com\"}";
User user = Jsonb.fromJson(json, User.class);
```

## Collections

Handle lists and maps:

```java
List<User> users = new ArrayList<>();
users.add(new User(1, "John", "john@example.com"));
users.add(new User(2, "Jane", "jane@example.com"));

String json = Jsonb.toJson(users);
List<User> restored = Jsonb.fromJson(json, new TypeReference<List<User>>() {});
```

## Next Steps

- Learn [custom adapters](custom-adapters.md)
- Understand [property mapping](property-mapping.md)
