# avaje-jsonp

This is based off `jakarta.json-api` and reference implementation `parsson`.

The reason for this fork/effort is due to the `parsson` implementation being too slow / not performant
enough. It is a long way short of the performance desires and at this stage the changes / PR's required
to get it performant seem unlikely to be merged.

The goal of this project is to provide a JSON parsing and generation library that competes with
`Jackson` and `DSL-JSON` performance wise (is frequently faster than `DSL-JSON` which is currently the fastest
JSON parser/generator for Java). This initial commit already includes changes that achieve that performance
goal for the current `avaje-jsonb` jmh benchmark tests we have.

The second goal is to provide a nice minimal dependency. Effectively smaller and more stable than `jackson-core`.
