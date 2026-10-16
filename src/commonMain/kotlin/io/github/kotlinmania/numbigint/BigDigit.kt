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

internal typealias BigDigit = UInt
internal typealias DoubleBigDigit = ULong

internal const val BIG_DIGIT_BITS: Int = UInt.SIZE_BITS
internal const val BIG_DIGIT_HALF_BITS: Int = BIG_DIGIT_BITS / 2
internal const val BIG_DIGIT_HALF: BigDigit = 0x0000_FFFFu
internal const val BIG_DIGIT_MAX: BigDigit = UInt.MAX_VALUE

private const val LO_MASK: DoubleBigDigit = 0xFFFF_FFFFuL

internal fun doubleBigDigitHigh(n: DoubleBigDigit): BigDigit = (n shr BIG_DIGIT_BITS).toUInt()

internal fun doubleBigDigitLow(n: DoubleBigDigit): BigDigit = (n and LO_MASK).toUInt()

/**
 * Split one [DoubleBigDigit] into two [BigDigit]s.
 */
internal fun fromDoubleBigDigit(n: DoubleBigDigit): Pair<BigDigit, BigDigit> = Pair(doubleBigDigitHigh(n), doubleBigDigitLow(n))

/**
 * Join two [BigDigit]s into one [DoubleBigDigit].
 */
internal fun toDoubleBigDigit(
    hi: BigDigit,
    lo: BigDigit,
): DoubleBigDigit = lo.toULong() or (hi.toULong() shl BIG_DIGIT_BITS)
