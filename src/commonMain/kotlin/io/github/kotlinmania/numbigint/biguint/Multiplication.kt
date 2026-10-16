// port-lint: source biguint/multiplication.rs
package io.github.kotlinmania.numbigint

import io.github.kotlinmania.numbigint.bigint.minus
import io.github.kotlinmania.numbigint.bigint.minusAssign
import io.github.kotlinmania.numbigint.bigint.plus
import io.github.kotlinmania.numbigint.bigint.plusAssign
import io.github.kotlinmania.numbigint.bigint.times

internal fun macWithCarry(
    a: BigDigit,
    b: BigDigit,
    c: BigDigit,
    acc: DoubleBigDigit,
): DoubleBigDigit = acc + a.toULong() + b.toULong() * c.toULong()

private fun mulWithCarry(a: BigDigit, b: BigDigit, acc: DoubleBigDigit): DoubleBigDigit =
    acc + a.toULong() * b.toULong()

/**
 * Three argument multiply accumulate:
 * `acc += b * c`
 */
private fun macDigit(acc: MutableList<BigDigit>, b: List<BigDigit>, c: BigDigit) {
    if (c == 0u) {
        return
    }

    var carry = 0uL
    var i = 0
    while (i < b.size) {
        val nextAcc = macWithCarry(acc[i], b[i], c, carry)
        acc[i] = nextAcc.toUInt()
        carry = nextAcc shr BIG_DIGIT_BITS
        i += 1
    }

    val (carryHi, carryLo) = fromDoubleBigDigit(carry)
    val finalCarry =
        if (carryHi == 0u) {
            add2Carry(acc, listOf(carryLo), aOffset = b.size)
        } else {
            add2Carry(acc, listOf(carryHi, carryLo), aOffset = b.size)
        }
    check(finalCarry == 0u) { "carry overflow during multiplication!" }
}

internal fun bigintFromSlice(slice: List<BigDigit>): BigInt =
    BigInt.from(biguintFromVec(slice.toMutableList()))

/**
 * Three argument multiply accumulate:
 * `acc += b * c`
 */
private fun mac3(acc0: MutableList<BigDigit>, b0: List<BigDigit>, c0: List<BigDigit>) {
    var accOffset = 0
    var b = b0
    var c = c0

    // Least-significant zeros have no effect on the output.
    if (b.firstOrNull() == 0u) {
        val nz = b.indexOfFirst { it != 0u }
        if (nz >= 0) {
            b = b.subList(nz, b.size)
            accOffset += nz
        } else {
            return
        }
    }
    if (c.firstOrNull() == 0u) {
        val nz = c.indexOfFirst { it != 0u }
        if (nz >= 0) {
            c = c.subList(nz, c.size)
            accOffset += nz
        } else {
            return
        }
    }

    val acc = if (accOffset > 0) acc0.subList(accOffset, acc0.size) else acc0
    val (x, y) = if (b.size < c.size) Pair(b, c) else Pair(c, b)

    // We use four algorithms for different input sizes.
    //
    // - For small inputs, long multiplication is fastest.
    // - If y is at least twice as long as x, split using Half-Karatsuba.
    // - Next we use Karatsuba multiplication (Toom-2), which we have optimized
    //   to avoid unnecessary allocations for intermediate values.
    // - For the largest inputs we use Toom-3, which better optimizes the
    //   number of operations, but uses more temporary allocations.
    //
    // The thresholds are somewhat arbitrary, chosen by evaluating the results
    // of `cargo bench --bench bigint multiply`.

    if (x.size <= 32) {
        // Long multiplication:
        for ((i, xi) in x.withIndex()) {
            macDigit(acc.subList(i, acc.size), y, xi)
        }
    } else if (x.size * 2 <= y.size) {
        // Karatsuba Multiplication for factors with significant length disparity.
        //
        // The Half-Karatsuba Multiplication Algorithm is a specialized case of
        // the normal Karatsuba multiplication algorithm, designed for the scenario
        // where y has at least twice as many base digits as x.
        //
        // In this case y (the longer input) is split into high2 and low2,
        // at m2 (half the length of y) and x (the shorter input),
        // is used directly without splitting.
        //
        // The algorithm then proceeds as follows:
        //
        // 1. Compute the product z0 = x * low2.
        // 2. Compute the product temp = x * high2.
        // 3. Adjust the weight of temp by adding m2 (* NBASE ^ m2)
        // 4. Add temp and z0 to obtain the final result.
        val m2 = y.size / 2
        val low2 = y.subList(0, m2)
        val high2 = y.subList(m2, y.size)

        // (x * high2) * NBASE ^ m2 + z0
        mac3(acc, x, low2)
        mac3(acc.subList(m2, acc.size), x, high2)
    } else if (x.size <= 256) {
        // Karatsuba multiplication:
        //
        // When x is smaller than y, it's significantly faster to pick b such that x is split in
        // half, not y:
        val bLen = x.size / 2
        val x0 = x.subList(0, bLen)
        val x1 = x.subList(bLen, x.size)
        val y0 = y.subList(0, bLen)
        val y1 = y.subList(bLen, y.size)

        // We reuse the same BigUint for all the intermediate multiplies and have to size p
        // appropriately here: x1.len() >= x0.len and y1.len() >= y0.len():
        val len = x1.size + y1.size + 1
        val p = BigUint(MutableList(len) { 0u })

        // p2 = x1 * y1
        mac3(p.data, x1, y1)

        // Not required, but the adds go faster if we drop any unneeded 0s from the end:
        p.normalize()

        add2(acc.subList(bLen, acc.size), p.data)
        add2(acc.subList(bLen * 2, acc.size), p.data)

        // Zero out p before the next multiply:
        p.data.clear()
        repeat(len) { p.data.add(0u) }

        // p0 = x0 * y0
        mac3(p.data, x0, y0)
        p.normalize()

        add2(acc, p.data)
        add2(acc.subList(bLen, acc.size), p.data)

        // p1 = (x1 - x0) * (y1 - y0)
        // We do this one last, since it may be negative and acc can't ever be negative:
        val (j0Sign, j0) = subSign(x1, x0)
        val (j1Sign, j1) = subSign(y1, y0)

        when (j0Sign * j1Sign) {
            Sign.Plus -> {
                p.data.clear()
                repeat(len) { p.data.add(0u) }

                mac3(p.data, j0.data, j1.data)
                p.normalize()

                sub2(acc.subList(bLen, acc.size), p.data)
            }

            Sign.Minus -> {
                mac3(acc.subList(bLen, acc.size), j0.data, j1.data)
            }

            Sign.NoSign -> {}
        }
    } else {
        // Toom-3 multiplication:
        //
        // Toom-3 is like Karatsuba above, but dividing the inputs into three parts.
        // Both are instances of Toom-Cook, using `k=3` and `k=2` respectively.
        //
        // The general idea is to treat the large integers digits as
        // polynomials of a certain degree and determine the coefficients/digits
        // of the product of the two via interpolation of the polynomial product.
        val i = y.size / 3 + 1

        val x0Len = minOf(x.size, i)
        val x1Len = minOf(x.size - x0Len, i)

        val y0Len = i
        val y1Len = minOf(y.size - y0Len, i)

        // Break x and y into three parts, representing an order two polynomial.
        // t is chosen to be the size of a digit so we can use faster shifts
        // in place of multiplications.
        //
        // x(t) = x2*t^2 + x1*t + x0
        val x0 = bigintFromSlice(x.subList(0, x0Len))
        val x1 = bigintFromSlice(x.subList(x0Len, x0Len + x1Len))
        val x2 = bigintFromSlice(x.subList(x0Len + x1Len, x.size))

        // y(t) = y2*t^2 + y1*t + y0
        val y0 = bigintFromSlice(y.subList(0, y0Len))
        val y1 = bigintFromSlice(y.subList(y0Len, y0Len + y1Len))
        val y2 = bigintFromSlice(y.subList(y0Len + y1Len, y.size))

        // x0 + x2, avoiding temporaries
        val p = x0 + x2

        // y0 + y2, avoiding temporaries
        val q = y0 + y2

        // x2 - x1 + x0, avoiding temporaries
        val p2 = p - x1

        // y2 - y1 + y0, avoiding temporaries
        val q2 = q - y1

        // w(0)
        val r0 = x0 * y0

        // w(inf)
        val r4 = x2 * y2

        // w(1)
        val r1 = (p + x1) * (q + y1)

        // w(-1)
        val r2 = p2 * q2

        // w(-2)
        val r3 = ((p2 + x2) * 2 - x0) * ((q2 + y2) * 2 - y0)

        // Evaluating these points gives us the following system of linear equations.
        //
        //  0  0  0  0  1 | a
        //  1  1  1  1  1 | b
        //  1 -1  1 -1  1 | c
        // 16 -8  4 -2  1 | d
        //  1  0  0  0  0 | e
        //
        // The solved equation (after gaussian elimination or similar)
        // in terms of its coefficients:
        //
        // w0 = w(0)
        // w1 = w(0)/2 + w(1)/3 - w(-1) + w(-2)/6 - 2*w(inf)
        // w2 = -w(0) + w(1)/2 + w(-1)/2 - w(inf)
        // w3 = -w(0)/2 + w(1)/6 + w(-1)/2 - w(-2)/6 + 2*w(inf)
        // w4 = w(inf)
        //
        // This particular sequence is given by Bodrato and is an interpolation
        // of the above equations.
        var comp3: BigInt = (r3 - r1) / 3u
        val comp1: BigInt = (r1 - r2) shr 1
        val comp2: BigInt = r2 - r0
        comp3 = ((comp2 - comp3) shr 1) + (r4 shl 1)
        comp2.plusAssign(comp1 - r4)
        comp1.minusAssign(comp3)

        // Recomposition. The coefficients of the polynomial are now known.
        for ((j, result) in listOf(r0, comp1, comp2, comp3, r4).withIndex().reversed()) {
            when (result.sign()) {
                Sign.Plus -> {
                    add2(acc.subList(i * j, acc.size), result.data.data)
                }

                Sign.Minus -> {
                    sub2(acc.subList(i * j, acc.size), result.data.data)
                }

                Sign.NoSign -> {}
            }
        }
    }
}

private fun mul3(x: List<BigDigit>, y: List<BigDigit>): BigUint {
    val len = x.size + y.size + 1
    val prod = BigUint(MutableList(len) { 0u })

    mac3(prod.data, x, y)
    return prod.normalized()
}

private fun scalarMul(a: BigUint, b: BigDigit) {
    if (b == 1u) return
    if (b == 0u) {
        a.setZero()
        return
    }
    if (b.countOneBits() == 1) {
        a.shiftLeftAssign(b.countTrailingZeroBits().toULong())
        return
    }

    var carry = 0uL
    var i = 0
    while (i < a.data.size) {
        val nextAcc = mulWithCarry(a.data[i], b, carry)
        a.data[i] = nextAcc.toUInt()
        carry = nextAcc shr BIG_DIGIT_BITS
        i += 1
    }
    if (carry != 0uL) {
        a.data.add(carry.toUInt())
    }
}

internal fun subSign(a0: List<BigDigit>, b0: List<BigDigit>): Pair<Sign, BigUint> {
    var a = a0
    var b = b0
    // Normalize:
    if (a.lastOrNull() == 0u) {
        val len = a.indexOfLast { it != 0u } + 1
        a = a.subList(0, len)
    }
    if (b.lastOrNull() == 0u) {
        val len = b.indexOfLast { it != 0u } + 1
        b = b.subList(0, len)
    }

    val cmp = cmpSlice(a, b)
    return when {
        cmp > 0 -> {
            val aVec = a.toMutableList()
            sub2(aVec, b)
            Pair(Sign.Plus, biguintFromVec(aVec))
        }

        cmp < 0 -> {
            val bVec = b.toMutableList()
            sub2(bVec, a)
            Pair(Sign.Minus, biguintFromVec(bVec))
        }

        else -> {
            Pair(Sign.NoSign, BigUint.ZERO)
        }
    }
}

operator fun BigUint.times(other: BigUint): BigUint =
    when {
        data.isEmpty() || other.data.isEmpty() -> BigUint.ZERO
        other.data.size == 1 -> this * other.data[0]
        data.size == 1 -> other * data[0]
        else -> mul3(data, other.data)
    }

operator fun BigUint.timesAssign(other: BigUint) {
    if (data.isEmpty()) return
    when {
        other.data.isEmpty() -> {
            setZero()
        }

        other.data.size == 1 -> {
            timesAssign(other.data[0])
        }

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

operator fun UInt.times(other: BigUint): BigUint = other * this

operator fun BigUint.timesAssign(other: UInt) {
    scalarMul(this, other)
}

operator fun BigUint.times(other: ULong): BigUint {
    val result = clone()
    result *= other
    return result
}

operator fun ULong.times(other: BigUint): BigUint = other * this

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

operator fun BigUint.times(other: UByte): BigUint = this * other.toUInt()

operator fun UByte.times(other: BigUint): BigUint = toUInt() * other

operator fun BigUint.timesAssign(other: UByte) {
    this *= other.toUInt()
}

operator fun BigUint.times(other: UShort): BigUint = this * other.toUInt()

operator fun UShort.times(other: BigUint): BigUint = toUInt() * other

operator fun BigUint.timesAssign(other: UShort) {
    this *= other.toUInt()
}

fun BigUint.checkedMul(v: BigUint): BigUint? = this * v

fun Iterable<BigUint>.productBigUint(): BigUint {
    val product = BigUint.one()
    for (value in this) {
        product.timesAssign(value)
    }
    return product
}
