package io.github.kotlinmania.numbigint

import io.github.kotlinmania.numbigint.bigint.tryFromBigintToBiguint
import io.github.kotlinmania.numbigint.bigint.tryFromBigintToLong
import io.github.kotlinmania.numbigint.bigint.tryFromBigintToULong
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppleApiResultTest {
    @Test
    fun parsingExposesTypedValuesAndErrors() {
        val signed = BigInt.fromStrRadix("-42", 10u)
        assertEquals(BigInt.from(-42), signed.value)
        assertNull(signed.error)
        assertTrue(signed.isSuccess())
        assertFalse(signed.isFailure())
        assertEquals(signed.value, signed.getOrThrow())
        val unsigned = BigUint.fromStrRadix("42", 10u)
        assertEquals(BigUint.fromUInt(42u), unsigned.value)
        assertNull(unsigned.error)
        assertTrue(unsigned.isSuccess())
        assertFalse(unsigned.isFailure())
        assertEquals(unsigned.value, unsigned.getOrThrow())
    }

    @Test
    fun parseFailuresRetainTheirMessages() {
        val signed = BigInt.fromStrRadix("", 10u)
        assertNull(signed.value)
        assertNull(signed.getOrNull())
        assertTrue(signed.isFailure())
        assertFalse(signed.isSuccess())
        assertEquals("cannot parse integer from empty string", assertNotNull(signed.error).message)
        assertFailsWith<IllegalArgumentException> { signed.getOrThrow() }
        val unsigned = BigUint.fromStrRadix("invalid", 10u)
        assertNull(unsigned.value)
        assertNull(unsigned.getOrNull())
        assertTrue(unsigned.isFailure())
        assertFalse(unsigned.isSuccess())
        assertEquals("invalid digit found in string", assertNotNull(unsigned.error).message)
        assertFailsWith<IllegalArgumentException> { unsigned.getOrThrow() }
    }

    @Test
    fun checkedConversionsExposeTypedValuesAndOriginalInput() {
        val signed = BigInt.from(-42)
        assertEquals(-42L, tryFromBigintToLong(signed).value)
        val unsignedFailure = tryFromBigintToULong(signed)
        assertNull(unsignedFailure.value)
        assertTrue(unsignedFailure.isFailure())
        assertNotNull(unsignedFailure.error)
        assertFailsWith<IllegalArgumentException> { unsignedFailure.getOrThrow() }
        val bigUnsignedFailure = tryFromBigintToBiguint(signed)
        assertEquals(signed, assertNotNull(bigUnsignedFailure.error).intoOriginal())
        assertNull(bigUnsignedFailure.getOrNull())
        val positive = BigInt.from(42)
        assertEquals(42uL, tryFromBigintToULong(positive).getOrThrow())
        assertEquals(BigUint.fromUInt(42u), tryFromBigintToBiguint(positive).getOrThrow())
        val overflow = BigInt.fromStrRadix("9223372036854775808", 10u).getOrThrow()
        assertTrue(tryFromBigintToLong(overflow).isFailure())
        assertEquals(42u, BigUint.fromUInt(42u).tryToUInt().value)
        assertTrue(BigUint.fromULong(1uL shl 32).tryToUInt().isFailure())
    }
}
