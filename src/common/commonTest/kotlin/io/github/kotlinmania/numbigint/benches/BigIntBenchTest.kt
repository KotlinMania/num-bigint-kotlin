// port-lint: tests benches/bigint.rs
package io.github.kotlinmania.numbigint

import io.github.kotlinmania.numbigint.benches.rng.Bencher
import io.github.kotlinmania.numbigint.benches.rng.getRng
import io.github.kotlinmania.numbigint.bigint.times
import kotlin.test.Test
import kotlin.test.assertEquals

private const val RFC3526_2048BIT_MODP_GROUP: String =
    "FFFFFFFF_FFFFFFFF_C90FDAA2_2168C234_C4C6628B_80DC1CD1" +
        "29024E08_8A67CC74_020BBEA6_3B139B22_514A0879_8E3404DD" +
        "EF9519B3_CD3A431B_302B0A6D_F25F1437_4FE1356D_6D51C245" +
        "E485B576_625E7EC6_F44C42E9_A637ED6B_0BFF5CB6_F406B7ED" +
        "EE386BFB_5A899FA5_AE9F2411_7C4B1FE6_49286651_ECE45B3D" +
        "C2007CB8_A163BF05_98DA4836_1C55D39A_69163FA8_FD24CF5F" +
        "83655D23_DCA3AD96_1C62F356_208552BB_9ED52907_7096966D" +
        "670C354E_4ABC9804_F1746C08_CA18217C_32905E46_2E36CE3B" +
        "E39E772C_180E8603_9B2783A2_EC07A28F_B5C55DF0_6F4C52C9" +
        "DE2BCBF6_95581718_3995497C_EA956AE5_15D22618_98FA0510" +
        "15728E5A_8AACAA68_FFFFFFFF_FFFFFFFF"

class BigIntBenchTest {
    private fun multiplyBench(
        b: Bencher,
        xbits: ULong,
        ybits: ULong,
    ) {
        val rng = getRng()
        val x = rng.genBigint(xbits)
        val y = rng.genBigint(ybits)

        b.iter { x * y }
    }

    private fun divideBench(
        b: Bencher,
        xbits: ULong,
        ybits: ULong,
    ) {
        val rng = getRng()
        val x = rng.genBigint(xbits)
        val y = rng.genBigint(ybits)

        b.iter { x / y }
    }

    private fun remainderBench(
        b: Bencher,
        xbits: ULong,
        ybits: ULong,
    ) {
        val rng = getRng()
        val x = rng.genBigint(xbits)
        val y = rng.genBigint(ybits)

        b.iter { x % y }
    }

    private fun factorial(n: Int): BigUint {
        val f = BigUint.one()
        for (i in 1..n) {
            val bu = BigUint.from(i.toULong())
            f.timesAssign(bu)
        }
        return f
    }

    /** Compute Fibonacci numbers */
    private fun fib(n: Int): BigUint {
        var f0 = BigUint.zero()
        var f1 = BigUint.one()
        repeat(n) {
            val f2 = f0 + f1
            f0 = f1
            f1 = f2
        }
        return f0
    }

    /**
     * Compute Fibonacci numbers with two ops per iteration
     * (add and subtract, like issue #200)
     */
    private fun fib2(n: Int): BigUint {
        var f0 = BigUint.zero()
        val f1 = BigUint.one()
        repeat(n) {
            f1 += f0
            f0 = f1 - f0
        }
        return f0
    }

    @Test
    fun multiply0() {
        val b = Bencher()
        multiplyBench(b, 1uL shl 8, 1uL shl 8)
    }

    @Test
    fun multiply1() {
        val b = Bencher()
        multiplyBench(b, 1uL shl 8, 1uL shl 16)
    }

    @Test
    fun multiply2() {
        val b = Bencher()
        multiplyBench(b, 1uL shl 16, 1uL shl 16)
    }

    @Test
    fun multiply3() {
        val b = Bencher()
        multiplyBench(b, 1uL shl 16, 1uL shl 17)
    }

    @Test
    fun multiply4() {
        val b = Bencher()
        multiplyBench(b, 1uL shl 12, 1uL shl 13)
    }

    @Test
    fun multiply5() {
        val b = Bencher()
        multiplyBench(b, 1uL shl 12, 1uL shl 14)
    }

    @Test
    fun divide0() {
        val b = Bencher()
        divideBench(b, 1uL shl 8, 1uL shl 6)
    }

    @Test
    fun divide1() {
        val b = Bencher()
        divideBench(b, 1uL shl 12, 1uL shl 8)
    }

    @Test
    fun divide2() {
        val b = Bencher()
        divideBench(b, 1uL shl 16, 1uL shl 12)
    }

    @Test
    fun divideBigLittle() {
        val b = Bencher()
        divideBench(b, 1uL shl 16, 1uL shl 4)
    }

    @Test
    fun remainder0() {
        val b = Bencher()
        remainderBench(b, 1uL shl 8, 1uL shl 6)
    }

    @Test
    fun remainder1() {
        val b = Bencher()
        remainderBench(b, 1uL shl 12, 1uL shl 8)
    }

    @Test
    fun remainder2() {
        val b = Bencher()
        remainderBench(b, 1uL shl 16, 1uL shl 12)
    }

    @Test
    fun remainderBigLittle() {
        val b = Bencher()
        remainderBench(b, 1uL shl 16, 1uL shl 4)
    }

    @Test
    fun factorial100() {
        val b = Bencher()
        b.iter { factorial(100) }
    }

    @Test
    fun fib100() {
        val b = Bencher()
        b.iter { fib(100) }
    }

    @Test
    fun fib1000() {
        val b = Bencher()
        b.iter { fib(1000) }
    }

    @Test
    fun fib10000() {
        val b = Bencher()
        b.iter { fib(10000) }
    }

    @Test
    fun fib2100() {
        val b = Bencher()
        b.iter { fib2(100) }
    }

    @Test
    fun fib21000() {
        val b = Bencher()
        b.iter { fib2(1000) }
    }

    @Test
    fun fib210000() {
        val b = Bencher()
        b.iter { fib2(10000) }
    }

    @Test
    fun facToString() {
        val b = Bencher()
        val fac = factorial(100)
        b.iter { fac.toString() }
    }

    @Test
    fun fibToString() {
        val b = Bencher()
        val fib = fib(100)
        b.iter { fib.toString() }
    }

    private fun toStrRadixBench(
        b: Bencher,
        radix: UInt,
        bits: ULong,
    ) {
        val rng = getRng()
        val x = rng.genBigint(bits)
        b.iter { x.toStrRadix(radix) }
    }

    @Test
    fun toStrRadix02() {
        val b = Bencher()
        toStrRadixBench(b, 2u, 1009uL)
    }

    @Test
    fun toStrRadix08() {
        val b = Bencher()
        toStrRadixBench(b, 8u, 1009uL)
    }

    @Test
    fun toStrRadix10() {
        val b = Bencher()
        toStrRadixBench(b, 10u, 1009uL)
    }

    @Test
    fun toStrRadix102() {
        val b = Bencher()
        toStrRadixBench(b, 10u, 10009uL)
    }

    @Test
    fun toStrRadix16() {
        val b = Bencher()
        toStrRadixBench(b, 16u, 1009uL)
    }

    @Test
    fun toStrRadix36() {
        val b = Bencher()
        toStrRadixBench(b, 36u, 1009uL)
    }

    private fun fromStrRadixBench(
        b: Bencher,
        radix: UInt,
    ) {
        val rng = getRng()
        val x = rng.genBigint(1009uL)
        val s = x.toStrRadix(radix)
        assertEquals(x, BigInt.fromStrRadix(s, radix).getOrThrow())
        b.iter { BigInt.fromStrRadix(s, radix) }
    }

    @Test
    fun fromStrRadix02() {
        val b = Bencher()
        fromStrRadixBench(b, 2u)
    }

    @Test
    fun fromStrRadix08() {
        val b = Bencher()
        fromStrRadixBench(b, 8u)
    }

    @Test
    fun fromStrRadix10() {
        val b = Bencher()
        fromStrRadixBench(b, 10u)
    }

    @Test
    fun fromStrRadix16() {
        val b = Bencher()
        fromStrRadixBench(b, 16u)
    }

    @Test
    fun fromStrRadix36() {
        val b = Bencher()
        fromStrRadixBench(b, 36u)
    }

    private fun randBench(
        b: Bencher,
        bits: ULong,
    ) {
        val rng = getRng()

        b.iter { rng.genBigint(bits) }
    }

    @Test
    fun rand64() {
        val b = Bencher()
        randBench(b, 1uL shl 6)
    }

    @Test
    fun rand256() {
        val b = Bencher()
        randBench(b, 1uL shl 8)
    }

    @Test
    fun rand1009() {
        val b = Bencher()
        randBench(b, 1009uL)
    }

    @Test
    fun rand2048() {
        val b = Bencher()
        randBench(b, 1uL shl 11)
    }

    @Test
    fun rand4096() {
        val b = Bencher()
        randBench(b, 1uL shl 12)
    }

    @Test
    fun rand8192() {
        val b = Bencher()
        randBench(b, 1uL shl 13)
    }

    @Test
    fun rand65536() {
        val b = Bencher()
        randBench(b, 1uL shl 16)
    }

    @Test
    fun rand131072() {
        val b = Bencher()
        randBench(b, 1uL shl 17)
    }

    @Test
    fun shl() {
        val b = Bencher()
        val n = BigUint.one() shl 1000u
        val m = n.clone()
        b.iter {
            m.cloneFrom(n)
            for (i in 0 until 50) {
                m.shiftLeftAssign(i.toULong())
            }
        }
    }

    @Test
    fun shr() {
        val b = Bencher()
        val n = BigUint.one() shl 2000u
        val m = n.clone()
        b.iter {
            m.cloneFrom(n)
            for (i in 0 until 50) {
                m.shiftRightAssign(i.toULong())
            }
        }
    }

    @Test
    fun hash() {
        val b = Bencher()
        val rng = getRng()
        val v: List<BigInt> = (1000uL until 2000uL).map { bits -> rng.genBigint(bits) }
        b.iter {
            val h: HashSet<BigInt> = v.toHashSet()
            assertEquals(h.size, v.size)
        }
    }

    @Test
    fun powBench() {
        val b = Bencher()
        b.iter {
            val upper = 100u
            val iBig = BigUint.from(1u)
            repeat((upper - 1u).toInt()) {
                iBig += 1u
                for (j in 2u..upper) {
                    iBig.pow(j)
                }
            }
        }
    }

    @Test
    fun powBenchBigexp() {
        val b = Bencher()
        b.iter {
            val upper = 100u
            val iBig = BigUint.from(1u)
            repeat((upper - 1u).toInt()) {
                iBig += 1u
                val jBig = BigUint.from(1u)
                repeat((upper - 1u).toInt()) {
                    jBig += 1u
                    iBig.pow(jBig)
                }
            }
        }
    }

    @Test
    fun powBench1e1000() {
        val b = Bencher()
        b.iter { BigUint.from(10u).pow(1_000u) }
    }

    @Test
    fun powBench1e10000() {
        val b = Bencher()
        b.iter { BigUint.from(10u).pow(10_000u) }
    }

    @Test
    fun powBench1e100000() {
        val b = Bencher()
        b.iter { BigUint.from(10u).pow(100_000u) }
    }

    @Test
    fun modpow() {
        val b = Bencher()
        val rng = getRng()
        val base = rng.genBiguint(2048uL)
        val e = rng.genBiguint(2048uL)
        val m = BigUint.fromStrRadix(RFC3526_2048BIT_MODP_GROUP, 16u).getOrThrow()

        b.iter { base.modpow(e, m) }
    }

    @Test
    fun modpowEven() {
        val b = Bencher()
        val rng = getRng()
        val base = rng.genBiguint(2048uL)
        val e = rng.genBiguint(2048uL)
        // Make the modulus even, so monty (base-2^32) doesn't apply.
        val m = BigUint.fromStrRadix(RFC3526_2048BIT_MODP_GROUP, 16u).getOrThrow() - 1u

        b.iter { base.modpow(e, m) }
    }

    @Test
    fun toU32Digits() {
        val b = Bencher()
        val rng = getRng()
        val n = rng.genBiguint(2048uL)

        b.iter { n.toU32Digits() }
    }

    @Test
    fun iterU32Digits() {
        val b = Bencher()
        val rng = getRng()
        val n = rng.genBiguint(2048uL)

        b.iter { n.iterU32Digits().asSequence().maxOrNull() }
    }

    @Test
    fun toU64Digits() {
        val b = Bencher()
        val rng = getRng()
        val n = rng.genBiguint(2048uL)

        b.iter { n.toU64Digits() }
    }

    @Test
    fun iterU64Digits() {
        val b = Bencher()
        val rng = getRng()
        val n = rng.genBiguint(2048uL)

        b.iter { n.iterU64Digits().asSequence().maxOrNull() }
    }
}
