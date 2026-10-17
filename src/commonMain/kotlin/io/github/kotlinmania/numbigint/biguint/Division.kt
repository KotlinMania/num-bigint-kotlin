// port-lint: source biguint/division.rs
package io.github.kotlinmania.numbigint

internal const val FAST_DIV_WIDE: Boolean = true

/**
 * Divide a two digit numerator by a one digit divisor, returns quotient and remainder.
 *
 * The caller must ensure that both the quotient and remainder will fit into a single digit.
 * This is not true for an arbitrary numerator or denominator.
 *
 * This function also matches what the x86 divide instruction does.
 */
private fun divWide(hi: BigDigit, lo: BigDigit, divisor: BigDigit): Pair<BigDigit, BigDigit> {
    check(hi < divisor)

    val lhs = toDoubleBigDigit(hi, lo)
    val rhs = divisor.toULong()
    return Pair((lhs / rhs).toUInt(), (lhs % rhs).toUInt())
}

/**
 * For small divisors, we can divide without promoting to `DoubleBigDigit` by
 * using half-size pieces of digit, like long-division.
 */
private fun divHalf(rem: BigDigit, digit: BigDigit, divisor: BigDigit): Pair<BigDigit, BigDigit> {
    check(rem < divisor && divisor <= BIG_DIGIT_HALF)
    val hiDividend = (rem shl BIG_DIGIT_HALF_BITS) or (digit shr BIG_DIGIT_HALF_BITS)
    val hi = hiDividend / divisor
    var nextRem = hiDividend % divisor
    val loDividend = (nextRem shl BIG_DIGIT_HALF_BITS) or (digit and BIG_DIGIT_HALF)
    val lo = loDividend / divisor
    nextRem = loDividend % divisor
    return Pair((hi shl BIG_DIGIT_HALF_BITS) or lo, nextRem)
}

internal fun divRemDigit(a: BigUint, b: BigDigit): Pair<BigUint, BigDigit> {
    require(b != 0u) { "attempt to divide by zero" }

    var rem = 0u
    var i = a.data.lastIndex
    while (i >= 0) {
        val (q, r) =
            if (!FAST_DIV_WIDE && b <= BIG_DIGIT_HALF) {
                divHalf(rem, a.data[i], b)
            } else {
                divWide(rem, a.data[i], b)
            }
        a.data[i] = q
        rem = r
        i -= 1
    }

    return Pair(a.normalized(), rem)
}

private fun remDigit(a: BigUint, b: BigDigit): BigDigit {
    require(b != 0u) { "attempt to divide by zero" }

    var rem = 0u
    var i = a.data.lastIndex
    while (i >= 0) {
        val (_, r) =
            if (!FAST_DIV_WIDE && b <= BIG_DIGIT_HALF) {
                divHalf(rem, a.data[i], b)
            } else {
                divWide(rem, a.data[i], b)
            }
        rem = r
        i -= 1
    }

    return rem
}

/**
 * Subtract a multiple.
 * `a -= b * c`
 * Returns a borrow if a is less than b.
 */
internal fun subMulDigitSameLen(
    a: MutableList<BigDigit>,
    b: List<BigDigit>,
    c: BigDigit,
    aOffset: Int = 0,
): BigDigit {
    check(a.size - aOffset == b.size)

    // carry is between -BIG_DIGIT_MAX and 0, so to avoid overflow we store
    // offsetCarry = carry + BIG_DIGIT_MAX
    var offsetCarry = BIG_DIGIT_MAX

    var i = 0
    while (i < b.size) {
        // We want to calculate sum = x - y * c + carry.
        // sum >= -(BIG_DIGIT_MAX * BIG_DIGIT_MAX) - BIG_DIGIT_MAX
        // sum <= BIG_DIGIT_MAX
        // Offsetting sum by (BIG_DIGIT_MAX << BIG_DIGIT_BITS) puts it in DoubleBigDigit range.
        val offsetSum =
            toDoubleBigDigit(BIG_DIGIT_MAX, a[aOffset + i]) -
                BIG_DIGIT_MAX.toULong() +
                offsetCarry.toULong() -
                b[i].toULong() * c.toULong()

        val (newOffsetCarry, newX) = fromDoubleBigDigit(offsetSum)
        offsetCarry = newOffsetCarry
        a[aOffset + i] = newX
        i += 1
    }

    // Return the borrow.
    return BIG_DIGIT_MAX - offsetCarry
}

private fun divRem(u: BigUint, d: BigUint): Pair<BigUint, BigUint> = divRemRef(u, d)

internal fun divRemRef(u: BigUint, d: BigUint): Pair<BigUint, BigUint> {
    require(!d.isZero()) { "attempt to divide by zero" }
    if (u.isZero()) {
        return Pair(BigUint.ZERO, BigUint.ZERO)
    }

    if (d.data.size == 1) {
        if (d.data[0] == 1u) {
            return Pair(u.clone(), BigUint.ZERO)
        }

        val (div, rem) = divRemDigit(u.clone(), d.data[0])
        return Pair(div, rem.toBigUint())
    }

    when {
        u < d -> return Pair(BigUint.ZERO, u.clone())
        u == d -> return Pair(BigUint.one(), BigUint.ZERO)
    }

    // This algorithm is from Knuth, TAOCP vol 2 section 4.3, algorithm D:
    //
    // First, normalize the arguments so the highest bit in the highest digit of the divisor is
    // set: the main loop uses the highest digit of the divisor for generating guesses, so we
    // want it to be the largest number we can efficiently divide by.
    //
    val shift = d.data.last().countLeadingZeroBits()

    return if (shift == 0) {
        // no need to clone d
        divRemCore(u.clone(), d.data)
    } else {
        val (q, r) = divRemCore(u.shiftLeft(shift.toULong()), (d.shiftLeft(shift.toULong())).data)
        // renormalize the remainder
        Pair(q, r.shiftRight(shift.toULong()))
    }
}

/**
 * An implementation of the base division algorithm.
 * Knuth, TAOCP vol 2 section 4.3.1, algorithm D, with an improvement from exercises 19-21.
 */
private fun divRemCore(a: BigUint, b: List<BigDigit>): Pair<BigUint, BigUint> {
    check(a.data.size >= b.size && b.size > 1)
    check(b.last().countLeadingZeroBits() == 0)

    // a0 stores an additional extra most significant digit of the dividend, not stored in a.
    var a0: BigDigit = 0u

    // [b1, b0] are the two most significant digits of the divisor. They never change.
    val b0 = b[b.size - 1]
    val b1 = b[b.size - 2]

    val qLen = a.data.size - b.size + 1
    val q = BigUint(MutableList(qLen) { 0u })

    for (j in (0 until qLen).reversed()) {
        check(a.data.size == b.size + j)

        val a1 = a.data.last()
        val a2 = a.data[a.data.size - 2]

        // The first q0 estimate is [a1,a0] / b0. It will never be too small, it may be too large
        // by at most 2.
        var q0: BigDigit
        var r: DoubleBigDigit
        if (a0 < b0) {
            val (qEst, rEst) = divWide(a0, a1, b0)
            q0 = qEst
            r = rEst.toULong()
        } else {
            check(a0 == b0)
            // Avoid overflowing q0, we know the quotient fits in BigDigit.
            // [a1,a0] = b0 * (1<<BITS - 1) + (a0 + a1)
            q0 = BIG_DIGIT_MAX
            r = a0.toULong() + a1.toULong()
        }

        // r = [a1,a0] - q0 * b0
        //
        // Now we want to compute a more precise estimate [a2,a1,a0] / [b1,b0] which can only be
        // less or equal to the current q0.
        //
        // q0 is too large if:
        // [a2,a1,a0] < q0 * [b1,b0]
        // (r << BITS) + a2 < q0 * b1
        while (r <= BIG_DIGIT_MAX.toULong() &&
            toDoubleBigDigit(r.toUInt(), a2) < q0.toULong() * b1.toULong()
        ) {
            q0 -= 1u
            r += b0.toULong()
        }

        // q0 is now either the correct quotient digit, or in rare cases 1 too large.
        // Subtract (q0 << j) from a. This may overflow, in which case we will have to correct.

        var borrow = subMulDigitSameLen(a.data, b, q0, aOffset = j)
        if (borrow > a0) {
            // q0 is too large. We need to add back one multiple of b.
            q0 -= 1u
            borrow -= add2Carry(a.data, b, aOffset = j)
        }
        // The top digit of a, stored in a0, has now been zeroed.
        check(borrow == a0)

        q.data[j] = q0

        // Pop off the next top digit of a.
        a0 = a.data.removeAt(a.data.lastIndex)
    }

    a.data.add(a0)
    a.normalize()

    check(cmpSlice(a.data, b) < 0)

    return Pair(q.normalized(), a)
}

operator fun BigUint.div(other: BigUint): BigUint {
    val (q, _) = divRem(this, other)
    return q
}

operator fun BigUint.divAssign(other: BigUint) {
    val next = this / other
    data.clear()
    data.addAll(next.data)
}

operator fun BigUint.div(other: UInt): BigUint {
    val (q, _) = divRemDigit(clone(), other)
    return q
}

operator fun BigUint.divAssign(other: UInt) {
    val next = this / other
    data.clear()
    data.addAll(next.data)
}

operator fun UInt.div(other: BigUint): BigUint =
    when (other.data.size) {
        0 -> throw ArithmeticException("attempt to divide by zero")
        1 -> (this / other.data[0]).toBigUint()
        else -> BigUint.ZERO
    }

operator fun BigUint.div(other: ULong): BigUint {
    val (q, _) = divRem(this, other.toBigUint())
    return q
}

operator fun BigUint.divAssign(other: ULong) {
    val next = this / other
    data.clear()
    data.addAll(next.data)
}

operator fun ULong.div(other: BigUint): BigUint =
    when (other.data.size) {
        0 -> throw ArithmeticException("attempt to divide by zero")
        1 -> (this / other.data[0]).toBigUint()
        2 -> (this / toDoubleBigDigit(other.data[1], other.data[0])).toBigUint()
        else -> BigUint.ZERO
    }

operator fun BigUint.rem(other: BigUint): BigUint {
    val otherU32 = other.toUIntOrNull()
    return if (otherU32 != null) {
        this % otherU32
    } else {
        val (_, r) = divRem(this, other)
        r
    }
}

operator fun BigUint.remAssign(other: BigUint) {
    val next = this % other
    data.clear()
    data.addAll(next.data)
}

operator fun BigUint.rem(other: UInt): BigUint = remDigit(this, other).toBigUint()

operator fun BigUint.remAssign(other: UInt) {
    val next = this % other
    data.clear()
    data.addAll(next.data)
}

operator fun UInt.rem(other: BigUint): BigUint =
    when (val primitive = other.toUIntOrNull()) {
        null -> this.toBigUint()
        0u -> throw ArithmeticException("attempt to divide by zero")
        else -> (this % primitive).toBigUint()
    }

operator fun BigUint.rem(other: ULong): BigUint {
    val (_, r) = divRem(this, other.toBigUint())
    return r
}

operator fun BigUint.remAssign(other: ULong) {
    val next = this % other
    data.clear()
    data.addAll(next.data)
}

operator fun ULong.rem(other: BigUint): BigUint =
    when (val primitive = other.toULongOrNull()) {
        null -> this.toBigUint()
        0uL -> throw ArithmeticException("attempt to divide by zero")
        else -> (this % primitive).toBigUint()
    }

operator fun BigUint.div(other: UByte): BigUint = this / other.toUInt()

operator fun UByte.div(other: BigUint): BigUint = toUInt() / other

operator fun BigUint.divAssign(other: UByte) {
    this /= other.toUInt()
}

operator fun BigUint.div(other: UShort): BigUint = this / other.toUInt()

operator fun UShort.div(other: BigUint): BigUint = toUInt() / other

operator fun BigUint.divAssign(other: UShort) {
    this /= other.toUInt()
}

operator fun BigUint.rem(other: UByte): BigUint = this % other.toUInt()

operator fun UByte.rem(other: BigUint): BigUint = toUInt() % other

operator fun BigUint.remAssign(other: UByte) {
    this %= other.toUInt()
}

operator fun BigUint.rem(other: UShort): BigUint = this % other.toUInt()

operator fun UShort.rem(other: BigUint): BigUint = toUInt() % other

operator fun BigUint.remAssign(other: UShort) {
    this %= other.toUInt()
}

fun BigUint.checkedDiv(v: BigUint): BigUint? {
    if (v.isZero()) {
        return null
    }
    return this / v
}

fun BigUint.checkedDivEuclid(v: BigUint): BigUint? {
    if (v.isZero()) {
        return null
    }
    return divEuclid(v)
}

fun BigUint.checkedRemEuclid(v: BigUint): BigUint? {
    if (v.isZero()) {
        return null
    }
    return remEuclid(v)
}

fun BigUint.checkedDivRemEuclid(v: BigUint): Pair<BigUint, BigUint>? {
    if (v.isZero()) {
        return null
    }
    return divRemEuclid(v)
}

fun BigUint.divEuclid(v: BigUint): BigUint {
    // trivially same as regular division
    return this / v
}

fun BigUint.remEuclid(v: BigUint): BigUint {
    // trivially same as regular remainder
    return this % v
}

fun BigUint.divRemEuclid(v: BigUint): Pair<BigUint, BigUint> {
    // trivially same as regular division and remainder
    return divRem(v)
}
