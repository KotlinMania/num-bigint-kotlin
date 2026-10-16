// port-lint: tests ci/big_rand/src/torture.rs
package io.github.kotlinmania.numbigint

import io.github.kotlinmania.rand.distr.Uniform
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class BigRandTortureTest {
    private fun getRng(): XorShiftRng =
        XorShiftRng.seedFromU64(0x1234_5678_9abc_def0uL)

    private fun testMulDivideTortureCount(count: Int) {
        val bitsMax = 1uL shl 12
        val rng = getRng()
        val bitDist = Uniform.uLong(0uL, bitsMax)

        repeat(count) {
            // Test with numbers of random sizes:
            val xbits = rng.sample(bitDist)
            val ybits = rng.sample(bitDist)

            val x = rng.genBiguint(xbits)
            val y = rng.genBiguint(ybits)

            if (x.isZero() || y.isZero()) {
                return@repeat
            }

            val prod = x * y
            assertEquals(prod / x, y)
            assertEquals(prod / y, x)
        }
    }

    @Test
    fun testMulDivideTorture() {
        testMulDivideTortureCount(1_000)
    }

    @Test
    @Ignore
    fun testMulDivideTortureLong() {
        testMulDivideTortureCount(1_000_000)
    }

    private fun testFactoredMulTortureCount(count: Int) {
        val bits = 1uL shl 16
        val rng = getRng()

        repeat(count) {
            val w = rng.genBiguint(bits)
            val x = rng.genBiguint(bits)
            val y = rng.genBiguint(bits)
            val z = rng.genBiguint(bits)

            val prod1 = (w * x) * (y * z)
            val prod2 = (w * y) * (x * z)
            val prod3 = (w * z) * (x * y)
            assertEquals(prod1, prod2)
            assertEquals(prod2, prod3)
        }
    }

    @Test
    fun testFactoredMulTorture() {
        testFactoredMulTortureCount(50)
    }

    @Test
    @Ignore
    fun testFactoredMulTortureLong() {
        testFactoredMulTortureCount(1_000)
    }
}
