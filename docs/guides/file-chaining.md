# File Chaining

Chain JSON operations efficiently.

## Chaining Operations

Build complex transformations:

```java
List<User> users = Jsonb.fromJson(inputJson, new TypeReference<List<User>>() {})
  .stream()
  .filter(u -> u.active)
  .map(u -> { u.email = u.email.toLowerCase(); return u; })
  .toList();

String output = Jsonb.toJson(users);
```

## Next Steps

- See [testing](testing.md)
