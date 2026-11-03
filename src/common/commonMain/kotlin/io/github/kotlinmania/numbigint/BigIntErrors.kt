package io.github.kotlinmania.numbigint

/*
 * Copyright 2013-2014 The Rust Project Developers.
 *
 * Licensed under the Apache License, Version 2.0 <LICENSE-APACHE or
 * http://www.apache.org/licenses/LICENSE-2.0> or the MIT license
 * <LICENSE-MIT or http://opensource.org/licenses/MIT>, at your
 * option. This file may not be copied, modified, or distributed
 * except according to those terms.
 */

/**
 * Big integer types.
 *
 * * A [BigUint] is unsigned and represented as a vector of digits.
 * * A [BigInt] is signed and is a combination of [BigUint] and [Sign].
 *
 * Common numerical operations are overloaded, so we can treat them
 * the same way we treat other numbers.
 *
 * ## Example
 *
 * ```kotlin
 * // Calculate large fibonacci numbers.
 * fun fib(n: Int): BigUint {
 *     var f0 = BigUint.ZERO
 *     var f1 = BigUint.one()
 *     repeat(n) {
 *         val f2 = f0 + f1
 *         f0 = f1
 *         f1 = f2
 *     }
 *     return f0
 * }
 * ```
 *
 * ## Random Generation
 *
 * `num-bigint` supports the generation of random big integers via [RandBigInt],
 * [RandomBits], [UniformBigInt], and [UniformBigUint].
 *
 * ## Serialization
 *
 * Implementations of `Serialize` and `Deserialize` are provided for both
 * [BigInt] and [BigUint]. Their serialized data is generated portably,
 * regardless of platform differences like the internal digit size.
 */

internal typealias UsizePromotion = ULong
internal typealias IsizePromotion = Long

/**
 * The error type returned when a big integer cannot be parsed from text.
 */
class ParseBigIntError private constructor(
    internal val kind: BigIntErrorKind,
) {
    val message: String
        get() = description()

    fun description(): String = description(this)

    fun fmt(): String = description(this)

    internal companion object {
        fun empty(): ParseBigIntError = ParseBigIntError(BigIntErrorKind.Empty)

        fun invalid(): ParseBigIntError = ParseBigIntError(BigIntErrorKind.InvalidDigit)
    }

    override fun equals(other: Any?): Boolean = this === other || (other is ParseBigIntError && kind == other.kind)

    override fun hashCode(): Int = kind.hashCode()

    override fun toString(): String = fmt()
}

internal enum class BigIntErrorKind {
    Empty,
    InvalidDigit,
}

internal fun description(error: ParseBigIntError): String =
    when (error.kind) {
        BigIntErrorKind.Empty -> "cannot parse integer from empty string"
        BigIntErrorKind.InvalidDigit -> "invalid digit found in string"
    }

/**
 * The error type returned when a checked conversion regarding big integer fails.
 */
class TryFromBigIntError internal constructor(
    private val original: BigInt?,
) {
    val message: String
        get() = description()

    fun description(): String = description(this)

    fun fmt(): String = description(this)

    /**
     * Extract the original value, if available. The value will be available
     * if the type before conversion was either [BigInt] or [BigUint].
     */
    fun intoOriginal(): BigInt? = original

    override fun equals(other: Any?): Boolean =
        this === other ||
            (other is TryFromBigIntError && original == other.original)

    override fun hashCode(): Int = original.hashCode()

    override fun toString(): String = fmt()

    internal companion object {
        fun new(original: BigInt?): TryFromBigIntError = TryFromBigIntError(original)
    }
}

internal fun description(error: TryFromBigIntError): String {
    error.intoOriginal()
    return "out of range conversion regarding big integer attempted"
}

internal class TryFromBigIntException internal constructor(
    val error: TryFromBigIntError,
) : IllegalArgumentException(error.message) {
    override val message: String
        get() = error.message
}
