# Rust callers — `num-bigint-kotlin`

> Generated from `workspace_dep_graph.json` (Cargo-aware dep graph) and workspace caller analysis.

## What this file is

For every Rust crate this repo owns (under `tmp/`), this file lists every **cross-repo** workspace caller: every `use` statement in another `*-kotlin` repo's Rust source that imports a symbol from one of this repo's crates.

**Granularity:** import-level (each row is one `use` line).

**Cross-repo only.** Stdlib (`std`, `core`, `alloc`) and intra-crate sub-module references are excluded.

## Domain file mapping for `lib.rs`

Per workspace transliteration invariant:
- `lib.rs` is **not** ported as `Lib.kt`.
- `mod big_digit` is ported to `src/commonMain/kotlin/io/github/kotlinmania/numbigint/BigDigit.kt`.
- Error types (`ParseBigIntError`, `TryFromBigIntError`, `TryFromBigIntException`) and promotion typealiases are ported to `src/commonMain/kotlin/io/github/kotlinmania/numbigint/BigIntErrors.kt`.
- Public re-exports `BigUint`, `BigInt`, `Sign` are canonical symbols in `biguint/BigUint.kt` and `bigint/BigInt.kt` exposed at crate root `io.github.kotlinmania.numbigint`.

## Crates in this repo

| package | module | inbound use sites | distinct caller crates | distinct caller repos | codex sites |
|---|---|---:|---:|---:|---:|
| `num-bigint` | `num_bigint` | 20 | 6 | 3 | 0 |

## `num-bigint`

### Inbound symbols (3 distinct, 20 total `use` sites)

#### `num_bigint::BigInt` — 16 site(s) (0 codex, 16 other, 5 caller crates)

- `allocative-kotlin/allocative` `tmp/allocative/src/impls/num_bigint.rs:14` — `use num_bigint::BigInt;`
- `starlark-kotlin/pagable` `tmp/pagable/src/impls.rs:17` — `use num_bigint::BigInt;`
- `starlark-kotlin/pagable` `tmp/pagable/src/impls.rs:61` — `use num_bigint::BigInt;`
- `starlark-kotlin/starlark_syntax` `tmp/starlark_syntax/src/lexer.rs:24` — `use num_bigint::BigInt;`
- `starlark-syntax-kotlin/starlark_syntax` `tmp/starlark_syntax/src/lexer.rs:24` — `use num_bigint::BigInt;`
- `starlark-kotlin/starlark` `tmp/starlark/src/stdlib/json.rs:24` — `use num_bigint::BigInt;`
- `starlark-kotlin/starlark` `tmp/starlark/src/values/types/bigint.rs:27` — `use num_bigint::BigInt;`
- `starlark-kotlin/starlark` `tmp/starlark/src/values/types/bigint.rs:299` — `use num_bigint::BigInt;`
- `starlark-kotlin/starlark` `tmp/starlark/src/values/layout/value.rs:46` — `use num_bigint::BigInt;`
- `starlark-kotlin/starlark` `tmp/starlark/src/values/layout/value.rs:1472` — `use num_bigint::BigInt;`
- `starlark-kotlin/starlark` `tmp/starlark/src/values/types/bigint/convert.rs:18` — `use num_bigint::BigInt;`
- `starlark-kotlin/starlark` `tmp/starlark/src/values/types/num/value.rs:230` — `use num_bigint::BigInt;`
- `starlark-kotlin/starlark` `tmp/starlark/src/values/types/int/pointer_i32.rs:27` — `use num_bigint::BigInt;`
- `starlark-kotlin/starlark` `tmp/starlark/src/values/types/int/inline_int.rs:28` — `use num_bigint::BigInt;`
- `starlark-kotlin/starlark` `tmp/starlark/src/values/types/int/int_or_big.rs:31` — `use num_bigint::BigInt;`
- `starlark-kotlin/allocative` `tmp/allocative/allocative/src/impls/num_bigint.rs:15` — `use num_bigint::BigInt;`

#### `num_bigint::BigUint` — 2 site(s) (0 codex, 2 other, 2 caller crates)

- `allocative-kotlin/allocative` `tmp/allocative/src/impls/num_bigint.rs:15` — `use num_bigint::BigUint;`
- `starlark-kotlin/allocative` `tmp/allocative/allocative/src/impls/num_bigint.rs:16` — `use num_bigint::BigUint;`

#### `num_bigint::Sign` — 2 site(s) (0 codex, 2 other, 1 caller crates)

- `starlark-kotlin/starlark` `tmp/starlark/src/values/types/bigint.rs:28` — `use num_bigint::Sign;`
- `starlark-kotlin/starlark` `tmp/starlark/src/values/types/int/int_or_big.rs:32` — `use num_bigint::Sign;`
