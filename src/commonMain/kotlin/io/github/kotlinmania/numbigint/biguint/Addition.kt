// port-lint: source biguint/addition.rs
package io.github.kotlinmania.numbigint

// Add with carry.
private fun adc(carry: BigDigit, lhs: BigDigit, rhs: BigDigit): Pair<BigDigit, BigDigit> {
    val sum = lhs.toULong() + rhs.toULong() + carry.toULong()
    return Pair(sum.toUInt(), (sum shr BIG_DIGIT_BITS).toUInt())
}

/**
 * Two argument addition of raw slices, `a += b`, returning the carry.
 *
 * This is used when the data list might need to resize to push a non-zero carry, so we perform
 * the addition first hoping that it will fit.
 *
 * The caller must ensure that `a` is at least as long as `b`.
 */
internal fun add2Carry(
    a: MutableList<BigDigit>,
    b: List<BigDigit>,
    aOffset: Int = 0,
    bOffset: Int = 0,
    len: Int = b.size - bOffset,
): BigDigit {
    check(a.size - aOffset >= len)

    var carry = 0u
    var i = 0
    while (i < len) {
        val (out, nextCarry) = adc(carry, a[aOffset + i], b[bOffset + i])
        a[aOffset + i] = out
        carry = nextCarry
        i += 1
    }

    var j = aOffset + len
    while (carry != 0u && j < a.size) {
        val (out, nextCarry) = adc(carry, a[j], 0u)
        a[j] = out
        carry = nextCarry
        j += 1
    }

    return carry
}

/**
 * Two argument addition of raw slices:
 * `a += b`
 *
 * The caller must ensure that a is big enough to store the result, typically by resizing a to
 * `max(a.len(), b.len()) + 1`, to fit a possible carry.
 */
internal fun add2(a: MutableList<BigDigit>, b: List<BigDigit>) {
    val carry = add2Carry(a, b)
    check(carry == 0u)
}

operator fun BigUint.plus(other: BigUint): BigUint {
    val result = clone()
    result += other
    return result
}

operator fun BigUint.plusAssign(other: BigUint) {
    val selfLen = data.size
    val carry = if (selfLen < other.data.size) {
        val loCarry = add2Carry(data, other.data, len = selfLen)
        data.addAll(other.data.subList(selfLen, other.data.size))
        add2Carry(data, listOf(loCarry), aOffset = selfLen)
    } else {
        add2Carry(data, other.data)
    }
    if (carry != 0u) {
        data.add(carry)
    }
}

operator fun BigUint.plus(other: UInt): BigUint {
    val result = clone()
    result += other
    return result
}

operator fun UInt.plus(other: BigUint): BigUint {
    return other + this
}

operator fun BigUint.plusAssign(other: UInt) {
    if (other != 0u) {
        if (data.isEmpty()) {
            data.add(0u)
        }

        val carry = add2Carry(data, listOf(other))
        if (carry != 0u) {
            data.add(carry)
        }
    }
}

operator fun BigUint.plus(other: ULong): BigUint {
    val result = clone()
    result += other
    return result
}

operator fun ULong.plus(other: BigUint): BigUint {
    return other + this
}

operator fun BigUint.plusAssign(other: ULong) {
    val (hi, lo) = fromDoubleBigDigit(other)
    if (hi == 0u) {
        this += lo
    } else {
        while (data.size < 2) {
            data.add(0u)
        }

        val carry = add2Carry(data, listOf(lo, hi))
        if (carry != 0u) {
            data.add(carry)
        }
    }
}

fun BigUint.checkedAdd(v: BigUint): BigUint? {
    return this + v
}

fun Iterable<BigUint>.sumBigUint(): BigUint {
    var sum = BigUint.zero()
    for (value in this) {
        sum.plusAssign(value)
    }
    return sum
}
