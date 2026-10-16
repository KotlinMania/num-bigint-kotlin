// port-lint: tests tests/roots.rs
package io.github.kotlinmania.numbigint

import io.github.kotlinmania.numbigint.bigint.minus
import io.github.kotlinmania.numbigint.bigint.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class BigUintRootsTest {
    private fun check(x0: Any, n: UInt) {
        val x = if (x0 is BigUint) x0 else BigUint.from(x0 as UInt)
        val root = x.nthRoot(n)

        if (n == 2u) {
            assertEquals(x.sqrt(), root)
        } else if (n == 3u) {
            assertEquals(x.cbrt(), root)
        }

        val lo = root.pow(n)
        assertTrue(lo <= x)
        assertEquals(root, lo.nthRoot(n))
        if (!lo.isZero()) {
            assertEquals(root - 1u, (lo - 1u).nthRoot(n))
        }

        val hi = (root + 1u).pow(n)
        assertTrue(hi > x)
        assertEquals(root + 1u, hi.nthRoot(n))
        assertEquals(root, (hi - 1u).nthRoot(n))
    }

    @Test
    fun testSqrt() {
        check(99u, 2u)
        check(100u, 2u)
        check(120u, 2u)
    }

    @Test
    fun testCbrt() {
        check(8u, 3u)
        check(26u, 3u)
    }

    @Test
    fun testNthRoot() {
        check(0u, 1u)
        check(10u, 1u)
        check(100u, 4u)
    }

    @Test
    fun testNthRootNIsZero() {
        assertFailsWith<Throwable> {
            check(4u, 0u)
        }
    }

    @Test
    fun testNthRootBig() {
        val x = BigUint.from(123_456_789u)
        val expected = BigUint.from(6u)

        assertEquals(expected, x.nthRoot(10u))
        check(x, 10u)
    }

    @Test
    fun testNthRootGoogol() {
        val googol = BigUint.from(10u).pow(100u)

        // perfect divisors of 100
        for (n in listOf(2u, 4u, 5u, 10u, 20u, 25u, 50u, 100u)) {
            val expected = BigUint.from(10u).pow(100u / n)
            assertEquals(expected, googol.nthRoot(n))
            check(googol.clone(), n)
        }
    }

    @Test
    fun testNthRootTwos() {
        val expMax = 12u
        val log2 = 1 shl expMax.toInt()
        val x = BigUint.one() shl log2

        // the perfect divisors are just powers of two
        for (exp in 1u..expMax) {
            val n = 1u shl exp.toInt()
            val expected = BigUint.one() shl (log2 / n.toInt())
            assertEquals(expected, x.nthRoot(n))
            check(x.clone(), n)
        }

        // degenerate cases should return quickly
        assertTrue(x.nthRoot(x.bits().toUInt()).isOne())
        assertTrue(x.nthRoot(Int.MAX_VALUE.toUInt()).isOne())
        assertTrue(x.nthRoot(UInt.MAX_VALUE).isOne())
    }

    @Test
    fun testRootsRand1() {
        // A random input that found regressions
        val s =
            "575981506858479247661989091587544744717244516135539456183849" +
                "986593934723426343633698413178771587697273822147578889823552" +
                "182702908597782734558103025298880194023243541613924361007059" +
                "353344183590348785832467726433749431093350684849462759540710" +
                "026019022227591412417064179299354183441181373862905039254106" +
                "4781867"
        val x = BigUint.fromStrRadix(s, 10u).getOrThrow()

        check(x.clone(), 2u)
        check(x.clone(), 3u)
        check(x.clone(), 10u)
        check(x, 100u)
    }
}

class BigIntRootsTest {
    private fun check(x: Long, n: UInt) {
        val bigX = BigInt.from(x)
        val res = bigX.nthRoot(n)

        if (n == 2u) {
            assertEquals(bigX.sqrt(), res)
        } else if (n == 3u) {
            assertEquals(bigX.cbrt(), res)
        }

        if (bigX.isNegative()) {
            assertTrue(res.pow(n) >= bigX)
            assertTrue((res - 1u).pow(n) < bigX)
        } else {
            assertTrue(res.pow(n) <= bigX)
            assertTrue((res + 1u).pow(n) > bigX)
        }
    }

    @Test
    fun testNthRoot() {
        check(-100L, 3u)
    }

    @Test
    fun testNthRootXNegNEven() {
        assertFailsWith<Throwable> {
            check(-100L, 4u)
        }
    }

    @Test
    fun testSqrtXNeg() {
        assertFailsWith<Throwable> {
            check(-4L, 2u)
        }
    }

    @Test
    fun testCbrt() {
        check(8L, 3u)
        check(-8L, 3u)
    }
}
