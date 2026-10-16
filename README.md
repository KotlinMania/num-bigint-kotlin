# num-bigint-kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Fnum--bigint--kotlin-blue.svg)](https://github.com/KotlinMania/num-bigint-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/num-bigint-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/num-bigint-kotlin)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/num-bigint-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/num-bigint-kotlin/actions)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org/docs/multiplatform.html)

Kotlin Multiplatform line-by-line clean-room port of the Rust crate [`num-bigint`](https://crates.io/crates/num-bigint) — arbitrary precision integers.

This port targets behavioral parity with the upstream Rust crate while presenting an idiomatic Kotlin Multiplatform API. Every Kotlin file is a faithful translation of an upstream Rust file and carries a `// port-lint: source <path>` header so the AST-distance tool can track provenance.

## Supported targets

- macOS arm64
- Linux x64
- Windows mingw-x64
- iOS arm64 / simulator-arm64
- tvOS arm64 / simulator-arm64
- watchOS arm64 / device-arm64 / simulator-arm64
- Android Native arm64 / x64
- JS (browser + Node.js)
- Wasm-JS (browser + Node.js)
- Wasm-WASI (Node.js)
- Android (API 24+)

## Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:num-bigint-kotlin:0.1.1")
}
```

## Build

```bash
./gradlew build
./gradlew test
```

## Apple-facing API

`BigInt` and `BigUint` expose their companion factories to Swift. The Swift
consumer tests construct both types and exercise arithmetic, formatting and
parsing rather than checking only that the module imports.

Parsing returns `BigIntParseResult` or `BigUintParseResult`. Checked conversions
return concrete `BigIntToLongResult`, `BigIntToULongResult`,
`BigIntToBigUintResult` or `BigUintToUIntResult` values. Each result contains
exactly one non-null `value` or `error`, with `isSuccess()`, `isFailure()`,
`getOrNull()` and `getOrThrow()` accessors. Error values do not extend
`Throwable`; Kotlin callers can use `getOrThrow()` to raise an
`IllegalArgumentException` on failure.

These concrete results replace `kotlin.Result` at the public parsing and
checked-conversion boundary. Code explicitly typed as `kotlin.Result` must use
the corresponding concrete result type; generic `Result` extension operations
are not part of this API. `TryFromBigIntError` is non-generic and retains a
`BigInt` input for failed signed-to-unsigned-big-integer conversion.

## Porting guidelines

See [CLAUDE.md](CLAUDE.md) and [AGENTS.md](AGENTS.md) for translator discipline, port-lint header convention, and Rust → Kotlin idiom mapping.

## Maintainer

**Sydney Renee** ([@sydneyrenee](https://github.com/sydneyrenee)) — *The Solace Project* — <sydney@solace.ofharmony.ai>

## Credits

This Kotlin port stands entirely on the shoulders of the upstream Rust [`num-bigint`](https://github.com/rust-num/num-bigint) crate. Sincere thanks to:

- **The Rust Project Developers** and the [rust-num](https://github.com/rust-num) contributors — original authors and maintainers of the [`num-bigint`](https://github.com/rust-num/num-bigint) crate. The crate's design, parser, constants, and test suite are theirs; this repository merely translates that work into Kotlin Multiplatform.

If you find this port useful, please also consider starring the upstream project — it is the source of all the real engineering credit here.

## License

Licensed under the **Apache License, Version 2.0** — see [LICENSE](LICENSE).

The upstream Rust [`num-bigint`](https://github.com/rust-num/num-bigint) crate is dual-licensed `MIT OR Apache-2.0`; this Kotlin port chooses Apache-2.0. Original copyright belongs to The Rust Project Developers and the rust-num contributors. Kotlin port copyright © 2026 Sydney Renee and The Solace Project.
