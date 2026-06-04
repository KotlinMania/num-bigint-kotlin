// port-lint: source biguint/monty.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.numbigint

import kotlin.native.HiddenFromObjC

private class MontyReducer(
    val n0inv: BigDigit,
) {
    companion object {
        fun new(n: BigUint): MontyReducer {
            val n0inv = invModAlt(n.data[0])
            return MontyReducer(n0inv)
        }
    }
}

@HiddenFromObjC
fun invModAlt(b: BigDigit): BigDigit {
    require(b and 1u != 0u)

    var k0 = 2u - b
    var t = b - 1u
    var i = 1
    while (i < BIG_DIGIT_BITS) {
        t *= t
        k0 *= t + 1u

        i = i shl 1
    }
    check(k0 * b == 1u)
    return 0u - k0
}

@HiddenFromObjC
fun montgomery(x: BigUint, y: BigUint, m: BigUint, k: BigDigit, n: Int): BigUint {
    require(x.data.size == n && y.data.size == n && m.data.size == n) {
        "${x.toDebugString()} ${y.toDebugString()} ${m.toDebugString()} $n"
    }

    val z = BigUint.ZERO
    z.data.resize(n * 2, 0u)

    var c: BigDigit = 0u
    for (i in 0 until n) {
        val c2 = addMulVvw(z.data.subList(i, n + i), x.data, y.data[i])
        val t = z.data[i] * k
        val c3 = addMulVvw(z.data.subList(i, n + i), m.data, t)
        val cx = c + c2
        val cy = cx + c3
        z.data[n + i] = cy
        c = if (cx < c2 || cy < c3) 1u else 0u
    }

    if (c == 0u) {
        val upper = z.data.drop(n)
        z.data.clear()
        z.data.addAll(upper)
    } else {
        val first = z.data.subList(0, n)
        val second = z.data.subList(n, z.data.size)
        subVv(first, second, m.data)
        val lower = z.data.take(n)
        z.data.clear()
        z.data.addAll(lower)
    }

    return z
}

@HiddenFromObjC
fun addMulVvw(z: MutableList<BigDigit>, x: List<BigDigit>, y: BigDigit): BigDigit {
    var c: BigDigit = 0u
    val len = minOf(z.size, x.size)
    for (i in 0 until len) {
        val (z1, z0) = mulAddWww(x[i], y, z[i])
        val (c0, zi) = addWw(z0, c, 0u)
        z[i] = zi
        c = c0 + z1
    }

    return c
}

/**
 * The resulting carry c is either 0 or 1.
 */
@HiddenFromObjC
fun subVv(z: MutableList<BigDigit>, x: List<BigDigit>, y: List<BigDigit>): BigDigit {
    var c: BigDigit = 0u
    val len = minOf(z.size, x.size, y.size)
    for (i in 0 until len) {
        val xi = x[i]
        val yi = y[i]
        val zi = xi - yi - c
        z[i] = zi
        c = ((yi and xi.inv()) or ((yi or xi.inv()) and zi)) shr (BIG_DIGIT_BITS - 1)
    }

    return c
}

/**
 * z1 * 2^W + z0 = x + y + c, with c == 0 or 1
 */
@HiddenFromObjC
fun addWw(x: BigDigit, y: BigDigit, c: BigDigit): Pair<BigDigit, BigDigit> {
    val yc = y + c
    val z0 = x + yc
    val z1 = if (z0 < x || yc < y) 1u else 0u

    return Pair(z1, z0)
}

/**
 * z1 * 2^W + z0 = x * y + c
 */
@HiddenFromObjC
fun mulAddWww(x: BigDigit, y: BigDigit, c: BigDigit): Pair<BigDigit, BigDigit> {
    val z = x.toULong() * y.toULong() + c.toULong()
    return Pair((z shr BIG_DIGIT_BITS).toUInt(), z.toUInt())
}

fun montyModpow(x0: BigUint, y: BigUint, m: BigUint): BigUint {
    require(m.data[0] and 1u == 1u)
    val mr = MontyReducer.new(m)
    val numWords = m.data.size

    var x = x0.clone()

    if (x.data.size > numWords) {
        x = x % m
    }
    if (x.data.size < numWords) {
        x.data.resize(numWords, 0u)
    }

    var rr = BigUint.one()
    rr = rr.shiftLeft(2uL * numWords.toULong() * BIG_DIGIT_BITS.toULong()) % m
    if (rr.data.size < numWords) {
        rr.data.resize(numWords, 0u)
    }
    val one = BigUint.one()
    one.data.resize(numWords, 0u)

    val window = 4
    val powers = ArrayList<BigUint>(1 shl window)
    powers.add(montgomery(one, rr, m, mr.n0inv, numWords))
    powers.add(montgomery(x, rr, m, mr.n0inv, numWords))
    for (i in 2 until (1 shl window)) {
        val r = montgomery(powers[i - 1], powers[1], m, mr.n0inv, numWords)
        powers.add(r)
    }

    var z = powers[0].clone()
    z.data.resize(numWords, 0u)
    var zz = BigUint.ZERO
    zz.data.resize(numWords, 0u)

    for (i in y.data.indices.reversed()) {
        var yi = y.data[i]
        var j = 0
        while (j < BIG_DIGIT_BITS) {
            if (i != y.data.lastIndex || j != 0) {
                zz = montgomery(z, z, m, mr.n0inv, numWords)
                z = montgomery(zz, zz, m, mr.n0inv, numWords)
                zz = montgomery(z, z, m, mr.n0inv, numWords)
                z = montgomery(zz, zz, m, mr.n0inv, numWords)
            }
            zz = montgomery(
                z,
                powers[(yi shr (BIG_DIGIT_BITS - window)).toInt()],
                m,
                mr.n0inv,
                numWords,
            )
            val tmp = z
            z = zz
            zz = tmp
            yi = yi shl window
            j += window
        }
    }

    zz = montgomery(z, one, m, mr.n0inv, numWords)

    zz.normalize()
    if (zz >= m) {
        zz = zz - m
        if (zz >= m) {
            zz = zz % m
        }
    }

    zz.normalize()
    return zz
}

private fun MutableList<BigDigit>.resize(size: Int, value: BigDigit) {
    while (this.size < size) {
        add(value)
    }
    while (this.size > size) {
        removeAt(lastIndex)
    }
}
