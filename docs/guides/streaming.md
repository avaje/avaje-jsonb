# Streaming JSON

Efficiently stream large JSON documents.

## Streaming Deserialization

Process large JSON arrays element by element:

```java
JsonbStream stream = Jsonb.stream();
stream.parse(inputStream, User.class, user -> {
  System.out.println("Processing user: " + user.name);
  saveToDatabase(user);
});
```

This avoids loading the entire array into memory.

## Next Steps

- See [polymorphic types](polymorphic-types.md)
