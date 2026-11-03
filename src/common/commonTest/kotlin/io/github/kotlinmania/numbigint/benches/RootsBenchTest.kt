// port-lint: tests benches/roots.rs
package io.github.kotlinmania.numbigint

import io.github.kotlinmania.numbigint.benches.rng.Bencher
import io.github.kotlinmania.numbigint.benches.rng.getRng
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// The `big64` cases demonstrate the speed of cases where the value
// can be converted to a `u64` primitive for faster calculation.
//
// The `big1k` cases demonstrate those that can convert to `f64` for
// a better initial guess of the actual value.
//
// The `big2k` and `big4k` cases are too big for `f64`, and use a simpler guess.

class RootsBenchTest {
    private fun check(
        x: BigUint,
        n: UInt,
    ) {
        val root = x.nthRoot(n)
        if (n == 2u) {
            assertEquals(root, x.sqrt())
        } else if (n == 3u) {
            assertEquals(root, x.cbrt())
        }

        val lo = root.pow(n)
        assertTrue(lo <= x)
        assertEquals(lo.nthRoot(n), root)
        assertEquals((lo - 1u).nthRoot(n), root - 1u)

        val hi = (root + 1u).pow(n)
        assertTrue(hi > x)
        assertEquals(hi.nthRoot(n), root + 1u)
        assertEquals((hi - 1u).nthRoot(n), root)
    }

    private fun benchSqrt(
        b: Bencher,
        bits: ULong,
    ) {
        val x = getRng().genBiguint(bits)

        check(x, 2u)
        b.iter { x.sqrt() }
    }

    @Test
    fun big64Sqrt() {
        val b = Bencher()
        benchSqrt(b, 64uL)
    }

    @Test
    fun big1kSqrt() {
        val b = Bencher()
        benchSqrt(b, 1024uL)
    }

    @Test
    fun big2kSqrt() {
        val b = Bencher()
        benchSqrt(b, 2048uL)
    }

    @Test
    fun big4kSqrt() {
        val b = Bencher()
        benchSqrt(b, 4096uL)
    }

    private fun benchCbrt(
        b: Bencher,
        bits: ULong,
    ) {
        val x = getRng().genBiguint(bits)

        check(x, 3u)
        b.iter { x.cbrt() }
    }

    @Test
    fun big64Cbrt() {
        val b = Bencher()
        benchCbrt(b, 64uL)
    }

    @Test
    fun big1kCbrt() {
        val b = Bencher()
        benchCbrt(b, 1024uL)
    }

    @Test
    fun big2kCbrt() {
        val b = Bencher()
        benchCbrt(b, 2048uL)
    }

    @Test
    fun big4kCbrt() {
        val b = Bencher()
        benchCbrt(b, 4096uL)
    }

    private fun benchNthRoot(
        b: Bencher,
        bits: ULong,
        n: UInt,
    ) {
        val x = getRng().genBiguint(bits)

        check(x, n)
        b.iter { x.nthRoot(n) }
    }

    @Test
    fun big64Nth10() {
        val b = Bencher()
        benchNthRoot(b, 64uL, 10u)
    }

    @Test
    fun big1kNth10() {
        val b = Bencher()
        benchNthRoot(b, 1024uL, 10u)
    }

    @Test
    fun big1kNth100() {
        val b = Bencher()
        benchNthRoot(b, 1024uL, 100u)
    }

    @Test
    fun big1kNth1000() {
        val b = Bencher()
        benchNthRoot(b, 1024uL, 1000u)
    }

    @Test
    fun big1kNth10000() {
        val b = Bencher()
        benchNthRoot(b, 1024uL, 10000u)
    }

    @Test
    fun big2kNth10() {
        val b = Bencher()
        benchNthRoot(b, 2048uL, 10u)
    }

    @Test
    fun big2kNth100() {
        val b = Bencher()
        benchNthRoot(b, 2048uL, 100u)
    }

    @Test
    fun big2kNth1000() {
        val b = Bencher()
        benchNthRoot(b, 2048uL, 1000u)
    }

    @Test
    fun big2kNth10000() {
        val b = Bencher()
        benchNthRoot(b, 2048uL, 10000u)
    }

    @Test
    fun big4kNth10() {
        val b = Bencher()
        benchNthRoot(b, 4096uL, 10u)
    }

    @Test
    fun big4kNth100() {
        val b = Bencher()
        benchNthRoot(b, 4096uL, 100u)
    }

    @Test
    fun big4kNth1000() {
        val b = Bencher()
        benchNthRoot(b, 4096uL, 1000u)
    }

    @Test
    fun big4kNth10000() {
        val b = Bencher()
        benchNthRoot(b, 4096uL, 10000u)
    }
}
