# Testing JSON Serialization

How to test JSON with avaje-jsonb.

## Serialization Tests

Test JSON output:

```java
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

@Test
public void testUserSerialization() {
  Jsonb jsonb = Jsonb.instance();
  JsonType<User> userType = jsonb.type(User.class);

  User user = new User(1, "John", "john@example.com");
  String json = userType.toJson(user);
  
  assertEquals("{\"id\":1,\"name\":\"John\",\"email\":\"john@example.com\"}", json);
}
```

## Deserialization Tests

Test JSON parsing:

```java
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

@Test
public void testUserDeserialization() {
  Jsonb jsonb = Jsonb.instance();
  JsonType<User> userType = jsonb.type(User.class);

  String json = "{\"id\":1,\"name\":\"John\",\"email\":\"john@example.com\"}";
  User user = userType.fromJson(json);
  
  assertEquals(1, user.id);
  assertEquals("John", user.name);
  assertEquals("john@example.com", user.email);
}
```

## Round-Trip Tests

Ensure data is preserved:

```java
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

@Test
public void testRoundTrip() {
  Jsonb jsonb = Jsonb.instance();
  JsonType<User> userType = jsonb.type(User.class);

  User original = new User(1, "John", "john@example.com");
  String json = userType.toJson(original);
  User restored = userType.fromJson(json);
  
  assertEquals(original.id, restored.id);
  assertEquals(original.name, restored.name);
}
```
