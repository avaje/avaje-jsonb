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
- JsonAdapter -> JsonAdapter (the key design principal of Moshi remains)
- Moshi -> Jsonb (Convert Moshi implementation to Jsonb interface)
- Moshi.Builder -> Jsonb.Builder (Basically the same but Jsonb.Builder as interface)
- JsonReader -> JsonReader (Make JsonReader an interface, implmentated using Jackson JsonParser at this stage)
- JsonWriter -> JsonWriter (Make JsonWriter an interface, implmentated using Jackson JsonGenerator at this stage)
- Add JsonType (For more user friendly use of API rather than underlying JsonAdapter)
- Provide a SPI (Ideally allow implementations using JSONP and GSON)
- Hide internals


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
 
