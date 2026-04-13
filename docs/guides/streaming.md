# Streaming JSON

Efficiently stream large JSON documents.

## Streaming Deserialization

Process large JSON arrays element by element:

```java
import io.avaje.json.JsonReader;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import java.util.stream.Stream;

Jsonb jsonb = Jsonb.instance();
JsonType<String> stringType = jsonb.type(String.class);
String payload = "[\"one\",\"two\",\"three\"]";

try (JsonReader reader = jsonb.reader(payload);
     Stream<String> values = stringType.stream(reader)) {
  values.forEach(System.out::println);
}
```

This avoids loading the entire array into memory.

## Next Steps

- See [polymorphic types](polymorphic-types.md)
