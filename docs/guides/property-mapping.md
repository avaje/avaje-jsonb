# Property Mapping

Map JSON properties to Java fields.

## Rename Properties

Use annotations to map properties:

```java
public class User {
  @JsonProperty("user_id")
  public long id;
  
  @JsonProperty("full_name")
  public String name;
  
  public String email;
}

// JSON: {"user_id":1,"full_name":"John","email":"john@example.com"}
```

## Ignore Properties

Skip properties during serialization:

```java
public class User {
  public long id;
  public String name;
  
  @JsonIgnore
  public String password;
}

// JSON: {"id":1,"name":"John"} (password not included)
```

## Next Steps

- Learn [streaming](streaming.md)
