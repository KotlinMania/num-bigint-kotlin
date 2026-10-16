// port-lint: tests biguint/power.rs
package io.github.kotlinmania.numbigint

import kotlin.test.Test
import kotlin.test.assertEquals

class PowerTest {
    @Test
    fun testPlainModpow() {
        val two = BigUint.fromUInt(2u)
        val modulus = BigUint.fromUInt(0x1100u)

        var exp = listOf(0u, 0b1u)
        assertEquals(
            two.pow(0b1_00000000u) % modulus,
            plainModpow(two, exp, modulus),
        )
        exp = listOf(0u, 0b10u)
        assertEquals(
            two.pow(0b10_00000000u) % modulus,
            plainModpow(two, exp, modulus),
        )
        exp = listOf(0u, 0b110010u)
        assertEquals(
            two.pow(0b110010_00000000u) % modulus,
            plainModpow(two, exp, modulus),
        )
        exp = listOf(0b1u, 0b1u)
        assertEquals(
            two.pow(0b1_00000001u) % modulus,
            plainModpow(two, exp, modulus),
        )
        exp = listOf(0b1100u, 0u, 0b1u)
        assertEquals(
            two.pow(0b1_00000000_00001100u) % modulus,
            plainModpow(two, exp, modulus),
        )
    }

    @Test
    fun testPowBiguint() {
        val base = BigUint.fromUInt(5u)
        val exponent = BigUint.fromUInt(3u)

        assertEquals(BigUint.fromUInt(125u), base.pow(exponent))
    }
}
