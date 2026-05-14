// port-lint: source biguint/multiplication.rs
package io.github.kotlinmania.numbigint

internal fun macWithCarry(
    a: BigDigit,
    b: BigDigit,
    c: BigDigit,
    acc: ULong,
): Pair<BigDigit, ULong> {
    var nextAcc = acc
    nextAcc += a.toULong()
    nextAcc += b.toULong() * c.toULong()
    val lo = nextAcc.toUInt()
    nextAcc = nextAcc shr BIG_DIGIT_BITS
    return Pair(lo, nextAcc)
}

private fun mulWithCarry(a: BigDigit, b: BigDigit, acc: ULong): Pair<BigDigit, ULong> {
    var nextAcc = acc
    nextAcc += a.toULong() * b.toULong()
    val lo = nextAcc.toUInt()
    nextAcc = nextAcc shr BIG_DIGIT_BITS
    return Pair(lo, nextAcc)
}

/**
 * Three argument multiply accumulate:
 * `acc += b * c`
 */
private fun macDigit(acc: MutableList<BigDigit>, offset: Int, b: List<BigDigit>, c: BigDigit) {
    if (c == 0u) {
        return
    }

    var carry = 0uL
    var i = 0
    while (i < b.size) {
        val (out, nextCarry) = macWithCarry(acc[offset + i], b[i], c, carry)
        acc[offset + i] = out
        carry = nextCarry
        i += 1
    }

    val (carryHi, carryLo) = fromDoubleBigDigit(carry)
    val finalCarry = if (carryHi == 0u) {
        add2Carry(acc, listOf(carryLo), aOffset = offset + b.size)
    } else {
        add2Carry(acc, listOf(carryLo, carryHi), aOffset = offset + b.size)
    }
    check(finalCarry == 0u) { "carry overflow during multiplication!" }
}

/**
 * Three argument multiply accumulate:
 * `acc += b * c`
 */
private fun mac3(acc: MutableList<BigDigit>, b: List<BigDigit>, c: List<BigDigit>) {
    var bStart = 0
    while (bStart < b.size && b[bStart] == 0u) {
        bStart += 1
    }
    if (bStart == b.size) {
        return
    }
    var cStart = 0
    while (cStart < c.size && c[cStart] == 0u) {
        cStart += 1
    }
    if (cStart == c.size) {
        return
    }

    val x = if (b.size - bStart < c.size - cStart) b.subList(bStart, b.size) else c.subList(cStart, c.size)
    val y = if (b.size - bStart < c.size - cStart) c.subList(cStart, c.size) else b.subList(bStart, b.size)
    val accOffset = bStart + cStart

    // Long multiplication:
    var i = 0
    while (i < x.size) {
        macDigit(acc, accOffset + i, y, x[i])
        i += 1
    }
}

private fun mul3(x: List<BigDigit>, y: List<BigDigit>): BigUint {
    val len = x.size + y.size + 1
    val prod = BigUint(MutableList(len) { 0u })

    mac3(prod.data, x, y)
    return prod.normalized()
}

private fun scalarMul(a: BigUint, b: BigDigit) {
    when {
        b == 0u -> a.setZero()
        b == 1u -> Unit
        b.countOneBits() == 1 -> a.shiftLeftAssign(b.countTrailingZeroBits().toULong())
        else -> {
            var carry = 0uL
            var i = 0
            while (i < a.data.size) {
                val (out, nextCarry) = mulWithCarry(a.data[i], b, carry)
                a.data[i] = out
                carry = nextCarry
                i += 1
            }
            if (carry != 0uL) {
                a.data.add(carry.toUInt())
            }
        }
    }
}

operator fun BigUint.times(other: BigUint): BigUint {
    return when {
        data.isEmpty() || other.data.isEmpty() -> BigUint.ZERO
        other.data.size == 1 -> this * other.data[0]
        data.size == 1 -> other * data[0]
        else -> mul3(data, other.data)
    }
}

operator fun BigUint.timesAssign(other: BigUint) {
    when {
        data.isEmpty() -> Unit
        other.data.isEmpty() -> setZero()
        other.data.size == 1 -> timesAssign(other.data[0])
        data.size == 1 -> {
            val next = other * data[0]
            data.clear()
            data.addAll(next.data)
        }
        else -> {
            val next = mul3(data, other.data)
            data.clear()
            data.addAll(next.data)
        }
    }
}

operator fun BigUint.times(other: UInt): BigUint {
    val result = clone()
    result *= other
    return result
}

operator fun UInt.times(other: BigUint): BigUint {
    return other * this
}

operator fun BigUint.timesAssign(other: UInt) {
    scalarMul(this, other)
}

operator fun BigUint.times(other: ULong): BigUint {
    val result = clone()
    result *= other
    return result
}

operator fun ULong.times(other: BigUint): BigUint {
    return other * this
}

operator fun BigUint.timesAssign(other: ULong) {
    if (other <= UInt.MAX_VALUE.toULong()) {
        scalarMul(this, other.toUInt())
    } else {
        val (hi, lo) = fromDoubleBigDigit(other)
        val next = mul3(data, listOf(lo, hi))
        data.clear()
        data.addAll(next.data)
    }
}

fun BigUint.checkedMul(v: BigUint): BigUint? {
    return this * v
}

fun Iterable<BigUint>.productBigUint(): BigUint {
    var product = BigUint.one()
    for (value in this) {
        product.timesAssign(value)
    }
    return product
}
