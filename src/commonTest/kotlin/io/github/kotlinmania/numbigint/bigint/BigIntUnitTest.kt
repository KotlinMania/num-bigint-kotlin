// port-lint: tests bigint.rs
package io.github.kotlinmania.numbigint

import kotlin.test.Test
import kotlin.test.assertEquals

class BigIntUnitTest {
    @Test
    fun testFromBiguint() {
        fun check(inpS: Sign, inpN: UInt, ansS: Sign, ansN: UInt) {
            val inp = BigUint.fromUInt(inpN)
            val ans = BigInt(ansS, BigUint.fromUInt(ansN))
            assertEquals(ans, BigInt.fromBiguint(inpS, inp))
        }
        check(Sign.Plus, 1u, Sign.Plus, 1u)
        check(Sign.Plus, 0u, Sign.NoSign, 0u)
        check(Sign.Minus, 1u, Sign.Minus, 1u)
        check(Sign.NoSign, 1u, Sign.NoSign, 0u)
    }

    @Test
    fun testFromSlice() {
        fun check(inpS: Sign, inpN: List<UInt>, ansS: Sign, ansN: List<UInt>) {
            val ans = BigInt.fromBiguint(ansS, BigUint.fromSlice(ansN))
            assertEquals(ans, BigInt.fromSlice(inpS, inpN))
        }
        check(Sign.Plus, listOf(1u), Sign.Plus, listOf(1u))
        check(Sign.Plus, listOf(0u, 0u, 0u), Sign.NoSign, emptyList())
        check(Sign.Minus, listOf(1u, 2u, 0u, 0u), Sign.Minus, listOf(1u, 2u))
        check(Sign.Minus, listOf(0u, 0u, 1u, 2u), Sign.Minus, listOf(0u, 0u, 1u, 2u))
        check(Sign.Minus, listOf(0u, 0u, 1u, 2u, 0u, 0u), Sign.Minus, listOf(0u, 0u, 1u, 2u))
        check(Sign.NoSign, listOf(1u, 2u), Sign.NoSign, emptyList())
        check(Sign.Minus, listOf((-1).toUInt()), Sign.Minus, listOf((-1).toUInt()))
    }

    @Test
    fun testAssignFromSlice() {
        fun check(inpS: Sign, inpN: List<UInt>, ansS: Sign, ansN: List<UInt>) {
            val a = BigInt.fromSlice(Sign.Minus, listOf(2627u, 0u, 9182_9812u, 42u))
            a.assignFromSlice(inpS, inpN)
            val ans = BigInt.fromBiguint(ansS, BigUint.fromSlice(ansN))
            assertEquals(ans, a)
        }
        check(Sign.Plus, listOf(1u), Sign.Plus, listOf(1u))
        check(Sign.Plus, listOf(0u, 0u, 0u), Sign.NoSign, emptyList())
        check(Sign.Minus, listOf(1u, 2u, 0u, 0u), Sign.Minus, listOf(1u, 2u))
        check(Sign.Minus, listOf(0u, 0u, 1u, 2u), Sign.Minus, listOf(0u, 0u, 1u, 2u))
        check(Sign.Minus, listOf(0u, 0u, 1u, 2u, 0u, 0u), Sign.Minus, listOf(0u, 0u, 1u, 2u))
        check(Sign.NoSign, listOf(1u, 2u), Sign.NoSign, emptyList())
        check(Sign.Minus, listOf((-1).toUInt()), Sign.Minus, listOf((-1).toUInt()))
    }
}
