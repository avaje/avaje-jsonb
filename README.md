[![Build](https://github.com/avaje/avaje-jsonb/actions/workflows/build.yml/badge.svg)](https://github.com/avaje/avaje-jsonb/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.avaje/avaje-jsonb.svg?label=Maven%20Central)](https://mvnrepository.com/artifact/io.avaje/avaje-jsonb)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/avaje/avaje-jsonb/blob/master/LICENSE)
[![Discord](https://img.shields.io/discord/1074074312421683250?color=%237289da&label=discord)](https://discord.gg/Qcqf9R27BR)

# [Avaje-JsonB](https://avaje.io/jsonb/)

Fast, reflection-free Json binding via apt source code generation. A light (~188kb + generated code) source code generation style alternative to Jacksons ObjectMapper, Gson (code generation vs reflection)

- Annotate java classes with `@Json` (or use `@Json.Import` for types we "don't own" or can't annotate)
- `avaje-jsonb-generator` annotation processor generates java source code to convert to/from json
- No need to manually register generated adapters. (Uses ServiceLoader to auto-register)
- Constructors and accessors/getters/setters of any style "just work" (records, constructors, 'fluid setters')
- Jackson-like annotations: `@Json.Raw`, `@Json.Property`, `@Json.Ignore`, `@Json.Alias`, etc.
- Support Mixins (adding jsonb features to types we can't directly annotate).
- Supports Generic Types.
- Provides support for dynamic json views (similar in style to that presented by [LinkedIn at java one in 2009](https://www.slideshare.net/linkedin/building-consistent-restful-apis-in-a-highperformance-environment)

### Built-in Type Adapters

Built-in support for reading and writing Java’s core data types:

 * Primitives (int, float, char...) and their boxed counterparts (Integer, Float, Character...).
 * BigInteger and BigDecimal
 * java.time classes (Instant, LocalDate, LocalDateTime...)
 * Arrays, Collections, Streams, Lists, Sets, and Maps
 * Strings
 * Enums
 * Other miscellaneous types (UUID, URL, URI)


# Quick Start

## Step 1 - Add dependency
```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-jsonb</artifactId>
  <version>${avaje-jsonb-version}</version>
</dependency>
```
And add avaje-jsonb-generator as a annotation processor
```xml

<!-- Annotation processors -->
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje.jsonb-generator</artifactId>
  <version>${avaje.jsonb.version}</version>
  <scope>provided</scope>
</dependency>
```
NOTE: If you have another annotation processor defined in the maven compiler plugin you will need to add it there.
```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>${maven-compiler-plugin.version}</version>
      <configuration>
        <annotationProcessorPaths>
          <path>
            <groupId>io.avaje</groupId>
            <artifactId>avaje-jsonb-generator</artifactId>
            <version>${avaje-jsonb-version}</version>
          </path>
        </annotationProcessorPaths>
      </configuration>
    </plugin>
  </plugins>
</build>
```

## Step 2 - Add `@Json`

Add `@Json` onto types we want to serialise.

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
  private OtherClass;

  //add getters/setters
}
```

Also works with records:
```java
@Json
public record Address(String street, String suburb, String city) { }
```

For types we cannot annotate with `@Json` we can place `@Json.Import(TypeToimport.class)` on any class/package-info to generate the adpaters.

## Step 3 - Use

```java
// build using defaults
Jsonb jsonb = Jsonb.builder().build();

JsonType<Customer> customerType = jsonb.type(Customer.class);

Customer customer = ...;

// serialise to json
String asJson =  customerType.toJson(customer);

// deserialse from json
Customer customer = customerType.fromJson(asJson);
```

## Step 4 - Use Json views

`avaje-jsonb` supports dynamic json views. This allows us to specify which specific properties
to include when serialising to json.

For example:

```java
Jsonb jsonb = Jsonb.builder().build();

JsonType<Customer> customerType = jsonb.type(Customer.class);

// only including the id and name
JsonView<Customer> idAndNameView = customerType.view("(id, name)");
String asJson =  idAndNameView.toJson(customer);


JsonView<Customer> myView =
  customerType.view("(id, name, billingAddress(*), contacts(lastName, email))");

// serialise to json the above specified properties only
String asJson =  myView.toJson(customer);
```

## Based off Moshi

`avaje-jsonb` is based off [Moshi](https://github.com/square/moshi) with some changes as summarised below:

#### Changes from Moshi
- Generates Java source code (rather than Kotlin)
- uses custom parser inspired by dsl-json (with option of using jackson-core `JsonParser` and `JsonGenerator` as parsers)
- Has no fallback to reflection - jsonb is code generation or bust.`
- JsonReader - Make JsonReader an interface, default implementation using `Jsonb JsonReadAdapter`
- JsonWriter - Make JsonWriter an interface, default implementation using `Jsonb JsonWriteAdapter`
- JsonAdapter -> JsonAdapter, the key design principal of Moshi remains as is.
- Moshi -> Jsonb - Rename Moshi to Jsonb and make it an interface
- Moshi.Builder -> Jsonb.Builder - Basically the same but Jsonb.Builder as interface plus added Component and AdapterBuilder
- Add JsonType for a more friendly API to use rather than underlying JsonAdapter
- Add Jsonb.Component interface - allows easy service loading of adapters
- Additionally, generates a Jsonb.Component and uses service loading to auto-register all generated adapters. This means there is no need to manually register the generated adapters.
- Add fromObject() as a "covert from object" feature like Jackson ObjectMapper
- Add naming convention support
- Add `@Json.Import` to generate adapters for types that we can't put the annotation on (types we might not 'own')
- Add Mixin feature similar to Jackson Mixins
- Add Types.listOf(), Types.setOf(), Types.mapOf() helper methods
- Provide an SPI with the view to target other json-p implementations JSONP/Yasson, GSON etc
- Adds more common java types with default built-in support - java.time types, java.util.UUID
- Adds support for json views

## Related works
- [moshi](https://github.com/square/moshi), [reddit - why use moshi over gson](https://www.reddit.com/r/androiddev/comments/684flw/why_use_moshi_over_gson/)
- [dsl-json](https://github.com/ngs-doo/dsl-json)
- [jackson core](https://github.com/FasterXML/jackson-core)
- [jackson databind](https://github.com/FasterXML/jackson-databind)

