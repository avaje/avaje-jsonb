# Testing JSON Serialization

How to test JSON with avaje-jsonb.

## Serialization Tests

Test JSON output:

```java
@Test
public void testUserSerialization() {
  User user = new User(1, "John", "john@example.com");
  String json = Jsonb.toJson(user);
  
  assertEquals("{\"id\":1,\"name\":\"John\",\"email\":\"john@example.com\"}", json);
}
```

## Deserialization Tests

Test JSON parsing:

```java
@Test
public void testUserDeserialization() {
  String json = "{\"id\":1,\"name\":\"John\",\"email\":\"john@example.com\"}";
  User user = Jsonb.fromJson(json, User.class);
  
  assertEquals(1, user.id);
  assertEquals("John", user.name);
  assertEquals("john@example.com", user.email);
}
```

## Round-Trip Tests

Ensure data is preserved:

```java
@Test
public void testRoundTrip() {
  User original = new User(1, "John", "john@example.com");
  String json = Jsonb.toJson(original);
  User restored = Jsonb.fromJson(json, User.class);
  
  assertEquals(original.id, restored.id);
  assertEquals(original.name, restored.name);
}
```
