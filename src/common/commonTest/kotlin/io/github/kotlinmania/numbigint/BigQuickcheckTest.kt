// port-lint: tests ci/big_quickcheck/src/lib.rs

/**
 * Quickcheck of [BigUint] and [BigInt]
 *
 * This test is in a completely separate module so we can use quickcheck helpers only when
 * quickcheck is active. The main crate can't have optional dev-dependencies, and it's
 * better not to expose it as a "feature" optional dependency.
 */
package io.github.kotlinmania.numbigint

import io.github.kotlinmania.numbigint.benches.rng.getRng
import io.github.kotlinmania.numbigint.bigint.minus
import io.github.kotlinmania.numbigint.bigint.plus
import io.github.kotlinmania.numbigint.bigint.times
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import io.github.kotlinmania.numbigint.bigint.arbitrary as arbitraryBigInt
import io.github.kotlinmania.numbigint.bigint.arbitraryTakeRest as arbitraryTakeRestBigInt
import io.github.kotlinmania.numbigint.bigint.shrink as shrinkBigInt
import io.github.kotlinmania.numbigint.bigint.sizeHint as sizeHintBigInt
import io.github.kotlinmania.numbigint.biguint.arbitrary as arbitraryBigUint
import io.github.kotlinmania.numbigint.biguint.arbitraryTakeRest as arbitraryTakeRestBigUint
import io.github.kotlinmania.numbigint.biguint.shrink as shrinkBigUint
import io.github.kotlinmania.numbigint.biguint.sizeHint as sizeHintBigUint

private const val QUICKCHECK_ITERATIONS: Int = 100

class BigQuickcheckTest {
    @Test
    fun quickcheckUnsignedEqReflexive() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigUint(rng)
            assertTrue(a == a)
            for (shrunk in shrinkBigUint(a)) {
                assertTrue(shrunk == shrunk)
            }
        }
        val bytes = ByteArray(16) { it.toByte() }
        val u = arbitraryBigUint(bytes).getOrThrow()
        assertEquals(u, arbitraryTakeRestBigUint(bytes).getOrThrow())
        assertEquals(Pair(0, null), sizeHintBigUint(0))
    }

    @Test
    fun quickcheckSignedEqReflexive() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigInt(rng)
            assertTrue(a == a)
            for (shrunk in shrinkBigInt(a)) {
                assertTrue(shrunk == shrunk)
            }
        }
        val bytes = ByteArray(17) { it.toByte() }
        val s = arbitraryBigInt(bytes).getOrThrow()
        assertEquals(s, arbitraryTakeRestBigInt(bytes).getOrThrow())
        assertEquals(Pair(1, null), sizeHintBigInt(0))
    }

    @Test
    fun quickcheckUnsignedEqSymmetric() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigUint(rng)
            val b = arbitraryBigUint(rng)
            val ok =
                if (a == b) {
                    b == a
                } else {
                    b != a
                }
            assertTrue(ok)
        }
    }

    @Test
    fun quickcheckSignedEqSymmetric() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigInt(rng)
            val b = arbitraryBigInt(rng)
            val ok =
                if (a == b) {
                    b == a
                } else {
                    b != a
                }
            assertTrue(ok)
        }
    }

    @Test
    fun quickcheckArithPrimitive() {
        val rng = getRng()

        fun testUnsignedAddPrimitive(a: ULong, b: ULong): Boolean {
            val actual = BigUint.from(a) + BigUint.from(b)
            val sum = a + b
            return if (sum < a) {
                true
            } else {
                BigUint.from(sum) == actual
            }
        }

        fun testSignedAddPrimitive(a: Long, b: Long): Boolean {
            val actual = BigInt.from(a) + BigInt.from(b)
            val sum = a + b
            val overflow = ((a xor sum) and (b xor sum)) < 0L
            return if (overflow) {
                true
            } else {
                BigInt.from(sum) == actual
            }
        }

        fun testUnsignedMulPrimitive(a: UInt, b: UInt): Boolean =
            BigUint.from(a.toULong() * b.toULong()) == BigUint.from(a) * BigUint.from(b)

        fun testSignedMulPrimitive(a: Int, b: Int): Boolean =
            BigInt.from(a.toLong() * b.toLong()) == BigInt.from(a) * BigInt.from(b)

        fun testUnsignedSubPrimitive(a: ULong, b: ULong): Boolean =
            if (b < a) {
                BigUint.from(a - b) == BigUint.from(a) - BigUint.from(b)
            } else {
                BigUint.from(b - a) == BigUint.from(b) - BigUint.from(a)
            }

        fun testSignedSubPrimitive(a: Long, b: Long): Boolean {
            val actual = BigInt.from(a) - BigInt.from(b)
            val diff = a - b
            val overflow = ((a xor b) and (a xor diff)) < 0L
            return if (overflow) {
                true
            } else {
                BigInt.from(diff) == actual
            }
        }

        fun testUnsignedDivPrimitive(a: ULong, b: ULong): Boolean =
            if (b == 0uL) {
                true
            } else {
                BigUint.from(a / b) == BigUint.from(a) / BigUint.from(b)
            }

        fun testSignedDivPrimitive(a: Long, b: Long): Boolean =
            if (b == 0L || (a == Long.MIN_VALUE && b == -1L)) {
                true
            } else {
                BigInt.from(a / b) == BigInt.from(a) / BigInt.from(b)
            }

        repeat(QUICKCHECK_ITERATIONS) {
            val ua = rng.nextU64()
            val ub = rng.nextU64()
            val sa = ua.toLong()
            val sb = ub.toLong()
            assertTrue(testUnsignedAddPrimitive(ua, ub))
            assertTrue(testSignedAddPrimitive(sa, sb))
            assertTrue(testUnsignedMulPrimitive(ua.toUInt(), ub.toUInt()))
            assertTrue(testSignedMulPrimitive(sa.toInt(), sb.toInt()))
            assertTrue(testUnsignedSubPrimitive(ua, ub))
            assertTrue(testSignedSubPrimitive(sa, sb))
            assertTrue(testUnsignedDivPrimitive(ua, ub))
            assertTrue(testSignedDivPrimitive(sa, sb))
        }
    }

    @Test
    fun quickcheckUnsignedAddCommutative() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigUint(rng)
            val b = arbitraryBigUint(rng)
            assertTrue(a + b == b + a)
        }
    }

    @Test
    fun quickcheckSignedAddCommutative() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigInt(rng)
            val b = arbitraryBigInt(rng)
            assertTrue(a + b == b + a)
        }
    }

    @Test
    fun quickcheckUnsignedAddZero() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigUint(rng)
            assertTrue(a == a + BigUint.zero())
        }
    }

    @Test
    fun quickcheckSignedAddZero() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigInt(rng)
            assertTrue(a == a + BigInt.zero())
        }
    }

    @Test
    fun quickcheckUnsignedAddAssociative() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigUint(rng)
            val b = arbitraryBigUint(rng)
            val c = arbitraryBigUint(rng)
            assertTrue((a + b) + c == a + (b + c))
        }
    }

    @Test
    fun quickcheckSignedAddAssociative() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigInt(rng)
            val b = arbitraryBigInt(rng)
            val c = arbitraryBigInt(rng)
            assertTrue((a + b) + c == a + (b + c))
        }
    }

    @Test
    fun quickcheckUnsignedMulZero() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigUint(rng)
            assertTrue(a * BigUint.zero() == BigUint.zero())
        }
    }

    @Test
    fun quickcheckSignedMulZero() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigInt(rng)
            assertTrue(a * BigInt.zero() == BigInt.zero())
        }
    }

    @Test
    fun quickcheckUnsignedMulOne() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigUint(rng)
            assertTrue(a * BigUint.one() == a)
        }
    }

    @Test
    fun quickcheckSignedMulOne() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigInt(rng)
            assertTrue(a * BigInt.one() == a)
        }
    }

    @Test
    fun quickcheckUnsignedMulCommutative() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigUint(rng)
            val b = arbitraryBigUint(rng)
            assertTrue(a * b == b * a)
        }
    }

    @Test
    fun quickcheckSignedMulCommutative() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigInt(rng)
            val b = arbitraryBigInt(rng)
            assertTrue(a * b == b * a)
        }
    }

    @Test
    fun quickcheckUnsignedMulAssociative() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigUint(rng)
            val b = arbitraryBigUint(rng)
            val c = arbitraryBigUint(rng)
            assertTrue((a * b) * c == a * (b * c))
        }
    }

    @Test
    fun quickcheckSignedMulAssociative() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigInt(rng)
            val b = arbitraryBigInt(rng)
            val c = arbitraryBigInt(rng)
            assertTrue((a * b) * c == a * (b * c))
        }
    }

    @Test
    fun quickcheckUnsignedDistributive() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigUint(rng)
            val b = arbitraryBigUint(rng)
            val c = arbitraryBigUint(rng)
            assertTrue(a * (b + c) == a * b + a * c)
        }
    }

    @Test
    fun quickcheckSignedDistributive() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigInt(rng)
            val b = arbitraryBigInt(rng)
            val c = arbitraryBigInt(rng)
            assertTrue(a * (b + c) == a * b + a * c)
        }
    }

    /** Tests that exactly one of a<b a>b a=b is true */
    @Test
    fun quickcheckUnsignedGeLeEqMutExclusive() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigUint(rng)
            val b = arbitraryBigUint(rng)
            val gtLtEq = listOf(a > b, a < b, a == b)
            assertEquals(1, gtLtEq.fold(0) { acc, e -> if (e) acc + 1 else acc })
        }
    }

    /** Tests that exactly one of a<b a>b a=b is true */
    @Test
    fun quickcheckSignedGeLeEqMutExclusive() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigInt(rng)
            val b = arbitraryBigInt(rng)
            val gtLtEq = listOf(a > b, a < b, a == b)
            assertEquals(1, gtLtEq.fold(0) { acc, e -> if (e) acc + 1 else acc })
        }
    }

    /** Tests correctness of subtraction assuming addition is correct */
    @Test
    fun quickcheckUnsignedSub() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigUint(rng)
            val b = arbitraryBigUint(rng)
            val ok =
                if (b < a) {
                    a - b + b == a
                } else {
                    b - a + a == b
                }
            assertTrue(ok)
        }
    }

    /** Tests correctness of subtraction assuming addition is correct */
    @Test
    fun quickcheckSignedSub() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigInt(rng)
            val b = arbitraryBigInt(rng)
            val ok =
                if (b < a) {
                    a - b + b == a
                } else {
                    b - a + a == b
                }
            assertTrue(ok)
        }
    }

    @Test
    fun quickcheckUnsignedPowZero() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigUint(rng)
            assertTrue(a.pow(0u) == BigUint.one())
        }
    }

    @Test
    fun quickcheckUnsignedPowOne() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigUint(rng)
            assertTrue(a.pow(1u) == a)
        }
    }

    @Test
    fun quickcheckUnsignedSqrt() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigUint(rng)
            assertTrue((a * a).sqrt() == a)
        }
    }

    @Test
    fun quickcheckUnsignedCbrt() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigUint(rng)
            assertTrue((a * a * a).cbrt() == a)
        }
    }

    @Test
    fun quickcheckSignedCbrt() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigInt(rng)
            assertTrue((a * a * a).cbrt() == a)
        }
    }

    @Test
    fun quickcheckUnsignedConversion() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigUint(rng)
            val radix = (rng.nextU32() % 35u) + 2u
            val string = a.toStrRadix(radix)
            assertEquals(a, BigUint.fromStrRadix(string, radix).getOrThrow())
        }
    }

    @Test
    fun quickcheckSignedConversion() {
        val rng = getRng()
        repeat(QUICKCHECK_ITERATIONS) {
            val a = arbitraryBigInt(rng)
            val radix = (rng.nextU32() % 35u) + 2u
            val string = a.toStrRadix(radix)
            assertEquals(a, BigInt.fromStrRadix(string, radix).getOrThrow())
        }
    }

    @Test
    fun quicktestShift() {
        val rng = getRng()

        fun testShrUnsigned(a: ULong, shift: UByte): Boolean {
            val s = (shift % 64u).toInt()
            val bigA = BigUint.from(a)
            return BigUint.from(a shr s) == (bigA shr s)
        }

        fun testShrSigned(a: Long, shift: UByte): Boolean {
            val s = (shift % 64u).toInt()
            val bigA = BigInt.from(a)
            return BigInt.from(a shr s) == (bigA shr s)
        }

        fun testShlUnsigned(a: UInt, shift: UByte): Boolean {
            val s = (shift % 32u).toInt()
            val a64 = a.toULong()
            val bigA = BigUint.from(a64)
            return BigUint.from(a64 shr s) == (bigA shr s)
        }

        fun testShlSigned(a: Int, shift: UByte): Boolean {
            val s = (shift % 32u).toInt()
            val a64 = a.toULong()
            val bigA = BigInt.from(a64)
            return BigInt.from(a64 shr s) == (bigA shr s)
        }

        repeat(QUICKCHECK_ITERATIONS) {
            val u64 = rng.nextU64()
            val shift = rng.nextU32().toUByte()
            assertTrue(testShrUnsigned(u64, shift))
            assertTrue(testShrSigned(u64.toLong(), shift))
            assertTrue(testShlUnsigned(u64.toUInt(), shift))
            assertTrue(testShlSigned(u64.toInt(), shift))
        }
    }

    @Test
    fun quickcheckModpow() {
        val rng = getRng()

        fun simpleModpow(
            base: BigInt,
            exponent: BigInt,
            modulus: BigInt,
        ): BigInt {
            check(!exponent.isNegative())
            var result = BigInt.one().modFloor(modulus)
            var b = base.modFloor(modulus)
            var exp = exponent.clone()
            while (!exp.isZero()) {
                if (exp.isOdd()) {
                    result = (result * b).modFloor(modulus)
                }
                b = (b * b).modFloor(modulus)
                exp = exp shr 1
            }
            return result
        }

        fun testModpow(
            base: Long,
            exponent: ULong,
            modulus: Long,
        ): Boolean {
            if (modulus == 0L) {
                return true
            }
            val b = BigInt.from(base)
            val exp = BigInt.from(exponent)
            val m = BigInt.from(modulus)
            val modpow = b.modpow(exp, m)
            val simple = simpleModpow(b, exp, m)
            return modpow == simple
        }

        repeat(QUICKCHECK_ITERATIONS) {
            val base = rng.nextU64().toLong()
            val exponent = rng.nextU64()
            val modulus = rng.nextU64().toLong()
            assertTrue(testModpow(base, exponent, modulus))
        }
    }

    @Test
    fun quickcheckModinv() {
        val rng = getRng()

        fun testModinv(
            value: Long,
            modulus: Long,
        ): Boolean {
            if (modulus == 0L) {
                return true
            }
            val v = BigInt.from(value)
            val m = BigInt.from(modulus)
            val inv = v.modinv(m)
            val coprime = v.gcd(m).isOne()
            return when {
                inv == null && !coprime -> {
                    true
                }

                inv == null && coprime -> {
                    false
                }

                inv != null && !coprime -> {
                    false
                }

                else -> {
                    val inverse = checkNotNull(inv)
                    val zero = BigInt.zero()
                    if ((m.isPositive() && !(zero <= inverse && inverse < m)) ||
                        (m.isNegative() && !(m < inverse && inverse <= zero))
                    ) {
                        return false
                    }
                    val product = (v * inverse).modFloor(m)
                    val modOne = BigInt.one().modFloor(m)
                    product == modOne
                }
            }
        }

        repeat(QUICKCHECK_ITERATIONS) {
            val value = rng.nextU64().toLong()
            val modulus = rng.nextU64().toLong()
            assertTrue(testModinv(value, modulus))
        }
    }

    @Test
    fun quickcheckToFloatEqualsI128Cast() {
        val rng = getRng()

        fun toF32EqualsCast(value: Long): Boolean =
            BigInt.from(value).toFloatOrNull() == Float.fromBits(value.toFloat().toRawBits())

        fun toF64EqualsCast(value: Long): Boolean = BigInt.from(value).toDoubleOrNull() == value.toDouble()

        repeat(QUICKCHECK_ITERATIONS) {
            val value = rng.nextU64().toLong()
            assertTrue(toF32EqualsCast(value))
            assertTrue(toF64EqualsCast(value))
        }
    }
}
