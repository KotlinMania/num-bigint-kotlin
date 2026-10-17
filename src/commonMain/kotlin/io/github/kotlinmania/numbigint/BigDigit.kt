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

internal const val BITS: UByte = 32u
internal const val HALF_BITS: UByte = 16u
internal val HALF: BigDigit = (1u shl HALF_BITS.toInt()) - 1u

internal const val MAX: BigDigit = UInt.MAX_VALUE
private val LO_MASK: DoubleBigDigit = MAX.toULong()

internal const val BIG_DIGIT_BITS: Int = UInt.SIZE_BITS
internal const val BIG_DIGIT_HALF_BITS: Int = BIG_DIGIT_BITS / 2
internal val BIG_DIGIT_HALF: BigDigit = HALF
internal const val BIG_DIGIT_MAX: BigDigit = MAX

private fun getHi(n: DoubleBigDigit): BigDigit = (n shr BITS.toInt()).toUInt()

private fun getLo(n: DoubleBigDigit): BigDigit = (n and LO_MASK).toUInt()

/**
 * Split one [DoubleBigDigit] into two [BigDigit]s.
 */
internal fun fromDoubleBigDigit(n: DoubleBigDigit): Pair<BigDigit, BigDigit> = Pair(getHi(n), getLo(n))

/**
 * Join two [BigDigit]s into one [DoubleBigDigit].
 */
internal fun toDoubleBigDigit(
    hi: BigDigit,
    lo: BigDigit,
): DoubleBigDigit = lo.toULong() or (hi.toULong() shl BITS.toInt())
