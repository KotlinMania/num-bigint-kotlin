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
        val (q, r) = if (!FAST_DIV_WIDE && b <= BIG_DIGIT_HALF) {
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
        val (_, r) = if (!FAST_DIV_WIDE && b <= BIG_DIGIT_HALF) {
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
private fun subMulDigitSameLen(a: MutableList<BigDigit>, b: List<BigDigit>, c: BigDigit): BigDigit {
    check(a.size == b.size)

    // carry is between -BIG_DIGIT_MAX and 0, so to avoid overflow we store
    // offsetCarry = carry + BIG_DIGIT_MAX
    var offsetCarry = BIG_DIGIT_MAX

    var i = 0
    while (i < a.size) {
        // We want to calculate sum = x - y * c + carry.
        // sum >= -(BIG_DIGIT_MAX * BIG_DIGIT_MAX) - BIG_DIGIT_MAX
        // sum <= BIG_DIGIT_MAX
        // Offsetting sum by (BIG_DIGIT_MAX << BIG_DIGIT_BITS) puts it in DoubleBigDigit range.
        val offsetSum =
            toDoubleBigDigit(BIG_DIGIT_MAX, a[i]) -
                BIG_DIGIT_MAX.toULong() +
                offsetCarry.toULong() -
                b[i].toULong() * c.toULong()

        val (newOffsetCarry, newX) = fromDoubleBigDigit(offsetSum)
        offsetCarry = newOffsetCarry
        a[i] = newX
        i += 1
    }

    // Return the borrow.
    return BIG_DIGIT_MAX - offsetCarry
}

private fun divRem(u: BigUint, d: BigUint): Pair<BigUint, BigUint> {
    return divRemRef(u, d)
}

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

    return when {
        u < d -> Pair(BigUint.ZERO, u.clone())
        u == d -> Pair(BigUint.one(), BigUint.ZERO)
        else -> divRemCore(u.clone(), d)
    }
}

/**
 * An implementation of the base division algorithm.
 * Knuth, TAOCP vol 2 section 4.3.1, algorithm D, with an improvement from exercises 19-21.
 */
private fun divRemCore(a: BigUint, b: BigUint): Pair<BigUint, BigUint> {
    val q = BigUint.zero()
    var r = BigUint.zero()
    var bit = a.bits()
    while (bit > 0uL) {
        bit -= 1uL
        r = r.shiftLeft(1uL)
        if (a.bit(bit)) {
            r.setBit(0uL, true)
        }
        if (r >= b) {
            r.minusAssign(b)
            q.setBit(bit, true)
        }
    }
    return Pair(q.normalized(), r.normalized())
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

operator fun UInt.div(other: BigUint): BigUint {
    return when (other.data.size) {
        0 -> throw ArithmeticException("attempt to divide by zero")
        1 -> (this / other.data[0]).toBigUint()
        else -> BigUint.ZERO
    }
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

operator fun ULong.div(other: BigUint): BigUint {
    return when (other.data.size) {
        0 -> throw ArithmeticException("attempt to divide by zero")
        1 -> (this / other.data[0]).toBigUint()
        2 -> (this / toDoubleBigDigit(other.data[1], other.data[0])).toBigUint()
        else -> BigUint.ZERO
    }
}

operator fun BigUint.rem(other: BigUint): BigUint {
    return if (other.data.size == 1) {
        remDigit(this, other.data[0]).toBigUint()
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

operator fun BigUint.rem(other: UInt): BigUint {
    return remDigit(this, other).toBigUint()
}

operator fun BigUint.remAssign(other: UInt) {
    val next = this % other
    data.clear()
    data.addAll(next.data)
}

operator fun UInt.rem(other: BigUint): BigUint {
    return when (val primitive = other.toUIntOrNull()) {
        null -> this.toBigUint()
        0u -> throw ArithmeticException("attempt to divide by zero")
        else -> (this % primitive).toBigUint()
    }
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

operator fun ULong.rem(other: BigUint): BigUint {
    return when (val primitive = other.toULongOrNull()) {
        null -> this.toBigUint()
        0uL -> throw ArithmeticException("attempt to divide by zero")
        else -> (this % primitive).toBigUint()
    }
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
