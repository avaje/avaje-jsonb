# Basic Usage of Avaje Jsonb

How to serialize and deserialize JSON with avaje-jsonb.

## Getting the Jsonb Instance

You can either use the default instance or build a custom one:

```java
import io.avaje.jsonb.Jsonb;

// Use default instance
Jsonb jsonb = Jsonb.instance();

// Or build with configuration
Jsonb jsonb = Jsonb.builder()
  .serializeNulls(true)
  .serializeEmpty(true)
  .build();
```

## Serialization

Convert Java objects to JSON:

```java
User user = new User(1, "John", "john@example.com");

// Using default instance
String json = Jsonb.instance().toJson(user);
// {"id":1,"name":"John","email":"john@example.com"}

// Or with JsonType for more control
String json = jsonb.type(User.class).toJson(user);
```

## Deserialization

Convert JSON to Java objects:

```java
String json = "{\"id\":1,\"name\":\"John\",\"email\":\"john@example.com\"}";

User user = jsonb.type(User.class).fromJson(json);
```

## Collections

Handle lists and maps using `Types` helper:

```java
import io.avaje.jsonb.Types;

List<User> users = new ArrayList<>();
users.add(new User(1, "John", "john@example.com"));
users.add(new User(2, "Jane", "jane@example.com"));

// Serialize list
String json = jsonb.type(Types.listOf(User.class)).toJson(users);

// Deserialize list
List<User> restored = jsonb.type(Types.listOf(User.class)).fromJson(json);
```

## Next Steps

- Learn [custom adapters](custom-adapters.md)
- Understand [property mapping](property-mapping.md)
