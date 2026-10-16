// port-lint: tests biguint/convert.rs
package io.github.kotlinmania.numbigint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConvertTest {
    private fun UInt.pow(exp: UInt): UInt {
        var acc = 1u
        repeat(exp.toInt()) {
            acc *= this
        }
        return acc
    }

    private fun UInt.checkedPow(exp: UInt): UInt? {
        var acc = 1uL
        repeat(exp.toInt()) {
            acc *= this.toULong()
            if (acc > UInt.MAX_VALUE.toULong()) {
                return null
            }
        }
        return acc.toUInt()
    }

    @Test
    fun testRadixBases() {
        for (radix in 3u..<256u) {
            if (!radix.isPowerOfTwo()) {
                val (base, power) = getRadixBase(radix)
                val powerU32 = power.toUInt()
                assertEquals(radix.pow(powerU32), base)
                assertNull(radix.checkedPow(powerU32 + 1u))
            }
        }
    }

    @Test
    fun testHalfRadixBases() {
        for (radix in 3u..<256u) {
            if (!radix.isPowerOfTwo()) {
                val (base, power) = getHalfRadixBase(radix)
                val powerU32 = power.toUInt()
                assertEquals(radix.pow(powerU32), base)
                assertTrue(radix.pow(powerU32 + 1u) > BIG_DIGIT_HALF)
            }
        }
    }
}
