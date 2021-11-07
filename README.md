# avaje-jsonb

## json binding via apt source code generation

#### Summary
- Annotate java classes with `@Json` (or use `@Json.Import` for types we "don't own" or can't annotate)
- Use `avaje-jsonb-generator` annotation processor to generate source code to convert to/from json
- Use `avaje-jsonb` instead of Jackson ObjectMapper to convert to/from json


## Goals
- Use Java annotation processing to generate java source for adapting JSON to/from java objects
- Similar approach to that of Moshi, LoganSquare, dsl-json, ig-json-parser
- Constructors and accessors/getters/setters of any style should all "just work" (record type, constructors, 'fluid setters' all just work)
- As an alternative approach to using ObjectMapper, GSON (source code generation approach vs largely reflection based approaches)
- Base off Moshi in design and approach. Noting Moshi only does source code generation for Kotlin and looks to have slightly different goals.
- Also noting that Moshi could be heading away from Java and to converting over to Kotlin.
- Initially uses jackson-core for underlying parsing/generation (unlike Moshi).
- Provide an abstraction allowing targeting of other parser/generators like JSONP/Yasson, GSON (a bit like the goal of Jakarta JSONB API)

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

