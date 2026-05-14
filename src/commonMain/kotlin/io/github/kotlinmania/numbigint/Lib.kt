// port-lint: source lib.rs
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
 * Common numerical operations are overloaded, so callers can treat them
 * the same way they treat other numbers.
 *
 * Example:
 *
 * ```kotlin
 * fun fib(n: Int): BigUint {
 *     var f0 = BigUint.ZERO
 *     var f1 = BigUint.ONE
 *     repeat(n) {
 *         val f2 = f0 + f1
 *         f0 = f1
 *         f1 = f2
 *     }
 *     return f0
 * }
 *
 * println("fib(1000) = ${fib(1000)}")
 * ```
 *
 * The upstream crate is tested for compiler version 1.60 and greater.
 */

internal typealias UsizePromotion = ULong
internal typealias IsizePromotion = Long

/**
 * The error type returned when a big integer cannot be parsed from text.
 */
class ParseBigIntError private constructor(
    private val kind: BigIntErrorKind,
) : IllegalArgumentException(kind.description) {
    override val message: String
        get() = kind.description

    internal companion object {
        fun empty(): ParseBigIntError = ParseBigIntError(BigIntErrorKind.Empty)
        fun invalid(): ParseBigIntError = ParseBigIntError(BigIntErrorKind.InvalidDigit)
    }

    override fun equals(other: Any?): Boolean {
        return this === other || (other is ParseBigIntError && kind == other.kind)
    }

    override fun hashCode(): Int = kind.hashCode()

    override fun toString(): String = message
}

private enum class BigIntErrorKind(
    val description: String,
) {
    Empty("cannot parse integer from empty string"),
    InvalidDigit("invalid digit found in string"),
}

/**
 * The error type returned when a checked conversion involving a big integer
 * fails.
 */
class TryFromBigIntError<T> internal constructor(
    private val original: T,
) {
    val message: String
        get() = DESCRIPTION

    /**
     * Extract the original value, if available. The value is available if the
     * type before conversion was either [BigInt] or [BigUint].
     */
    fun intoOriginal(): T = original

    override fun equals(other: Any?): Boolean {
        return this === other ||
            (other is TryFromBigIntError<*> && original == other.original)
    }

    override fun hashCode(): Int = original.hashCode()

    override fun toString(): String = message

    private companion object {
        const val DESCRIPTION = "out of range conversion regarding big integer attempted"
    }
}

class TryFromBigIntException internal constructor(
    val error: TryFromBigIntError<*>,
) : IllegalArgumentException(error.message) {
    override val message: String
        get() = error.message
}

internal typealias BigDigit = UInt
internal typealias DoubleBigDigit = ULong

internal const val BIG_DIGIT_BITS: Int = UInt.SIZE_BITS
internal const val BIG_DIGIT_HALF_BITS: Int = BIG_DIGIT_BITS / 2
internal const val BIG_DIGIT_HALF: BigDigit = 0x0000_FFFFu
internal const val BIG_DIGIT_MAX: BigDigit = UInt.MAX_VALUE

private const val LO_MASK: DoubleBigDigit = 0xFFFF_FFFFuL

internal fun doubleBigDigitHigh(n: DoubleBigDigit): BigDigit {
    return (n shr BIG_DIGIT_BITS).toUInt()
}

internal fun doubleBigDigitLow(n: DoubleBigDigit): BigDigit {
    return (n and LO_MASK).toUInt()
}

/**
 * Split one [DoubleBigDigit] into two [BigDigit]s.
 */
internal fun fromDoubleBigDigit(n: DoubleBigDigit): Pair<BigDigit, BigDigit> {
    return Pair(doubleBigDigitHigh(n), doubleBigDigitLow(n))
}

/**
 * Join two [BigDigit]s into one [DoubleBigDigit].
 */
internal fun toDoubleBigDigit(hi: BigDigit, lo: BigDigit): DoubleBigDigit {
    return lo.toULong() or (hi.toULong() shl BIG_DIGIT_BITS)
}
