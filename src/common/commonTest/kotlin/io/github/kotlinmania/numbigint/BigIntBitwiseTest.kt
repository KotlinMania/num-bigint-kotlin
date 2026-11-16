// port-lint: tests tests/bigint_bitwise.rs
package io.github.kotlinmania.numbigint

import io.github.kotlinmania.numbigint.ValueVec.M
import io.github.kotlinmania.numbigint.ValueVec.N
import io.github.kotlinmania.numbigint.ValueVec.P
import io.github.kotlinmania.numbigint.bigint.bitAnd
import io.github.kotlinmania.numbigint.bigint.bitOr
import io.github.kotlinmania.numbigint.bigint.bitXor
import kotlin.test.Test
import kotlin.test.assertEquals

private sealed class ValueVec {
    data object N : ValueVec()

    class P(
        vararg val s: UInt,
    ) : ValueVec()

    class M(
        vararg val s: UInt,
    ) : ValueVec()

    fun toBigInt(): BigInt? =
        when (this) {
            is N -> BigInt.fromSlice(Sign.NoSign, emptyList())
            is P -> BigInt.fromSlice(Sign.Plus, s.toList())
            is M -> BigInt.fromSlice(Sign.Minus, s.toList())
        }
}

// a, !a
private val NOT_VALUES: List<Pair<ValueVec, ValueVec>> =
    listOf(
        Pair(N, M(1u)),
        Pair(P(1u), M(2u)),
        Pair(P(2u), M(3u)),
        Pair(P(0u.inv() - 2u), M(0u.inv() - 1u)),
        Pair(P(0u.inv() - 1u), M(0u.inv())),
        Pair(P(0u.inv()), M(0u, 1u)),
        Pair(P(0u, 1u), M(1u, 1u)),
        Pair(P(1u, 1u), M(2u, 1u)),
    )

// a, b, a & b, a | b, a ^ b
private val BITWISE_VALUES: List<List<ValueVec>> =
    listOf(
        listOf(N, N, N, N, N),
        listOf(N, P(1u), N, P(1u), P(1u)),
        listOf(N, P(0u.inv()), N, P(0u.inv()), P(0u.inv())),
        listOf(N, P(0u, 1u), N, P(0u, 1u), P(0u, 1u)),
        listOf(N, M(1u), N, M(1u), M(1u)),
        listOf(N, M(0u.inv()), N, M(0u.inv()), M(0u.inv())),
        listOf(N, M(0u, 1u), N, M(0u, 1u), M(0u, 1u)),
        listOf(P(1u), P(0u.inv()), P(1u), P(0u.inv()), P(0u.inv() - 1u)),
        listOf(P(0u.inv()), P(0u.inv()), P(0u.inv()), P(0u.inv()), N),
        listOf(P(0u.inv()), P(1u, 1u), P(1u), P(0u.inv(), 1u), P(0u.inv() - 1u, 1u)),
        listOf(P(1u), M(0u.inv()), P(1u), M(0u.inv()), M(0u, 1u)),
        listOf(P(0u.inv()), M(1u), P(0u.inv()), M(1u), M(0u, 1u)),
        listOf(P(0u.inv()), M(0u.inv()), P(1u), M(1u), M(2u)),
        listOf(P(0u.inv()), M(1u, 1u), P(0u.inv()), M(1u, 1u), M(0u, 2u)),
        listOf(P(1u, 1u), M(0u.inv()), P(1u, 1u), M(0u.inv()), M(0u, 2u)),
        listOf(M(1u), M(0u.inv()), M(0u.inv()), M(1u), P(0u.inv() - 1u)),
        listOf(M(0u.inv()), M(0u.inv()), M(0u.inv()), M(0u.inv()), N),
        listOf(M(0u.inv()), M(1u, 1u), M(0u.inv(), 1u), M(1u), P(0u.inv() - 1u, 1u)),
    )

private const val I32_MIN: Long = Int.MIN_VALUE.toLong()
private const val I32_MAX: Long = Int.MAX_VALUE.toLong()
private val U32_MAX: Long = UInt.MAX_VALUE.toLong()

// some corner cases
private val I64_VALUES: List<Long> =
    listOf(
        Long.MIN_VALUE,
        Long.MIN_VALUE + 1,
        Long.MIN_VALUE + 2,
        Long.MIN_VALUE + 3,
        -U32_MAX - 3,
        -U32_MAX - 2,
        -U32_MAX - 1,
        -U32_MAX,
        -U32_MAX + 1,
        -U32_MAX + 2,
        -U32_MAX + 3,
        I32_MIN - 3,
        I32_MIN - 2,
        I32_MIN - 1,
        I32_MIN,
        I32_MIN + 1,
        I32_MIN + 2,
        I32_MIN + 3,
        -3L,
        -2L,
        -1L,
        0L,
        1L,
        2L,
        3L,
        I32_MAX - 3,
        I32_MAX - 2,
        I32_MAX - 1,
        I32_MAX,
        I32_MAX + 1,
        I32_MAX + 2,
        I32_MAX + 3,
        U32_MAX - 3,
        U32_MAX - 2,
        U32_MAX - 1,
        U32_MAX,
        U32_MAX + 1,
        U32_MAX + 2,
        U32_MAX + 3,
        Long.MAX_VALUE - 3,
        Long.MAX_VALUE - 2,
        Long.MAX_VALUE - 1,
        Long.MAX_VALUE,
    )

class BigIntBitwiseTest {
    @Test
    fun testNot() {
        for (elm in NOT_VALUES) {
            val a = elm.first.toBigInt()!!
            val not = elm.second.toBigInt()!!

            // sanity check for tests that fit in i64
            val primA = a.toLongOrNull()
            val primNot = not.toLongOrNull()
            if (primA != null && primNot != null) {
                assertEquals(primNot, primA.inv())
            }

            assertEquals(not, !a.clone(), "!${a.toLowerHexString()}")
            assertEquals(a, !not.clone(), "!${not.toLowerHexString()}")
        }
    }

    @Test
    fun testNotI64() {
        for (primA in I64_VALUES) {
            val a = primA.toBigInt()
            val not = primA.inv().toBigInt()
            assertEquals(not, !a.clone(), "!${a.toLowerHexString()}")
        }
    }

    @Test
    fun testBitwise() {
        for (row in BITWISE_VALUES) {
            val a = row[0].toBigInt()!!
            val b = row[1].toBigInt()!!
            val and = row[2].toBigInt()!!
            val or = row[3].toBigInt()!!
            val xor = row[4].toBigInt()!!

            // sanity check for tests that fit in i64
            val primA = a.toLongOrNull()
            val primB = b.toLongOrNull()
            if (primA != null && primB != null) {
                val primAnd = and.toLongOrNull()
                if (primAnd != null) {
                    assertEquals(primAnd, primA and primB)
                }
                val primOr = or.toLongOrNull()
                if (primOr != null) {
                    assertEquals(primOr, primA or primB)
                }
                val primXor = xor.toLongOrNull()
                if (primXor != null) {
                    assertEquals(primXor, primA xor primB)
                }
            }

            assertEquals(and, a.clone() bitAnd b, "${a.toLowerHexString()} & ${b.toLowerHexString()}")
            assertEquals(and, b.clone() bitAnd a, "${b.toLowerHexString()} & ${a.toLowerHexString()}")
            assertEquals(or, a.clone() bitOr b, "${a.toLowerHexString()} | ${b.toLowerHexString()}")
            assertEquals(or, b.clone() bitOr a, "${b.toLowerHexString()} | ${a.toLowerHexString()}")
            assertEquals(xor, a.clone() bitXor b, "${a.toLowerHexString()} ^ ${b.toLowerHexString()}")
            assertEquals(xor, b.clone() bitXor a, "${b.toLowerHexString()} ^ ${a.toLowerHexString()}")
        }
    }

    @Test
    fun testBitwiseI64() {
        for (primA in I64_VALUES) {
            val a = primA.toBigInt()
            for (primB in I64_VALUES) {
                val b = primB.toBigInt()
                val and = (primA and primB).toBigInt()
                val or = (primA or primB).toBigInt()
                val xor = (primA xor primB).toBigInt()
                assertEquals(and, a.clone() bitAnd b, "${a.toLowerHexString()} & ${b.toLowerHexString()}")
                assertEquals(or, a.clone() bitOr b, "${a.toLowerHexString()} | ${b.toLowerHexString()}")
                assertEquals(xor, a.clone() bitXor b, "${a.toLowerHexString()} ^ ${b.toLowerHexString()}")
            }
        }
    }
}
