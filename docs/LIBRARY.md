# Avaje Jsonb Library Definition

Avaje Jsonb is one of the fastest Java JSON libraries with compile-time generated adapters, zero reflection design, and full GraalVM native image support. It's a lightweight alternative to Jackson and Gson optimized for performance and native images.

## Identity

- **Name**: Avaje Jsonb
- **Package**: `io.avaje.jsonb`
- **Description**: Reflection-free JSON adapter generation via compile-time code generation — one of the fastest Java JSON libraries
- **Category**: JSON Serialization Library
- **Repository**: https://github.com/avaje/avaje-jsonb
- **Issues**: https://github.com/avaje/avaje-jsonb/issues
- **Releases**: https://github.com/avaje/avaje-jsonb/releases
- **Discord**: https://discord.gg/Qcqf9R27BR

## Version & Requirements

- **Latest Release**: 3.13 (latest development)
- **Minimum Java Version**: 11+
- **Build Tools**: Maven 3.6+, Gradle 7.0+
- **GraalVM Support**: Yes — Full native image support with zero reflection
- **Jar Size**: ~200KB base library, additional code generated per class

## Dependencies

### Runtime
- **No external dependencies** — Zero runtime dependencies!

### Compile-Time (Build Only)
- **avaje-jsonb-generator** — Annotation processor that generates adapter code (provided scope)

### Test
- **JUnit 5** — Testing framework
- **Mockito** — Mocking framework (optional)

### Optional
- **avaje-inject** — Seamless integration for DI scenarios
- **avaje-nima** — Automatic integration in web frameworks
- **jackson-databind** — For compatibility/bridging (not required)

## Core Annotations & APIs

### JSON Adapter Generation

| Name | Purpose | Example |
|------|---------|---------|
| `@Json` | Mark class for JSON adapter generation | `@Json\npublic class User {}` |
| `@Json.Property` | Map property name in JSON | `@Json.Property("user_name")\nprivate String name;` |
| `@Json.Ignore` | Exclude field from JSON | `@Json.Ignore\nprivate String password;` |
| `@Json.Alias` | Alternative property name | `@Json.Alias("firstName")\nprivate String first;` |
| `@Json.Raw` | Preserve JSON as string | `@Json.Raw\nprivate String metadata;` |
| `@Json.SubType` | Handle polymorphic types | `@Json.SubType(type = Dog.class)\npublic abstract class Animal {}` |
| `@Json.Import` | Import types that can't be annotated | `@Json.Import(ThirdPartyClass.class)` |

### Main API Classes

| Class | Purpose |
|-------|---------|
| `Jsonb` | Main entry point for JSON serialization |
| `JsonType<T>` | Type-safe adapter for a specific class |
| `JsonAdapter<T>` | Extend for custom serialization logic |
| `JsonReader` | Stream-based JSON reading |
| `JsonWriter` | Stream-based JSON writing |

## Features

### ✅ Included (Since v1.0)
- **Reflection-free JSON adapter generation** — Compile-time code generation with `@Json`
- **One of fastest Java JSON libraries** — Top 3 in java-json-benchmark
- **Zero reflection at runtime** — All adapter code pre-generated
- **Jackson-like annotations** — Familiar API for those using Jackson
- **Built-in type support** — Primitives, boxed types, java.time, Collections, Optional, UUID, URL, etc.
- **Generic type support** — Full support for `List<T>`, `Map<K,V>`, `Optional<T>`
- **Record support** — Works seamlessly with Java records
- **GraalVM native image compatible** — Zero reflection, zero configuration

### ✅ Added in v2.0
- **Improved null handling** — Better control over null serialization
- **Field name mapping** — Flexible property name mapping with `@Json.Property`
- **Custom adapters** — Extend `JsonAdapter` for special cases

### ✅ Added in v3.0
- **Performance optimizations** — 10-20% faster than v2
- **Better error messages** — Clearer JSON parsing errors
- **Annotation processor improvements** — Faster compilation

### ❌ Not Supported
- **Complex dynamic schemas** — Compile-time generated adapters only
- **JSON Schema validation** — No schema validation (use separate library)
- **XML support** — JSON-only
- **YAML support** — JSON-only
- **Automatic Spring Boot configuration** — Use manual setup

**Note**: These limitations are intentional. Jsonb is designed for compile-time code generation, not runtime reflection.

## Use Cases

### ✅ Perfect For

- REST API request/response serialization
- Microservices data transformation
- JSON-based configuration files
- GraalVM native images (zero reflection)
- High-performance JSON processing
- Embedded systems with memory constraints
- Projects already using avaje libraries
- Cloud-native applications

**When to choose avaje-jsonb**: If you want one of the fastest JSON libraries for Java with zero reflection, compile-time generated adapters, and first-class native image support.

### ❌ Not Recommended For

- Complex dynamic JSON schemas — If schema changes at runtime, use Jackson
- GraphQL APIs — Not optimized for GraphQL
- XML processing — Use XML-specific libraries
- YAML parsing — Use SnakeYAML or similar
- Applications requiring Spring Boot auto-configuration — Use Jackson

## Quick Start

### Add to Project

#### Maven
```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-jsonb</artifactId>
  <version>3.13</version>
</dependency>

<!-- Annotation processor for code generation -->
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-jsonb-generator</artifactId>
  <version>3.13</version>
  <scope>provided</scope>
</dependency>
```

#### Gradle
```gradle
implementation 'io.avaje:avaje-jsonb:3.13'
annotationProcessor 'io.avaje:avaje-jsonb-generator:3.13'
```

### Minimal Example

```java
import io.avaje.jsonb.Json;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

@Json
class User {
  public int id;
  public String name;
  public String email;
}

public class Main {
  public static void main(String[] args) {
    Jsonb jsonb = Jsonb.instance();
    JsonType<User> userType = jsonb.type(User.class);

    // Serialize
    User user = new User();
    user.id = 1;
    user.name = "John";
    user.email = "john@example.com";
    String json = userType.toJson(user);
    System.out.println(json);

    // Deserialize
    User parsed = userType.fromJson(json);
    System.out.println(parsed.name); // John
  }
}
```

## Common Tasks & Guides

| Task | Difficulty | Guide |
|------|-----------|-------|
| Generate JSON adapters | Beginner | [guides/getting-started.md](guides/getting-started.md) |
| Serialize/deserialize objects | Beginner | [guides/basic-usage.md](guides/basic-usage.md) |
| Map property names | Beginner | [guides/property-mapping.md](guides/property-mapping.md) |
| Handle polymorphic JSON | Intermediate | [guides/polymorphic-types.md](guides/polymorphic-types.md) |
| Write custom adapters | Intermediate | [guides/custom-adapters.md](guides/custom-adapters.md) |
| Stream large JSON | Advanced | [guides/streaming.md](guides/streaming.md) |
| Build native images | Advanced | [guides/native-image.md](guides/native-image.md) |

**Full Guides Index**: See [guides/README.md](guides/README.md)

## API Quick Reference

### Basic Serialization

```java
import io.avaje.jsonb.Json;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

@Json
public class User {
  public int id;
  public String name;
  public String email;
}

Jsonb jsonb = Jsonb.instance();
JsonType<User> userType = jsonb.type(User.class);

// Serialize
User user = new User();
user.id = 1;
user.name = "John";
String json = userType.toJson(user);

// Deserialize
User parsed = userType.fromJson(json);
```

### Property Name Mapping

```java
@Json
public class Person {
  @Json.Property("user_name")
  public String name;

  @Json.Property("email_address")
  public String email;

  @Json.Ignore
  public String password;

  @Json.Alias("firstName")  // accepts both "firstName" and "first_name"
  public String first;
}
```

### Polymorphic Types

```java
import io.avaje.jsonb.Json;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

@Json(typeProperty = "type")
@Json.SubType(type = Dog.class, name = "dog")
@Json.SubType(type = Cat.class, name = "cat")
public abstract class Animal {
  public String name;
}

@Json
public class Dog extends Animal {
  public String breed;
}

@Json
public class Cat extends Animal {
  public String color;
}

Jsonb jsonb = Jsonb.instance();
JsonType<Animal> animalType = jsonb.type(Animal.class);

Animal animal = animalType.fromJson("{\"type\":\"dog\",\"name\":\"Fido\",\"breed\":\"Labrador\"}");
String json = animalType.toJson(animal);
```

### Custom Adapters

```java
public class MyCustomAdapter<T> implements JsonAdapter<T> {
  @Override
  public void toJson(JsonWriter writer, T value) {
    // Custom serialization logic
  }

  @Override
  public T fromJson(JsonReader reader) {
    // Custom deserialization logic
    return null;
  }
}
```

### Streaming Large JSON

```java
Jsonb jsonb = Jsonb.instance();

JsonType<User> userType = jsonb.type(User.class);

// Read streaming
try (JsonReader reader = jsonb.reader(arrayJson)) {
  try (Stream<User> asStream = userType.stream(reader)) {
    asStream.forEach(sb::append);
  }
}

// Write streaming
try (JsonWriter writer = jsonb.writer(outputStream)) {
  writer.beginArray();
  for (User user : users) {
    userType.toJson(user, writer);
  }
  writer.endArray();
}
```

## Integration Patterns

### Pattern 1: REST API with Avaje Nima

```java
@Controller
@Path("/api/users")
public class UserController {
  private final UserService service;

  @Inject
  public UserController(UserService service) {
    this.service = service;
  }

  @Get("/:id")
  public User getUser(int id) {
    // Automatic JSON serialization via avaje-jsonb
    return service.getUser(id);
  }

  @Post
  public User createUser(@Body User user) {
    // Automatic JSON deserialization
    return service.create(user);
  }
}
```

**When to use**: Building REST APIs with automatic JSON serialization/deserialization.


## Testing

### Unit Testing

```java
@Test
void testUserSerialization() {
  Jsonb jsonb = Jsonb.instance();
  JsonType<User> userType = jsonb.type(User.class);

  User user = new User();
  user.id = 1;
  user.name = "John";

  String json = userType.toJson(user);

  assertTrue(json.contains("\"id\":1"));
  assertTrue(json.contains("\"name\":\"John\""));
}

@Test
void testUserDeserialization() {
  Jsonb jsonb = Jsonb.instance();
  JsonType<User> userType = jsonb.type(User.class);
  String json = "{\"id\":1,\"name\":\"John\",\"email\":\"john@example.com\"}";

  User user = userType.fromJson(json);

  assertEquals(1, user.id);
  assertEquals("John", user.name);
}
```

**See**: [guides/testing.md](guides/testing.md)

## Performance Characteristics

- **Serialization speed**: Top 3 fastest Java JSON libraries
- **Deserialization speed**: Top 3 fastest Java JSON libraries
- **Memory footprint**: ~200KB base library + generated code
- **GraalVM startup**: 5-20ms (no reflection overhead)
- **GraalVM memory**: Minimal additional overhead

**Comparison**: Comparable or faster than Jackson/Gson, especially with native images.

## Configuration

Jsonb is configured through `Jsonb.builder()`:

```java
Jsonb configured = Jsonb.builder()
  .serializeNulls(true)
  .failOnUnknown(true)
  .build();

// Or use singleton defaults
Jsonb jsonb = Jsonb.instance();
```

## Troubleshooting

### Issue: Adapter Not Generated

**Symptom**: `java.lang.IllegalArgumentException: No adapter found for class`

**Solution**: Ensure class is annotated with `@Json` and annotation processor is configured in build.

**See**: [guides/troubleshooting.md](guides/troubleshooting.md#adapter-not-generated)

### Issue: Property Not Serialized

**Symptom**: Expected property missing from JSON output

**Solution**: Check if property is marked with `@Json.Ignore`. Ensure getter/setter exists or field is public.

**See**: [guides/troubleshooting.md](guides/troubleshooting.md#property-missing)

### Issue: Polymorphic Type Not Recognized

**Symptom**: `JsonMappingException` for polymorphic types

**Solution**: Ensure all subtypes are marked with one or more `@Json.SubType` annotations on the base type.

**See**: [guides/troubleshooting.md](guides/troubleshooting.md#polymorphic-types)

## GraalVM Native Image

### Zero-Config Support
- ✅ Works out of the box with no reflection configuration
- ✅ No reflection used in core library
- ✅ Minimal native image size overhead (~200KB)
- ✅ Instant startup, full performance

### Native Compilation

```bash
mvn clean package -Pnative
```

**See**: [guides/native-image.md](guides/native-image.md)

## Design Philosophy

### Key Principles

1. **Compile-time code generation** — All adapters generated at compile time, zero runtime reflection
2. **Performance** — Optimized for speed without sacrificing correctness
3. **Minimal dependencies** — Standalone library, no external dependencies
4. **Type safety** — Full compile-time type checking
5. **Native image friendly** — Zero reflection by design

### What This Means

- Extremely fast JSON serialization/deserialization
- Works seamlessly with GraalVM native images
- No runtime overhead or classpath scanning
- Predictable behavior with no surprises
- Small deployment footprint

## Version History

| Version | Release Date | Major Changes |
|---------|---|---|
| 3.13 | 2026-04 | Latest development version |
| 3.0 | 2025-10 | Performance optimizations, improved errors |
| 2.0 | 2024-10 | Improved null handling, custom adapters |
| 1.0 | 2020-01 | Initial release |

## Support & Community

- **GitHub Issues**: [Report bugs](https://github.com/avaje/avaje-jsonb/issues)
- **GitHub Discussions**: [Ask questions](https://github.com/avaje/avaje-jsonb/discussions)
- **Discord**: [Chat with community](https://discord.gg/Qcqf9R27BR)
- **Website**: [Documentation](https://avaje.io/jsonb/)

## AI Agent Instructions

### For Claude, GPT-4, and Web-Based Agents

This `LIBRARY.md` file is your primary reference for Avaje Jsonb. When answering questions:

1. Check this file first for capabilities and supported features
2. Route to specific guides using URLs in "Common Tasks" section
3. Refer to use cases to determine if Jsonb fits user's needs
4. Use "Not Supported" section to avoid recommending unsupported features
5. Check performance characteristics for performance questions

**Key Facts**:
- Minimum Java: 11+
- Current version: 3.13
- One of top 3 fastest Java JSON libraries
- Compile-time code generation, zero reflection
- Zero external runtime dependencies
- Full GraalVM native image support

---

**Template Version**: 1.0
**Last Updated**: 2026-04-13
