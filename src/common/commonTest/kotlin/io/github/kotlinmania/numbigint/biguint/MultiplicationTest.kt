// port-lint: tests biguint/multiplication.rs
package io.github.kotlinmania.numbigint

import io.github.kotlinmania.numbigint.bigint.minus
import kotlin.test.Test
import kotlin.test.assertEquals

class MultiplicationTest {
    @Test
    fun testSubSign() {
        fun subSignI(a: List<BigDigit>, b: List<BigDigit>): BigInt {
            val (sign, value) = subSign(a, b)
            return BigInt.fromBiguint(sign, value)
        }

        val a = BigUint.fromStrRadix("265252859812191058636308480000000", 10u).getOrThrow()
        val b = BigUint.fromStrRadix("26525285981219105863630848000000", 10u).getOrThrow()
        val aI = BigInt.from(a.clone())
        val bI = BigInt.from(b.clone())

        assertEquals(aI - bI, subSignI(a.data, b.data))
        assertEquals(bI - aI, subSignI(b.data, a.data))
    }
}
