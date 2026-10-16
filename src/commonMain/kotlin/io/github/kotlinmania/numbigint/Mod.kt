package io.github.kotlinmania.numbigint

// Tracking file for upstream num-bigint/src/lib.rs.
//
// Upstream module declarations:
//   Module `macros` provides internal arithmetic forwarding helpers.
//   Module `bigint` provides signed BigInt, Sign, and ToBigInt.
//   Module `bigrand` provides random big integer generation when rand is enabled.
//   Module `biguint` provides unsigned BigUint, ToBigUint, U32Digits, and U64Digits.
//   Module `big_digit` is parceled into `BigDigit.kt`.
//   Error types and size promotions are parceled into `BigIntErrors.kt`.
//
// Upstream re-exports:
//   Re-exports BigUint, ToBigUint, U32Digits, and U64Digits from biguint
//   Re-exports BigInt, Sign, and ToBigInt from bigint
//   When rand feature is enabled: re-exports RandBigInt, RandomBits, UniformBigInt, and UniformBigUint from bigrand
//
// Callers migrated:
//   allocative-kotlin/allocative -> io.github.kotlinmania.numbigint.BigInt, io.github.kotlinmania.numbigint.BigUint
//   starlark-kotlin/pagable -> io.github.kotlinmania.numbigint.BigInt
//   starlark-kotlin/starlark_syntax -> io.github.kotlinmania.numbigint.BigInt
//   starlark-syntax-kotlin/starlark_syntax -> io.github.kotlinmania.numbigint.BigInt
//   starlark-kotlin/starlark -> io.github.kotlinmania.numbigint.BigInt, io.github.kotlinmania.numbigint.Sign
//   starlark-kotlin/allocative -> io.github.kotlinmania.numbigint.BigInt, io.github.kotlinmania.numbigint.BigUint
