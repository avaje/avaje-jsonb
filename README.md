# jsonb via source code generation

Goals:
- Use Java annotation processing to generate java source for adapting JSON to/from java objects
- Somewhat similar approach to that of Moshi, LoganSquare, JSON DSL
- As an alternative approach to using ObjectMapper, GSON (largely reflection based approaches)
- Base off Moshi in design and approach. Noting Moshi only does source code generation for Kotlin.
- Also noting that Moshi looks to be heading away from Java and to converting over to Kotlin.
- Initially target jackson-core for underlying parsing/generation (unlike Moshi)
- Ideally provides an abstraction allowing targeting of other parser/generators like GSON, JSONP (a bit like the goal of JSONB API)

## Summary of changes from Moshi
- Generates Java source code
- Has no fallback to reflection approach - a code gen or bust approach here
- JsonAdapter -> JsonAdapter - the key design principal of Moshi remains pretty much as it was
- Moshi -> Jsonb - Rename Moshi to Jsonb and make it an interface
- Moshi.Builder -> Jsonb.Builder - Basically the same but Jsonb.Builder as interface plus added Component and AdapterBuilder
- JsonReader - Make JsonReader an interface, implemented using Jackson JsonParser at this stage
- JsonWriter - Make JsonWriter an interface, implemented using Jackson JsonGenerator at this stage
- Add JsonType for a more friendly API rather than underlying JsonAdapter
- Add Jsonb.Component interface
- Also generate a Jsonb.Component and use service loading to auto-register all generated adapters. No need to manually register the generated adapters.
- Add fromObject() as a "covert from object" like what Jackson ObjectMapper has
- Add naming convention support
- Add `@Json.Import` to generate adapters for types that we can't put the annotation on
- Add Types.listOf(), Types.setOf(), Types.mapOf() helper methods
- Provide an SPI with the view to target other json-p implementations JSONP/Yasson, GSON etc
- Hide more internals


## Related works and links
- [moshi](https://github.com/square/moshi)
- [reddit - why use moshi over gson](https://www.reddit.com/r/androiddev/comments/684flw/why_use_moshi_over_gson/)
- [LoganSquare](https://github.com/bluelinelabs/LoganSquare)
- [instagram - ig-json-parser](https://github.com/Instagram/ig-json-parser)
- [jackson core](https://github.com/FasterXML/jackson-core)
- [jackson databind](https://github.com/FasterXML/jackson-databind)
- [gson](https://github.com/google/gson)
- [jakarta jsonp](https://github.com/eclipse-ee4j/jsonp)
- [jakarta jsonb api](https://github.com/eclipse-ee4j/jsonb-api)
- [jakarta jsonb reference implementation - yasson](https://github.com/eclipse-ee4j/yasson)

