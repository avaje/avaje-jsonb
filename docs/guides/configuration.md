# Configuration & Tuning

Avaje Jsonb exposes several low-level parser and generator settings that control
buffer sizes and parsing limits. These can be tuned at startup via **system
properties** or **environment variables** — no code changes required.

## Settings Reference

| Setting | System Property | ENV Var | Default |
|---------|----------------|---------|---------|
| Generator write buffer | `jsonb.generatorBufferSize` | `JSONB_GENERATOR_BUFFER_SIZE` | `4096` |
| Parser byte buffer | `jsonb.parserBufferSize` | `JSONB_PARSER_BUFFER_SIZE` | `4096` |
| Parser char buffer | `jsonb.parserCharBufferSize` | `JSONB_PARSER_CHAR_BUFFER_SIZE` | `4096` |
| Max number digits | `jsonb.parserMaxNumberDigits` | `JSONB_PARSER_MAX_NUMBER_DIGITS` | `309` |
| Max string buffer | `jsonb.parserMaxStringBuffer` | `JSONB_PARSER_MAX_STRING_BUFFER` | `50000` |

### Priority order

System property → ENV var → default. Setting a system property always wins over an ENV var.

## When to Tune

### `parserMaxStringBuffer`

The most commonly tuned setting. If a single JSON string value (e.g. a large
text field or a base64-encoded blob) exceeds 50,000 characters the parser throws:

```
Maximum string buffer limit exceeded: '50000' near line 1, column 128
```

Raise the limit to accommodate your largest expected string:

```bash
# ENV var (recommended for containers / native images)
export JSONB_PARSER_MAX_STRING_BUFFER=500000

# System property (JVM flag)
-Djsonb.parserMaxStringBuffer=500000
```

### Buffer sizes

The byte and char buffers are pre-allocated per parser instance and reused across
requests via the buffer recycler pool. Increasing them avoids buffer reallocations
for large payloads at the cost of more baseline memory per thread.

Only tune these if profiling shows allocation pressure from buffer growth:

```bash
export JSONB_PARSER_BUFFER_SIZE=16384
export JSONB_PARSER_CHAR_BUFFER_SIZE=16384
export JSONB_GENERATOR_BUFFER_SIZE=16384
```

### `parserMaxNumberDigits`

Guards against pathological inputs with extremely long numeric literals. The
default of 309 covers the maximum significant digits in a `double`. Rarely needs
changing.

## GraalVM Native Image

When running as a GraalVM native image, `JAVA_TOOL_OPTIONS` is not supported, so
`-D` system properties cannot be passed at container startup. **Use environment
variables instead** — they work identically at native-image runtime:

```bash
# In your container / pod spec
env:
  - name: JSONB_PARSER_MAX_STRING_BUFFER
    value: "200000"
```

This is the primary reason ENV var support was added alongside system properties.

## Jsonb Builder Options

Runtime behaviour (null handling, unknown fields, etc.) is configured through
`Jsonb.builder()` rather than system properties:

```java
Jsonb jsonb = Jsonb.builder()
  .serializeNulls(true)       // include null fields in output
  .serializeEmpty(true)       // include empty collections in output
  .failOnUnknown(true)        // throw on unrecognised JSON fields
  .build();
```

## Parse Error Messages

Parser errors always include the position and surrounding context to make
debugging straightforward:

```
Unexpected character at line 3, column 12, near: ...,"value":xyz,...
Maximum string buffer limit exceeded: '50000' near line 1, column 4096
```

No configuration is needed — full error detail is always produced.
