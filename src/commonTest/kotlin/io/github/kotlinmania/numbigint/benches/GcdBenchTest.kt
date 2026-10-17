// port-lint: tests benches/gcd.rs
package io.github.kotlinmania.numbigint

import io.github.kotlinmania.numbigint.benches.rng.Bencher
import io.github.kotlinmania.numbigint.benches.rng.getRng
import kotlin.test.Test
import kotlin.test.assertEquals

class GcdBenchTest {
    private fun bench(
        b: Bencher,
        bits: ULong,
        gcd: (BigUint, BigUint) -> BigUint,
    ) {
        val rng = getRng()
        val x = rng.genBiguint(bits)
        val y = rng.genBiguint(bits)

        assertEquals(euclid(x, y), x.gcd(y))

        b.iter { gcd(x, y) }
    }

    private fun euclid(
        x: BigUint,
        y: BigUint,
    ): BigUint {
        // Use Euclid's algorithm
        var m = x.clone()
        var n = y.clone()
        while (!m.isZero()) {
            val temp = m
            m = n % temp
            n = temp
        }
        return n
    }

    @Test
    fun gcdEuclid0064() {
        val b = Bencher()
        bench(b, 64uL, ::euclid)
    }

    @Test
    fun gcdEuclid0256() {
        val b = Bencher()
        bench(b, 256uL, ::euclid)
    }

    @Test
    fun gcdEuclid1024() {
        val b = Bencher()
        bench(b, 1024uL, ::euclid)
    }

    @Test
    fun gcdEuclid4096() {
        val b = Bencher()
        bench(b, 4096uL, ::euclid)
    }

    // Integer for BigUint now uses Stein for gcd

    @Test
    fun gcdStein0064() {
        val b = Bencher()
        bench(b, 64uL, BigUint::gcd)
    }

    @Test
    fun gcdStein0256() {
        val b = Bencher()
        bench(b, 256uL, BigUint::gcd)
    }

    @Test
    fun gcdStein1024() {
        val b = Bencher()
        bench(b, 1024uL, BigUint::gcd)
    }

    @Test
    fun gcdStein4096() {
        val b = Bencher()
        bench(b, 4096uL, BigUint::gcd)
    }
}
