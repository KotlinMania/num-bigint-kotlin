// port-lint: tests biguint.rs
package io.github.kotlinmania.numbigint

import kotlin.test.Test
import kotlin.test.assertEquals

class BigUintUnitTest {
    @Test
    fun testFromSlice() {
        fun check(slice: List<UInt>, data: List<BigDigit>) {
            assertEquals(data, BigUint.fromSlice(slice).data)
        }
        check(listOf(1u), listOf(1u))
        check(listOf(0u, 0u, 0u), emptyList())
        check(listOf(1u, 2u, 0u, 0u), listOf(1u, 2u))
        check(listOf(0u, 0u, 1u, 2u), listOf(0u, 0u, 1u, 2u))
        check(listOf(0u, 0u, 1u, 2u, 0u, 0u), listOf(0u, 0u, 1u, 2u))
        check(listOf((-1).toUInt()), listOf((-1).toUInt()))
    }

    @Test
    fun testU32U128() {
        assertEquals(listOf(0u, 0u, 0u, 0u), u32FromU128(Pair(0uL, 0uL)))
        assertEquals(
            listOf(UInt.MAX_VALUE, UInt.MAX_VALUE, UInt.MAX_VALUE, UInt.MAX_VALUE),
            u32FromU128(Pair(ULong.MAX_VALUE, ULong.MAX_VALUE)),
        )

        assertEquals(listOf(0u, 0u, 0u, UInt.MAX_VALUE), u32FromU128(Pair(0uL, UInt.MAX_VALUE.toULong())))

        assertEquals(listOf(0u, 0u, UInt.MAX_VALUE, UInt.MAX_VALUE), u32FromU128(Pair(0uL, ULong.MAX_VALUE)))

        assertEquals(
            listOf(0u, 1u, 0u, UInt.MAX_VALUE - 1u),
            u32FromU128(Pair(1uL, ULong.MAX_VALUE + UInt.MAX_VALUE.toULong())),
        )

        assertEquals(listOf(0u, 2u, 1u, 0u), u32FromU128(Pair(2uL, 1uL shl 32)))
    }

    @Test
    fun testU128U32Roundtrip() {
        // roundtrips
        val values =
            listOf(
                Pair(0uL, 0uL),
                Pair(0uL, 1uL),
                Pair(2uL, ULong.MAX_VALUE - 2uL),
                Pair(0uL, UInt.MAX_VALUE.toULong()),
                Pair(0uL, ULong.MAX_VALUE),
                Pair(1uL, ULong.MAX_VALUE + UInt.MAX_VALUE.toULong()),
                Pair(ULong.MAX_VALUE, ULong.MAX_VALUE),
            )

        for (value in values) {
            val parts = u32FromU128(value)
            assertEquals(value, u32ToU128(parts[0], parts[1], parts[2], parts[3]))
        }
    }
}
