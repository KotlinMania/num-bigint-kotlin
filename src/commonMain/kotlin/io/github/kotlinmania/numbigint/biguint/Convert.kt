// port-lint: source biguint/convert.rs
package io.github.kotlinmania.numbigint

import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.truncate

/**
 * Find last set bit.
 * `fls(0) == 0`, `fls(UInt.MAX_VALUE) == 32`.
 */
private fun fls(v: UInt): UInt {
    return UInt.SIZE_BITS.toUInt() - v.countLeadingZeroBits().toUInt()
}

private fun fls(v: ULong): UInt {
    return ULong.SIZE_BITS.toUInt() - v.countLeadingZeroBits().toUInt()
}

private fun ilog2(v: UInt): UInt {
    return fls(v) - 1u
}

// Convert from a power of two radix where bits evenly divides `BIG_DIGIT_BITS`.
internal fun fromBitwiseDigitsLe(v: List<UByte>, bits: UInt): BigUint {
    check(v.isNotEmpty() && bits <= 8u && BIG_DIGIT_BITS.toUInt() % bits == 0u)
    check(v.all { it.toUInt() < (1u shl bits.toInt()) })

    val digitsPerBigDigit = BIG_DIGIT_BITS.toUInt() / bits

    val data = mutableListOf<BigDigit>()
    var i = 0
    while (i < v.size) {
        var acc = 0u
        val end = minOf(i + digitsPerBigDigit.toInt(), v.size)
        var j = end - 1
        while (j >= i) {
            acc = (acc shl bits.toInt()) or v[j].toUInt()
            j -= 1
        }
        data.add(acc)
        i = end
    }

    return biguintFromVec(data)
}

// Convert from a power of two radix where bits doesn't evenly divide `BIG_DIGIT_BITS`.
private fun fromInexactBitwiseDigitsLe(v: List<UByte>, bits: UInt): BigUint {
    check(v.isNotEmpty() && bits <= 8u && BIG_DIGIT_BITS.toUInt() % bits != 0u)
    check(v.all { it.toUInt() < (1u shl bits.toInt()) })

    val bigDigits = divCeil(v.size.toULong() * bits.toULong(), BIG_DIGIT_BITS.toULong())
    check(bigDigits <= Int.MAX_VALUE.toULong())
    val data = ArrayList<BigDigit>(bigDigits.toInt())

    var d = 0u
    var dbits = 0u

    // walk v accumulating bits in d; whenever we accumulate `BIG_DIGIT_BITS` in d,
    // spit out a big digit:
    for (c in v) {
        d = d or (c.toUInt() shl dbits.toInt())
        dbits += bits

        if (dbits >= BIG_DIGIT_BITS.toUInt()) {
            data.add(d)
            dbits -= BIG_DIGIT_BITS.toUInt()
            // if dbits was greater than `BIG_DIGIT_BITS`, we dropped some of the bits in c
            // that couldn't fit in d; grab the bits we lost here:
            d = c.toUInt() shr (bits - dbits).toInt()
        }
    }

    if (dbits > 0u) {
        check(dbits < BIG_DIGIT_BITS.toUInt())
        data.add(d)
    }

    return biguintFromVec(data)
}

// Read big-endian radix digits.
private fun fromRadixDigitsBe(v: List<UByte>, radix: UInt): BigUint {
    check(v.isNotEmpty() && !radix.isPowerOfTwo())
    check(v.all { it.toUInt() < radix })

    var result = BigUint.zero()
    for (digit in v) {
        result.timesAssign(radix)
        result.plusAssign(digit.toUInt())
    }
    return result
}

internal fun fromRadixBeDigits(buf: List<UByte>, radix: UInt): BigUint? {
    require(radix in 2u..256u) { "The radix must be within 2...256" }

    if (buf.isEmpty()) {
        return BigUint.ZERO
    }

    if (radix != 256u && buf.any { it.toUInt() >= radix }) {
        return null
    }

    val res = if (radix.isPowerOfTwo()) {
        // Powers of two can use bitwise masks and shifting instead of multiplication.
        val bits = ilog2(radix)
        val v = buf.asReversed()
        if (BIG_DIGIT_BITS.toUInt() % bits == 0u) {
            fromBitwiseDigitsLe(v, bits)
        } else {
            fromInexactBitwiseDigitsLe(v, bits)
        }
    } else {
        fromRadixDigitsBe(buf, radix)
    }

    return res
}

internal fun fromRadixLeDigits(buf: List<UByte>, radix: UInt): BigUint? {
    require(radix in 2u..256u) { "The radix must be within 2...256" }

    if (buf.isEmpty()) {
        return BigUint.ZERO
    }

    if (radix != 256u && buf.any { it.toUInt() >= radix }) {
        return null
    }

    val res = if (radix.isPowerOfTwo()) {
        // Powers of two can use bitwise masks and shifting instead of multiplication.
        val bits = ilog2(radix)
        if (BIG_DIGIT_BITS.toUInt() % bits == 0u) {
            fromBitwiseDigitsLe(buf, bits)
        } else {
            fromInexactBitwiseDigitsLe(buf, bits)
        }
    } else {
        fromRadixDigitsBe(buf.asReversed(), radix)
    }

    return res
}

/**
 * Creates and initializes a `BigUint`.
 */
internal fun fromStrRadixImpl(s0: String, radix: UInt): Result<BigUint> {
    require(radix in 2u..36u) { "The radix must be within 2...36" }
    var s = s0
    if (s.startsWith("+")) {
        val tail = s.drop(1)
        if (!tail.startsWith("+")) {
            s = tail
        }
    }

    if (s.isEmpty()) {
        return Result.failure(ParseBigIntError.empty())
    }

    if (s.startsWith("_")) {
        // Must lead with a real digit!
        return Result.failure(ParseBigIntError.invalid())
    }

    // First normalize all characters to plain digit values.
    val v = ArrayList<UByte>(s.length)
    for (ch in s) {
        val d = when (ch) {
            in '0'..'9' -> ch.code - '0'.code
            in 'a'..'z' -> ch.code - 'a'.code + 10
            in 'A'..'Z' -> ch.code - 'A'.code + 10
            '_' -> continue
            else -> UByte.MAX_VALUE.toInt()
        }
        if (d.toUInt() < radix) {
            v.add(d.toUByte())
        } else {
            return Result.failure(ParseBigIntError.invalid())
        }
    }

    val res = if (radix.isPowerOfTwo()) {
        // Powers of two can use bitwise masks and shifting instead of multiplication.
        val bits = ilog2(radix)
        v.reverse()
        if (BIG_DIGIT_BITS.toUInt() % bits == 0u) {
            fromBitwiseDigitsLe(v, bits)
        } else {
            fromInexactBitwiseDigitsLe(v, bits)
        }
    } else {
        fromRadixDigitsBe(v, radix)
    }
    return Result.success(res)
}

private fun highBitsToU64(v: BigUint): ULong {
    return when (v.data.size) {
        0 -> 0uL
        1 -> v.data[0].toULong()
        else -> {
            var bits = v.bits()
            var ret = 0uL
            var retBits = 0u

            for (d in v.data.asReversed()) {
                val digitBits = (bits - 1uL) % BIG_DIGIT_BITS.toULong() + 1uL
                val bitsWant = minOf(64u - retBits, digitBits.toUInt())

                if (bitsWant != 0u) {
                    if (bitsWant != 64u) {
                        ret = ret shl bitsWant.toInt()
                    }
                    val d0 = d.toULong() shr (digitBits - bitsWant.toULong()).toInt()
                    ret = ret or d0
                }

                // Implement round-to-odd: If any lower bits are 1, set LSB to 1
                // so that rounding again to floating point value using
                // nearest-ties-to-even is correct.
                if (digitBits - bitsWant.toULong() != 0uL) {
                    val masked = d.toULong() shl (64 - (digitBits - bitsWant.toULong()).toInt())
                    if (masked != 0uL) {
                        ret = ret or 1uL
                    }
                }

                retBits += bitsWant
                bits -= bitsWant.toULong()
            }

            ret
        }
    }
}

fun BigUint.toLongOrNull(): Long? {
    return toULongOrNull()?.takeIf { it <= Long.MAX_VALUE.toULong() }?.toLong()
}

fun BigUint.toUIntOrNull(): UInt? {
    return when (data.size) {
        0 -> 0u
        1 -> data[0]
        else -> null
    }
}

fun BigUint.toULongOrNull(): ULong? {
    var ret = 0uL
    var bits = 0

    for (i in data) {
        if (bits >= 64) {
            return null
        }
        ret += i.toULong() shl bits
        bits += BIG_DIGIT_BITS
    }

    return ret
}

fun BigUint.toDoubleOrNull(): Double? {
    val mantissa = highBitsToU64(this)
    if (mantissa == 0uL) {
        return 0.0
    }
    val exponent = bits() - fls(mantissa).toULong()

    return if (exponent > DOUBLE_MAX_EXPONENT_VALUE.toULong()) {
        Double.POSITIVE_INFINITY
    } else {
        mantissa.toDouble() * 2.0.pow(exponent.toInt())
    }
}

fun BigUint.tryToUInt(): Result<UInt> {
    return toUIntOrNull()?.let { Result.success(it) }
        ?: Result.failure(TryFromBigIntException(TryFromBigIntError(Unit)))
}

internal fun fromULongImpl(n0: ULong): BigUint {
    var n = n0
    val ret = BigUint.ZERO

    while (n != 0uL) {
        ret.data.add(n.toUInt())
        n = (n shr 1) shr (BIG_DIGIT_BITS - 1)
    }

    return ret
}

internal fun fromDoubleImpl(n0: Double): BigUint? {
    // handle not-a-number, positive infinity, negative infinity
    if (!n0.isFinite()) {
        return null
    }

    // match the rounding of casting from float to int
    var n = truncate(n0)

    // handle 0.x and -0.x
    if (n == 0.0) {
        return BigUint.ZERO
    }
    if (n < 0.0) {
        return null
    }

    val ret = BigUint.ZERO
    val base = 2.0.pow(BIG_DIGIT_BITS)
    while (n > 0.0) {
        val digit = (n % base).toLong().toUInt()
        ret.data.add(digit)
        n = floor(n / base)
    }
    return ret.normalized()
}

fun BigUint.toBigUint(): BigUint? {
    return clone()
}

fun Int.toBigUint(): BigUint? {
    return if (this >= 0) toUInt().toBigUint() else null
}

fun UInt.toBigUint(): BigUint {
    return BigUint.fromUInt(this)
}

fun Long.toBigUint(): BigUint? {
    return if (this >= 0) toULong().toBigUint() else null
}

fun ULong.toBigUint(): BigUint {
    return BigUint.fromULong(this)
}

fun Float.toBigUint(): BigUint? {
    return BigUint.fromDouble(toDouble())
}

fun Double.toBigUint(): BigUint? {
    return BigUint.fromDouble(this)
}

fun Boolean.toBigUint(): BigUint {
    return if (this) BigUint.one() else BigUint.ZERO
}

// Extract bitwise digits that evenly divide `BigDigit`.
internal fun toBitwiseDigitsLe(u: BigUint, bits: UInt): List<UByte> {
    check(!u.isZero() && bits <= 8u && BIG_DIGIT_BITS.toUInt() % bits == 0u)

    val lastI = u.data.size - 1
    val mask = (1u shl bits.toInt()) - 1u
    val digitsPerBigDigit = BIG_DIGIT_BITS.toUInt() / bits
    val digits = divCeil(u.bits(), bits.toULong())
    check(digits <= Int.MAX_VALUE.toULong())
    val res = ArrayList<UByte>(digits.toInt())

    for (idx in 0 until lastI) {
        var r = u.data[idx]
        repeat(digitsPerBigDigit.toInt()) {
            res.add((r and mask).toUByte())
            r = r shr bits.toInt()
        }
    }

    var r = u.data[lastI]
    while (r != 0u) {
        res.add((r and mask).toUByte())
        r = r shr bits.toInt()
    }

    return res
}

// Extract bitwise digits that don't evenly divide `BigDigit`.
private fun toInexactBitwiseDigitsLe(u: BigUint, bits: UInt): List<UByte> {
    check(!u.isZero() && bits <= 8u && BIG_DIGIT_BITS.toUInt() % bits != 0u)

    val mask = (1u shl bits.toInt()) - 1u
    val digits = divCeil(u.bits(), bits.toULong())
    check(digits <= Int.MAX_VALUE.toULong())
    val res = ArrayList<UByte>(digits.toInt())

    var r = 0u
    var rbits = 0u

    for (c in u.data) {
        r = r or (c shl rbits.toInt())
        rbits += BIG_DIGIT_BITS.toUInt()

        while (rbits >= bits) {
            res.add((r and mask).toUByte())
            r = r shr bits.toInt()

            // r had more bits than it could fit; grab the bits we lost.
            if (rbits > BIG_DIGIT_BITS.toUInt()) {
                r = c shr (BIG_DIGIT_BITS.toUInt() - (rbits - bits)).toInt()
            }

            rbits -= bits
        }
    }

    if (rbits != 0u) {
        res.add(r.toUByte())
    }

    while (res.lastOrNull() == 0.toUByte()) {
        res.removeAt(res.lastIndex)
    }

    return res
}

// Extract little-endian radix digits.
internal fun toRadixDigitsLe(u: BigUint, radix: UInt): List<UByte> {
    check(!u.isZero() && !radix.isPowerOfTwo())

    // Estimate how big the result will be, so we can pre-allocate it.
    val radixLog2 = kotlin.math.log2(radix.toDouble())
    val radixDigits = (u.bits().toDouble() / radixLog2).let { kotlin.math.ceil(it).toInt() }
    val res = ArrayList<UByte>(radixDigits)

    var digits = u.clone()
    val (base, power) = if (FAST_DIV_WIDE) {
        getRadixBase(radix)
    } else {
        getHalfRadixBase(radix)
    }
    while (digits.data.size > 1) {
        val (q, rem0) = divRemDigit(digits, base)
        var rem = rem0
        repeat(power) {
            res.add((rem % radix).toUByte())
            rem /= radix
        }
        digits = q
    }

    var rem = digits.data[0]
    while (rem != 0u) {
        res.add((rem % radix).toUByte())
        rem /= radix
    }

    return res
}

internal fun toRadixLeDigits(u: BigUint, radix: UInt): List<UByte> {
    return if (u.isZero()) {
        listOf(0.toUByte())
    } else if (radix.isPowerOfTwo()) {
        // Powers of two can use bitwise masks and shifting instead of division.
        val bits = ilog2(radix)
        if (BIG_DIGIT_BITS.toUInt() % bits == 0u) {
            toBitwiseDigitsLe(u, bits)
        } else {
            toInexactBitwiseDigitsLe(u, bits)
        }
    } else if (radix == 10u) {
        // 10 is so common that it's worth separating out for const-propagation.
        // Optimizers can often turn constant division into a faster multiplication.
        toRadixDigitsLe(u, 10u)
    } else {
        toRadixDigitsLe(u, radix)
    }
}

internal fun toStrRadixReversed(u: BigUint, radix: UInt): List<UByte> {
    require(radix in 2u..36u) { "The radix must be within 2...36" }

    if (u.isZero()) {
        return listOf('0'.code.toUByte())
    }

    val res = toRadixLeDigits(u, radix).toMutableList()

    // Now convert everything to ASCII digits.
    var i = 0
    while (i < res.size) {
        val r = res[i]
        check(r.toUInt() < radix)
        res[i] = if (r.toUInt() < 10u) {
            (r.toInt() + '0'.code).toUByte()
        } else {
            (r.toInt() + 'a'.code - 10).toUByte()
        }
        i += 1
    }
    return res
}

/**
 * Returns the greatest power of the radix for the `BigDigit` bit size.
 */
private fun getRadixBase(radix: UInt): Pair<BigDigit, Int> {
    check(!radix.isPowerOfTwo())
    check(radix in 3u..255u)
    return generateRadixBase(radix, BIG_DIGIT_MAX)
}

/**
 * Returns the greatest power of the radix for half the `BigDigit` bit size.
 */
private fun getHalfRadixBase(radix: UInt): Pair<BigDigit, Int> {
    check(!radix.isPowerOfTwo())
    check(radix in 3u..255u)
    return generateRadixBase(radix, BIG_DIGIT_HALF)
}

/**
 * Generate tables of the greatest power of each radix that is less that the given maximum. These
 * are returned from `getRadixBase` to batch the multiplication and division of radix conversions on
 * full `BigUint` values, operating on primitive integers as much as possible.
 *
 * Example: `BASES_16[3] = (59049, 10)` because `3^10` fits in `UInt`, but `3^11` is too big.
 *
 * Powers of two are not included, just zeroed, as they're implemented with shifts.
 */
private fun generateRadixBase(radix: UInt, max: BigDigit): Pair<BigDigit, Int> {
    var power = 1
    var base = radix
    while (true) {
        val next = base.toULong() * radix.toULong()
        if (next > max.toULong()) {
            break
        }
        base = next.toUInt()
        power += 1
    }
    return Pair(base, power)
}

private fun UInt.isPowerOfTwo(): Boolean {
    return this != 0u && (this and (this - 1u)) == 0u
}

private const val DOUBLE_MAX_EXPONENT_VALUE: Int = 1024

private fun divCeil(a: ULong, b: ULong): ULong {
    val d = a / b
    val m = a % b
    return if (m == 0uL) d else d + 1uL
}

fun ULong.sqrt(): ULong {
    return nthRoot(2u)
}

fun ULong.cbrt(): ULong {
    return nthRoot(3u)
}

fun ULong.nthRoot(n: UInt): ULong {
    require(n > 0u) { "root degree n must be at least 1" }
    if (this <= 1uL || n == 1u) {
        return this
    }
    var low = 1uL
    var high = this
    var answer = 1uL
    while (low <= high) {
        val mid = low + (high - low) / 2uL
        val cmp = comparePow(mid, n, this)
        if (cmp <= 0) {
            answer = mid
            low = mid + 1uL
        } else {
            high = mid - 1uL
        }
    }
    return answer
}

private fun comparePow(base: ULong, exp: UInt, limit: ULong): Int {
    var acc = 1uL
    repeat(exp.toInt()) {
        if (base != 0uL && acc > limit / base) {
            return 1
        }
        acc *= base
    }
    return acc.compareTo(limit)
}
