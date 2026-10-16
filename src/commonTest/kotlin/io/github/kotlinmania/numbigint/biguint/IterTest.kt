// port-lint: tests biguint/iter.rs
package io.github.kotlinmania.numbigint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class IterTest {
    @Test
    fun testIterU32Digits() {
        var n = BigUint.fromUInt(5u)
        var it = n.iterU32Digits()
        assertEquals(1, it.len())
        assertEquals(5u, it.nextOrNull())
        assertEquals(0, it.len())
        assertNull(it.nextOrNull())
        assertEquals(0, it.len())
        assertNull(it.nextOrNull())

        n = BigUint.fromULong(112500000000uL)
        it = n.iterU32Digits()
        assertEquals(2, it.len())
        assertEquals(830850304u, it.nextOrNull())
        assertEquals(1, it.len())
        assertEquals(26u, it.nextOrNull())
        assertEquals(0, it.len())
        assertNull(it.nextOrNull())
    }

    @Test
    fun testIterU64Digits() {
        var n = BigUint.fromUInt(5u)
        var it = n.iterU64Digits()
        assertEquals(1, it.len())
        assertEquals(5uL, it.nextOrNull())
        assertEquals(0, it.len())
        assertNull(it.nextOrNull())
        assertEquals(0, it.len())
        assertNull(it.nextOrNull())

        n = BigUint.fromStrRadix("18446744073709551616", 10u).getOrThrow()
        it = n.iterU64Digits()
        assertEquals(2, it.len())
        assertEquals(0uL, it.nextOrNull())
        assertEquals(1, it.len())
        assertEquals(1uL, it.nextOrNull())
        assertEquals(0, it.len())
        assertNull(it.nextOrNull())
    }

    @Test
    fun testIterU32DigitsBe() {
        var n = BigUint.fromUInt(5u)
        var it = n.iterU32Digits()
        assertEquals(1, it.len())
        assertEquals(5u, it.nextOrNull())
        assertEquals(0, it.len())
        assertNull(it.nextOrNull())
        assertEquals(0, it.len())
        assertNull(it.nextOrNull())

        n = BigUint.fromULong(112500000000uL)
        it = n.iterU32Digits()
        assertEquals(2, it.len())
        assertEquals(830850304u, it.nextOrNull())
        assertEquals(1, it.len())
        assertEquals(26u, it.nextOrNull())
        assertEquals(0, it.len())
        assertNull(it.nextOrNull())
    }

    @Test
    fun testIterU64DigitsBe() {
        var n = BigUint.fromUInt(5u)
        var it = n.iterU64Digits()
        assertEquals(1, it.len())
        assertEquals(5uL, it.nextBack())
        assertEquals(0, it.len())
        assertNull(it.nextOrNull())
        assertEquals(0, it.len())
        assertNull(it.nextOrNull())

        n = BigUint.fromStrRadix("18446744073709551616", 10u).getOrThrow()
        it = n.iterU64Digits()
        assertEquals(2, it.len())
        assertEquals(1uL, it.nextBack())
        assertEquals(1, it.len())
        assertEquals(0uL, it.nextBack())
        assertEquals(0, it.len())
        assertNull(it.nextOrNull())
    }
}
