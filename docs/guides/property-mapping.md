# Property Mapping

Map JSON properties to Java fields.

## Rename Properties

Use `@Json.Property` to map properties:

```java
import io.avaje.jsonb.Json;

@Json
public class User {
  @Json.Property("user_id")
  public long id;
  
  @Json.Property("full_name")
  public String name;
  
  public String email;
}

// JSON: {"user_id":1,"full_name":"John","email":"john@example.com"}
```

## Ignore Properties

Skip properties during serialization and deserialization:

```java
import io.avaje.jsonb.Json;

@Json
public class User {
  public long id;
  public String name;
  
  @Json.Ignore
  public String password;
}

// JSON: {"id":1,"name":"John"} (password excluded)
```

## Next Steps

- Learn [streaming](streaming.md)
