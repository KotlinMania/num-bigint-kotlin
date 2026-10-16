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
 */

internal typealias UsizePromotion = ULong
internal typealias IsizePromotion = Long

/**
 * The error type returned when a big integer cannot be parsed from text.
 */
class ParseBigIntError private constructor(
    private val kind: BigIntErrorKind,
) {
    val message: String
        get() = kind.description

    internal companion object {
        fun empty(): ParseBigIntError = ParseBigIntError(BigIntErrorKind.Empty)

        fun invalid(): ParseBigIntError = ParseBigIntError(BigIntErrorKind.InvalidDigit)
    }

    override fun equals(other: Any?): Boolean = this === other || (other is ParseBigIntError && kind == other.kind)

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
class TryFromBigIntError internal constructor(
    private val original: BigInt?,
) {
    val message: String
        get() = DESCRIPTION

    /**
     * Extract the original value, if available. The value is available if the
     * type before conversion was either [BigInt] or [BigUint].
     */
    fun intoOriginal(): BigInt? = original

    override fun equals(other: Any?): Boolean =
        this === other ||
            (other is TryFromBigIntError && original == other.original)

    override fun hashCode(): Int = original.hashCode()

    override fun toString(): String = message

    private companion object {
        const val DESCRIPTION = "out of range conversion regarding big integer attempted"
    }
}

internal class TryFromBigIntException internal constructor(
    val error: TryFromBigIntError,
) : IllegalArgumentException(error.message) {
    override val message: String
        get() = error.message
}
