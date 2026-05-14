// port-lint: source biguint/subtraction.rs
package io.github.kotlinmania.numbigint

// Subtract with borrow.
private fun sbb(borrow: BigDigit, lhs: BigDigit, rhs: BigDigit): Pair<BigDigit, BigDigit> {
    val subtrahend = rhs.toULong() + borrow.toULong()
    val minuend = lhs.toULong()
    val out = (minuend - subtrahend).toUInt()
    val nextBorrow = if (minuend < subtrahend) 1u else 0u
    return Pair(out, nextBorrow)
}

internal fun sub2(a: MutableList<BigDigit>, b: List<BigDigit>) {
    var borrow = 0u

    val len = minOf(a.size, b.size)
    var i = 0
    while (i < len) {
        val (out, nextBorrow) = sbb(borrow, a[i], b[i])
        a[i] = out
        borrow = nextBorrow
        i += 1
    }

    while (borrow != 0u && i < a.size) {
        val (out, nextBorrow) = sbb(borrow, a[i], 0u)
        a[i] = out
        borrow = nextBorrow
        i += 1
    }

    // note: we're required to fail on underflow
    require(borrow == 0u && b.drop(len).all { it == 0u }) {
        "Cannot subtract b from a because b is larger than a."
    }
}

// Only for the subtraction implementation. `a` and `b` must have same length.
private fun sub2Rev(a: List<BigDigit>, b: MutableList<BigDigit>, len: Int = a.size): BigDigit {
    check(b.size >= len)

    var borrow = 0u
    var i = 0
    while (i < len) {
        val (out, nextBorrow) = sbb(borrow, a[i], b[i])
        b[i] = out
        borrow = nextBorrow
        i += 1
    }
    return borrow
}

private fun sub2RevFull(a: List<BigDigit>, b: MutableList<BigDigit>) {
    check(b.size >= a.size)
    val borrow = sub2Rev(a, b, minOf(a.size, b.size))

    // note: we're required to fail on underflow
    require(borrow == 0u && b.drop(a.size).all { it == 0u }) {
        "Cannot subtract b from a because b is larger than a."
    }
}

operator fun BigUint.minus(other: BigUint): BigUint {
    val result = clone()
    result -= other
    return result
}

operator fun BigUint.minusAssign(other: BigUint) {
    sub2(data, other.data)
    normalize()
}

fun subtractFromBigUint(lhs: BigUint, rhs: BigUint): BigUint {
    val other = rhs.clone()
    val otherLen = other.data.size
    if (otherLen < lhs.data.size) {
        val loBorrow = sub2Rev(lhs.data.subList(0, otherLen), other.data, otherLen)
        other.data.addAll(lhs.data.subList(otherLen, lhs.data.size))
        if (loBorrow != 0u) {
            sub2(other.data.subList(otherLen, other.data.size), listOf(1u))
        }
    } else {
        sub2RevFull(lhs.data, other.data)
    }
    return other.normalized()
}

operator fun BigUint.minus(other: UInt): BigUint {
    val result = clone()
    result -= other
    return result
}

operator fun BigUint.minusAssign(other: UInt) {
    sub2(data, listOf(other))
    normalize()
}

operator fun UInt.minus(other: BigUint): BigUint {
    val result = other.clone()
    if (result.data.isEmpty()) {
        result.data.add(this)
    } else {
        sub2RevFull(listOf(this), result.data)
    }
    return result.normalized()
}

operator fun BigUint.minus(other: ULong): BigUint {
    val result = clone()
    result -= other
    return result
}

operator fun BigUint.minusAssign(other: ULong) {
    val (hi, lo) = fromDoubleBigDigit(other)
    sub2(data, listOf(lo, hi))
    normalize()
}

operator fun ULong.minus(other: BigUint): BigUint {
    val result = other.clone()
    while (result.data.size < 2) {
        result.data.add(0u)
    }
    val (hi, lo) = fromDoubleBigDigit(this)
    sub2RevFull(listOf(lo, hi), result.data)
    return result.normalized()
}

fun BigUint.checkedSub(v: BigUint): BigUint? {
    return when {
        this < v -> null
        this == v -> BigUint.ZERO
        else -> this - v
    }
}
