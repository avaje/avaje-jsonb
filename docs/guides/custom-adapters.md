# Custom JSON Adapters

Create custom JSON serialization/deserialization logic.

## Adapter Implementation

```java
import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import java.time.LocalDateTime;

public final class LocalDateTimeAdapter implements JsonAdapter<LocalDateTime> {

  @Override
  public void toJson(JsonWriter writer, LocalDateTime value) {
    writer.value(value.toString());
  }

  @Override
  public LocalDateTime fromJson(JsonReader reader) {
    return LocalDateTime.parse(reader.readString());
  }
}
```

## Register the Adapter

```java
import io.avaje.jsonb.Jsonb;
import java.time.LocalDateTime;

Jsonb jsonb = Jsonb.builder()
  .add(LocalDateTime.class, new LocalDateTimeAdapter())
  .build();
```

## Next Steps

- See [property mapping](property-mapping.md)
