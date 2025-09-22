[![Build](https://github.com/avaje/avaje-jsonb/actions/workflows/build.yml/badge.svg)](https://github.com/avaje/avaje-jsonb/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.avaje/avaje-jsonb.svg?label=Maven%20Central)](https://mvnrepository.com/artifact/io.avaje/avaje-jsonb)
[![javadoc](https://javadoc.io/badge2/io.avaje/avaje-jsonb/javadoc.svg?&color=purple)](https://javadoc.io/doc/io.avaje/avaje-jsonb)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/avaje/avaje-jsonb/blob/master/LICENSE)
[![Discord](https://img.shields.io/discord/1074074312421683250?color=%237289da&label=discord)](https://discord.gg/Qcqf9R27BR)

# [avaje-jsonb](https://avaje.io/jsonb/)

Fast, reflection-free Json binding via apt source code generation. A light (~200kb + generated code) source code generation style alternative to Jackson's ObjectMapper or Gson. (code generation vs reflection)

- Annotate java classes with `@Json` (or use `@Json.Import` for types we "don't own" or can't annotate)
- `avaje-jsonb-generator` annotation processor generates Java source code to convert to/from json
- No need to manually register generated adapters. (Uses ServiceLoader to auto-register)
- Constructors and accessors/getters/setters of any style "just work" (records, constructors, 'fluid setters')
- Jackson-like annotations: `@Json.Raw`, `@Json.Property`, `@Json.Ignore`, `@Json.Alias`, etc.
- Support Imports and Mixins (adding jsonb features to types we can't directly annotate).
- Supports Generic Types.
- Provides support for dynamic json views (similar in style to that presented by [LinkedIn at java one in 2009](https://www.slideshare.net/linkedin/building-consistent-restful-apis-in-a-highperformance-environment)
- One of the top three [fastest Java JSON libraries](https://github.com/fabienrenaud/java-json-benchmark#users-model)

### Built-in Type Adapters

Built-in support for reading and writing Java’s core data types:

 * Primitives (int, float, char...) and their boxed counterparts (Integer, Float, Character...).
 * BigInteger and BigDecimal
 * java.time classes (Instant, LocalDate, LocalDateTime...)
 * Arrays, Collections, Streams, Lists, Sets, and Maps
 * Optionals (will unwrap and serialize the contained value)
 * Strings
 * Enums
 * Other miscellaneous types (UUID, URL, URI)

# Quick Start

## Step 1 - Add dependencies
```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-jsonb</artifactId>
  <version>${avaje-jsonb-version}</version>
</dependency>
```

And add avaje-jsonb-generator as an annotation processor.
```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-jsonb-generator</artifactId>
  <version>${avaje-jsonb-version}</version>
  <scope>provided</scope>
</dependency>
```

## Step 2 - Add `@Json`

Add `@Json` to the types we want to serialize.

The `avaje-jsonb-generator` annotation processor will generate a JsonAdapter as java source code
for each type annotated with `@Json`. These will be automatically registered with Jsonb
when it is started using a service loader mechanism.

```java
@Json
public class Address {
  private String street;
  private String suburb;
  private String city;
  // object fields will automatically have adapters generated, no @Json required
  // (though you can add @Json anyway to modify the generated adapter how you wish)
  private OtherClass other;

  //add getters/setters
}
```

This also works with records:
```java
@Json
public record Address(String street, String suburb, String city) { }
```

For types we cannot annotate with `@Json` we can place `@Json.Import(TypeToimport.class)` on any class/package-info to generate the adapters.

## Step 3 - Use

```java
// build using defaults
Jsonb jsonb = Jsonb.instance();

JsonType<Customer> customerType = jsonb.type(Customer.class);

Customer customer = ...;

// serialize to json
String asJson = customerType.toJson(customer);

// deserialize from json
Customer customer = customerType.fromJson(asJson);
```

## Step 4 - Use Json views

`avaje-jsonb` supports dynamic json views. This allows us to specify which specific properties
to include when serializing to json.

For example:

```java
Jsonb jsonb = Jsonb.instance();

JsonType<Customer> customerType = jsonb.type(Customer.class);

// only including the id and name
JsonView<Customer> idAndNameView = customerType.view("(id, name)");
String asJson = idAndNameView.toJson(customer);


JsonView<Customer> myView =
  customerType.view("(id, name, billingAddress(*), contacts(lastName, email))");

// serialise to json the above specified properties only
String asJson = myView.toJson(customer);
```

### Generated Code
Given the class:
```java
@Json
public class Address {
  private String street;
  private City city;
  private Suburb suburb;
  //getters/setters ommited for brevity
}
```
The following code will be generated and used for serialization/deserialization.

```java
@Generated
public final class AddressJsonAdapter implements JsonAdapter<Address>, ViewBuilderAware {

  private final JsonAdapter<String> stringJsonAdapter;
  private final JsonAdapter<City> cityJsonAdapter;
  private final JsonAdapter<Suburb> suburbJsonAdapter;
  private final PropertyNames names;

  public AddressJsonAdapter(Jsonb jsonb) {
    this.stringJsonAdapter = jsonb.adapter(String.class);
    this.cityJsonAdapter = jsonb.adapter(City.class);
    this.suburbJsonAdapter = jsonb.adapter(Suburb.class);
    this.names = jsonb.properties("street", "city", "suburb");
  }

  @Override
  public boolean isViewBuilderAware() {
    return true;
  }

  @Override
  public ViewBuilderAware viewBuild() {
    return this;
  }

  @Override
  public void build(ViewBuilder builder, String name, MethodHandle handle) {
    builder.beginObject(name, handle);
    builder.add("street", stringJsonAdapter, builder.method(Address.class, "getStreet", java.lang.String.class));
    builder.add("city", cityJsonAdapter, builder.method(Address.class, "getCity", City.class));
    builder.add("suburb", suburbJsonAdapter, builder.method(Address.class, "getSuburb", Suburb.class));
    builder.endObject();
  }

  @Override
  public void toJson(JsonWriter writer, Address address) {
    writer.beginObject(names);
    writer.names(names);
    writer.name(0);
    stringJsonAdapter.toJson(writer, address.getStreet());
    writer.name(1);
    cityJsonAdapter.toJson(writer, address.getCity());
    writer.name(2);
    suburbJsonAdapter.toJson(writer, address.getSuburb());
    writer.endObject();
  }

  @Override
  public Address fromJson(JsonReader reader) {
    Address _$address = new Address();

    // read json
    reader.beginObject(names);
    while (reader.hasNextField()) {
      final String fieldName = reader.nextField();
      switch (fieldName) {
        case "street": {
          _$address.setStreet(stringJsonAdapter.fromJson(reader)); break;
        }
        case "city": {
          _$address.setCity(cityJsonAdapter.fromJson(reader)); break;
        }
        case "suburb": {
          _$address.setSuburb(suburbJsonAdapter.fromJson(reader)); break;
        }
        default: {
          reader.unmappedField(fieldName);
          reader.skipValue();
        }
      }
    }
    reader.endObject();

    return _$address;
  }
}
```

## Based on Moshi

`avaje-jsonb` was based on [Moshi](https://github.com/square/moshi) with some changes as summarised below:

#### Changes from Moshi
- Generates Java source code (rather than Kotlin)
- Uses custom parser inspired by `dsl-json` (with option of using `jackson-core`'s `JsonParser` and `JsonGenerator` as parsers)
- Has no fallback to reflection - jsonb is code generation or bust.
- JsonReader - Make JsonReader an interface, default implementation using `Jsonb JsonReadAdapter`
- JsonWriter - Make JsonWriter an interface, default implementation using `Jsonb JsonWriteAdapter`
- JsonAdapter -> Make JsonAdapter an interface.
- Moshi -> Jsonb - Rename Moshi to Jsonb and make it an interface
- Moshi.Builder -> Jsonb.Builder - Basically the same but Jsonb.Builder as interface plus added Component and AdapterBuilder
- Add JsonType for a more friendly API to use rather than the underlying JsonAdapter
- Add Jsonb.Component interface - allows easy service loading of adapters
- Additionally, it generates a Jsonb.Component and uses service loading to auto-register all generated adapters. This means there is no need to register the generated adapters manually.
- Add fromObject() as a "covert from object" feature like Jackson ObjectMapper
- Add naming convention support
- Add `@Json.Import` to generate adapters for types that we can't put the annotation on (types we might not 'own')
- Add support for generating adapters (for `@Json.Import`ed types) with annotations from Jackson, GSON and Jakarta
- Add Mixin feature similar to Jackson Mixins
- Add Types.listOf(), Types.setOf(), Types.mapOf() helper methods
- Adds more common Java types with default built-in support - java.time types, java.util.UUID
- Adds support for json views

## Optional extensions

### Optional support for Jackson, GSON and Jakarta annotations
When using `@Json.Import` for types we "don't own", we provide basic support for annotations from other popular libraries.\
Simply add either `jackson-annotations`, `gson` or `jakarta.json.bind-api`, and use `@Json.Import` to generate an adapter.

| Avaje Jsonb      | Jackson         | Gson                           | Jakarta JSON-B        |
|------------------|-----------------|--------------------------------|-----------------------|
| `@Json.Alias`    | `@JsonAlias`    | `@SerializedName(alternate=…)` | —                     |
| `@Json.Creator`  | `@JsonCreator`  | —                              | `@JsonbCreator`       |
| `@Json.Ignore`   | `@JsonIgnore`   | `@Expose(serialize = false)`   | `@JsonbTransient`     |
| `@Json.Property` | `@JsonProperty` | `@SerializedName`              | `@JsonbProperty`      |
| `@Json.Raw`      | `@JsonRawValue` | —                              | —                     |
| `@Json.Value`    | —               | —                              | —                     |

### Optional support for Spring Web
When using Spring Web, you can use the following dependency to use avaje-jsonb for HTTP serialization:
```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-jsonb-spring-starter</artifactId>
  <version>${avaje-jsonb-version}</version>
</dependency>
```

## Related works
- [moshi](https://github.com/square/moshi), [reddit - why use moshi over gson](https://www.reddit.com/r/androiddev/comments/684flw/why_use_moshi_over_gson/)
- [dsl-json](https://github.com/ngs-doo/dsl-json)
- [jackson core](https://github.com/FasterXML/jackson-core)
- [jackson databind](https://github.com/FasterXML/jackson-databind)

