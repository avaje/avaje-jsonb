[![Build](https://github.com/avaje/avaje-jsonb/actions/workflows/build.yml/badge.svg)](https://github.com/avaje/avaje-jsonb/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.avaje/avaje-jsonb.svg?label=Maven%20Central)](https://mvnrepository.com/artifact/io.avaje/avaje-jsonb)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/avaje/avaje-jsonb/blob/master/LICENSE)

# avaje-jsonb - [docs](https://github.com/avaje/avaje-jsonb/wiki)

json binding via apt source code generation

- Annotate java classes with `@Json` (or use `@Json.Import` for types we "don't own" or can't annotate)
- `avaje-jsonb-generator` annotation processor generates java source code to convert to/from json
- Use `avaje-jsonb` instead of Jackson ObjectMapper to convert to/from json.
- By default uses Jackson core under the hood

[Wiki documentation](https://github.com/avaje/avaje-jsonb/wiki)

[Javadoc API documentation](https://avaje.io/apidocs/avaje-jsonb/io/avaje/jsonb/package-summary.html)

## Goals
- Use Java annotation processing to generate java source for adapting JSON to/from java objects
- Similar in approach to that of Moshi, LoganSquare, dsl-json, ig-json-parser
- Constructors and accessors/getters/setters of any style should all "just work" (record type, constructors, 'fluid setters' all just work)
- As a source code generation style alternative to ObjectMapper, GSON (source code generation approach vs largely reflection based approaches)
- Currently, uses jackson-core for underlying parsing/generation but ideally provides an abstraction allowing targeting of other parsers/generators like JSONP/Yasson, GSON (a bit like the goal of Jakarta JSONB API)
- Provide support for dynamic json views (similar in style to that presented by [LinkedIn at java one in 2009](https://www.slideshare.net/linkedin/building-consistent-restful-apis-in-a-highperformance-environment)


# Quick start

## Step 1 - Add dependencies
```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-jsonb</artifactId>
  <version>0.11</version>
</dependency>

<!-- annotation processor -->
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-jsonb-generator</artifactId>
  <version>0.11</version>
  <scope>provided</scope>
</dependency>
```

## Step 2 - Add `@Json`

Add `@Json` onto types we want to serialise.

The `avaje-jsonb-generator` annotation processor will generate a JsonAdapter as java source code
for each type annotated with `@Json`. These will be automatically registered with Jsonb
when it is started using a service loader mechanism.

For types we can not annotate with `@Json` we can instead use `@Json.Import`.
```java
@Json
public class Customer {
  ...
}
```
```java
@Json
public record Address(String street, String suburb, String city) { }
```

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

## Step 4 - Use json views

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
People may recognise this json view syntax as being pretty much the syntax presented
by [LinkedIn in at java one in 2009](https://www.slideshare.net/linkedin/building-consistent-restful-apis-in-a-highperformance-environment)


## Based off Moshi

`avaje-jsonb` is based off [Moshi](https://github.com/square/moshi) with some changes as summarised below:

#### Changes from Moshi
- Generates Java source code (rather than Kotlin)
- Uses jackson-core `JsonParser` and `JsonGenerator` under the hood with a view of supporting other json-p libraries in future (JSONP/Yasson, GSON etc)
- Has no fallback to reflection approach - this is a code generation or bust approach taken by `avaje-jsonb`
- JsonReader - Make JsonReader an interface, default implementation using `Jackson JsonParser` at this stage
- JsonWriter - Make JsonWriter an interface, default implementation using `Jackson JsonGenerator` at this stage
- JsonAdapter -> JsonAdapter, the key design principal of Moshi remains pretty much as it was
- Moshi -> Jsonb - Rename Moshi to Jsonb and make it an interface
- Moshi.Builder -> Jsonb.Builder - Basically the same but Jsonb.Builder as interface plus added Component and AdapterBuilder
- Add JsonType for a more friendly API to use rather than underlying JsonAdapter
- Add Jsonb.Component interface - allows easy service loading of adapters
- Additionally, generates a Jsonb.Component and uses service loading to auto-register all generated adapters. This means there is no need to manually register the generated adapters.
- Add fromObject() as a "covert from object" feature like Jackson ObjectMapper
- Add naming convention support
- Add `@Json.Import` to generate adapters for types that we can't put the annotation on (types we might not 'own')
- Add Types.listOf(), Types.setOf(), Types.mapOf() helper methods
- Provide an SPI with the view to target other json-p implementations JSONP/Yasson, GSON etc
- More common java types with default built-in support - java.time types, java.util.UUID (need to flesh this out)
- Add support for json views

## Related works
- [moshi](https://github.com/square/moshi), [reddit - why use moshi over gson](https://www.reddit.com/r/androiddev/comments/684flw/why_use_moshi_over_gson/)
- [dsl-json](https://github.com/ngs-doo/dsl-json)
- [LoganSquare](https://github.com/bluelinelabs/LoganSquare)
- [instagram - ig-json-parser](https://github.com/Instagram/ig-json-parser)
- [jackson core](https://github.com/FasterXML/jackson-core)
- [jackson databind](https://github.com/FasterXML/jackson-databind)
- [gson](https://github.com/google/gson)
- [jakarta jsonp](https://github.com/eclipse-ee4j/jsonp)
- [jakarta jsonb api](https://github.com/eclipse-ee4j/jsonb-api)
- [jakarta jsonb reference implementation - yasson](https://github.com/eclipse-ee4j/yasson)

