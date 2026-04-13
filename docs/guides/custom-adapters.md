# Custom JSON Adapters

Create custom JSON serialization/deserialization logic.

## Custom Adapter

```java
import io.avaje.jsonb.JsonAdapter;

public class LocalDateTimeAdapter implements JsonAdapter<LocalDateTime> {
  
  @Override
  public LocalDateTime fromJson(Object value) {
    return LocalDateTime.parse(value.toString());
  }
  
  @Override
  public Object toJson(LocalDateTime value) {
    return value.toString();
  }
}
```

Register the adapter:

```java
Jsonb jsonb = Jsonb.builder()
  .add(new LocalDateTimeAdapter())
  .build();
```

## Next Steps

- See [property mapping](property-mapping.md)
