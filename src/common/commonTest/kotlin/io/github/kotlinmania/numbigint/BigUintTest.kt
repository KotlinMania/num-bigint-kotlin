// port-lint: tests tests/biguint.rs
package io.github.kotlinmania.numbigint

import io.github.kotlinmania.numbigint.bigint.plus
import kotlin.math.E
import kotlin.math.PI
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun hash(x: Any): Int = x.hashCode()

// LEFT, RIGHT, AND, OR, XOR
private val BIT_TESTS: List<List<List<UInt>>> =
    listOf(
        listOf(emptyList(), emptyList(), emptyList(), emptyList(), emptyList()),
        listOf(listOf(1u, 0u, 1u), listOf(1u, 1u), listOf(1u), listOf(1u, 1u, 1u), listOf(0u, 1u, 1u)),
        listOf(listOf(1u, 0u, 1u), listOf(0u, 1u, 1u), listOf(0u, 0u, 1u), listOf(1u, 1u, 1u), listOf(1u, 1u)),
        listOf(
            listOf(268u, 482u, 17u),
            listOf(964u, 54u),
            listOf(260u, 34u),
            listOf(972u, 502u, 17u),
            listOf(712u, 468u, 17u),
        ),
    )

private fun toStrPairs(): List<Pair<BigUint, List<Pair<UInt, String>>>> {
    val bits = 32
    return listOf(
        Pair(
            BigUint.zero(),
            listOf(Pair(2u, "0"), Pair(3u, "0")),
        ),
        Pair(
            BigUint.fromSlice(listOf(0xffu)),
            listOf(
                Pair(2u, "11111111"),
                Pair(3u, "100110"),
                Pair(4u, "3333"),
                Pair(5u, "2010"),
                Pair(6u, "1103"),
                Pair(7u, "513"),
                Pair(8u, "377"),
                Pair(9u, "313"),
                Pair(10u, "255"),
                Pair(11u, "212"),
                Pair(12u, "193"),
                Pair(13u, "168"),
                Pair(14u, "143"),
                Pair(15u, "120"),
                Pair(16u, "ff"),
            ),
        ),
        Pair(
            BigUint.fromSlice(listOf(0xfffu)),
            listOf(
                Pair(2u, "111111111111"),
                Pair(4u, "333333"),
                Pair(16u, "fff"),
            ),
        ),
        Pair(
            BigUint.fromSlice(listOf(1u, 2u)),
            listOf(
                Pair(2u, "10${"0".repeat(bits - 1)}1"),
                Pair(4u, "2${"0".repeat(bits / 2 - 1)}1"),
                Pair(10u, "8589934593"),
                Pair(16u, "2${"0".repeat(bits / 4 - 1)}1"),
            ),
        ),
        Pair(
            BigUint.fromSlice(listOf(1u, 2u, 3u)),
            listOf(
                Pair(2u, "11${"0".repeat(bits - 2)}10${"0".repeat(bits - 1)}1"),
                Pair(4u, "3${"0".repeat(bits / 2 - 1)}2${"0".repeat(bits / 2 - 1)}1"),
                Pair(8u, "6000000000100000000001"),
                Pair(10u, "55340232229718589441"),
                Pair(16u, "3${"0".repeat(bits / 4 - 1)}2${"0".repeat(bits / 4 - 1)}1"),
            ),
        ),
    )
}

class BigUintTest {
    @Test
    fun testFromBytesBe() {
        fun check(s: String, result: String) {
            val b = BigUint.parseBytes(result.encodeToByteArray(), 10u)!!
            assertEquals(b, BigUint.fromBytesBe(s.encodeToByteArray().map { it.toUByte() }))
        }
        check("A", "65")
        check("AA", "16705")
        check("AB", "16706")
        check("Hello world!", "22405534230753963835153736737")
        assertEquals(BigUint.zero(), BigUint.fromBytesBe(emptyList()))
    }

    @Test
    fun testToBytesBe() {
        fun check(s: String, result: String) {
            val b = BigUint.parseBytes(result.encodeToByteArray(), 10u)!!
            assertEquals(s.encodeToByteArray().map { it.toUByte() }, b.toBytesBe())
        }
        check("A", "65")
        check("AA", "16705")
        check("AB", "16706")
        check("Hello world!", "22405534230753963835153736737")
        val b = BigUint.zero()
        assertEquals(listOf<UByte>(0u), b.toBytesBe())

        // Test with leading/trailing zero bytes and a full BigDigit of value 0
        val b2 = BigUint.fromStrRadix("00010000000000000200", 16u).getOrThrow()
        assertEquals(listOf<UByte>(1u, 0u, 0u, 0u, 0u, 0u, 0u, 2u, 0u), b2.toBytesBe())
    }

    @Test
    fun testFromBytesLe() {
        fun check(s: String, result: String) {
            val b = BigUint.parseBytes(result.encodeToByteArray(), 10u)!!
            assertEquals(b, BigUint.fromBytesLe(s.encodeToByteArray().map { it.toUByte() }))
        }
        check("A", "65")
        check("AA", "16705")
        check("BA", "16706")
        check("!dlrow olleH", "22405534230753963835153736737")
        assertEquals(BigUint.zero(), BigUint.fromBytesLe(emptyList()))
    }

    @Test
    fun testToBytesLe() {
        fun check(s: String, result: String) {
            val b = BigUint.parseBytes(result.encodeToByteArray(), 10u)!!
            assertEquals(s.encodeToByteArray().map { it.toUByte() }, b.toBytesLe())
        }
        check("A", "65")
        check("AA", "16705")
        check("BA", "16706")
        check("!dlrow olleH", "22405534230753963835153736737")
        val b = BigUint.zero()
        assertEquals(listOf<UByte>(0u), b.toBytesLe())

        // Test with leading/trailing zero bytes and a full BigDigit of value 0
        val b2 = BigUint.fromStrRadix("00010000000000000200", 16u).getOrThrow()
        assertEquals(listOf<UByte>(0u, 2u, 0u, 0u, 0u, 0u, 0u, 0u, 1u), b2.toBytesLe())
    }

    @Test
    fun testCmp() {
        val rawSlices: List<List<UInt>> =
            listOf(
                emptyList(),
                listOf(1u),
                listOf(2u),
                listOf(0u.inv()),
                listOf(0u, 1u),
                listOf(2u, 1u),
                listOf(1u, 1u, 1u),
            )
        val data = rawSlices.map { BigUint.fromSlice(it) }
        for ((i, ni) in data.withIndex()) {
            for ((j0, nj) in data.subList(i, data.size).withIndex()) {
                val j = j0 + i
                if (i == j) {
                    assertEquals(0, ni.compareTo(nj))
                    assertEquals(0, nj.compareTo(ni))
                    assertEquals(ni, nj)
                    assertFalse(ni != nj)
                    assertTrue(ni <= nj)
                    assertTrue(ni >= nj)
                    assertFalse(ni < nj)
                    assertFalse(ni > nj)
                } else {
                    assertTrue(ni.compareTo(nj) < 0)
                    assertTrue(nj.compareTo(ni) > 0)

                    assertFalse(ni == nj)
                    assertTrue(ni != nj)

                    assertTrue(ni <= nj)
                    assertFalse(ni >= nj)
                    assertTrue(ni < nj)
                    assertFalse(ni > nj)

                    assertFalse(nj <= ni)
                    assertTrue(nj >= ni)
                    assertFalse(nj < ni)
                    assertTrue(nj > ni)
                }
            }
        }
    }

    @Test
    fun testHash() {
        val a = BigUint.new(emptyList())
        val b = BigUint.new(listOf(0u))
        val c = BigUint.new(listOf(1u))
        val d = BigUint.new(listOf(1u, 0u, 0u, 0u, 0u, 0u))
        val e = BigUint.new(listOf(0u, 0u, 0u, 0u, 0u, 1u))
        assertEquals(hash(a), hash(b))
        assertNotEquals(hash(b), hash(c))
        assertEquals(hash(c), hash(d))
        assertNotEquals(hash(d), hash(e))
    }

    @Test
    fun testBitand() {
        for (elm in BIT_TESTS) {
            val a = BigUint.fromSlice(elm[0])
            val b = BigUint.fromSlice(elm[1])
            val c = BigUint.fromSlice(elm[2])

            assertOp(a, b, c, BigUint::clone, BigUint::clone) { x, y -> x bitAnd y }
            assertOp(b, a, c, BigUint::clone, BigUint::clone) { x, y -> x bitAnd y }
            assertAssignOp(a, b, c, BigUint::clone, BigUint::clone) { x, y -> x.bitAndAssign(y) }
            assertAssignOp(b, a, c, BigUint::clone, BigUint::clone) { x, y -> x.bitAndAssign(y) }
        }
    }

    @Test
    fun testBitor() {
        for (elm in BIT_TESTS) {
            val a = BigUint.fromSlice(elm[0])
            val b = BigUint.fromSlice(elm[1])
            val c = BigUint.fromSlice(elm[3])

            assertOp(a, b, c, BigUint::clone, BigUint::clone) { x, y -> x bitOr y }
            assertOp(b, a, c, BigUint::clone, BigUint::clone) { x, y -> x bitOr y }
            assertAssignOp(a, b, c, BigUint::clone, BigUint::clone) { x, y -> x.bitOrAssign(y) }
            assertAssignOp(b, a, c, BigUint::clone, BigUint::clone) { x, y -> x.bitOrAssign(y) }
        }
    }

    @Test
    fun testBitxor() {
        for (elm in BIT_TESTS) {
            val a = BigUint.fromSlice(elm[0])
            val b = BigUint.fromSlice(elm[1])
            val c = BigUint.fromSlice(elm[4])

            assertOp(a, b, c, BigUint::clone, BigUint::clone) { x, y -> x bitXor y }
            assertOp(b, a, c, BigUint::clone, BigUint::clone) { x, y -> x bitXor y }
            assertOp(a, c, b, BigUint::clone, BigUint::clone) { x, y -> x bitXor y }
            assertOp(c, a, b, BigUint::clone, BigUint::clone) { x, y -> x bitXor y }
            assertOp(b, c, a, BigUint::clone, BigUint::clone) { x, y -> x bitXor y }
            assertOp(c, b, a, BigUint::clone, BigUint::clone) { x, y -> x bitXor y }
            assertAssignOp(a, b, c, BigUint::clone, BigUint::clone) { x, y -> x.bitXorAssign(y) }
            assertAssignOp(b, a, c, BigUint::clone, BigUint::clone) { x, y -> x.bitXorAssign(y) }
            assertAssignOp(a, c, b, BigUint::clone, BigUint::clone) { x, y -> x.bitXorAssign(y) }
            assertAssignOp(c, a, b, BigUint::clone, BigUint::clone) { x, y -> x.bitXorAssign(y) }
            assertAssignOp(b, c, a, BigUint::clone, BigUint::clone) { x, y -> x.bitXorAssign(y) }
            assertAssignOp(c, b, a, BigUint::clone, BigUint::clone) { x, y -> x.bitXorAssign(y) }
        }
    }

    @Test
    fun testShl() {
        fun check(s: String, shift: Int, ans: String) {
            val buAssign = BigUint.fromStrRadix(s, 16u).getOrThrow()
            val bu = (buAssign.clone() shl shift).toStrRadix(16u)
            assertEquals(ans, bu)
            buAssign.shiftLeftAssign(shift.toULong())
            assertEquals(ans, buAssign.toStrRadix(16u))
        }

        check("0", 3, "0")
        check("1", 3, "8")

        check(
            "100000000000000010000000000000001",
            3,
            "800000000000000080000000000000008",
        )
        check(
            "10000000100000001",
            2,
            "40000000400000004",
        )
        check(
            "100010001",
            1,
            "200020002",
        )

        check(
            "4000000000000000",
            3,
            "20000000000000000",
        )
        check(
            "40000000",
            2,
            "100000000",
        )
        check(
            "4000",
            2,
            "10000",
        )

        check(
            "4000000000000000",
            67,
            "200000000000000000000000000000000",
        )
        check(
            "40000000",
            35,
            "20000000000000000",
        )
        check(
            "4000",
            19,
            "200000000",
        )

        check(
            "fedcba9876543210fedcba9876543210",
            4,
            "fedcba9876543210fedcba98765432100",
        )
        check(
            "88887777666655554444333322221111",
            16,
            "888877776666555544443333222211110000",
        )
    }

    @Test
    fun testShr() {
        fun check(s: String, shift: Int, ans: String) {
            val buAssign = BigUint.fromStrRadix(s, 16u).getOrThrow()
            val bu = (buAssign.clone() shr shift).toStrRadix(16u)
            assertEquals(ans, bu)
            buAssign.shiftRightAssign(shift.toULong())
            assertEquals(ans, buAssign.toStrRadix(16u))
        }

        check("0", 3, "0")
        check("f", 3, "1")

        check(
            "100000000000000010000000000000001",
            3,
            "20000000000000002000000000000000",
        )
        check(
            "10000000100000001",
            2,
            "4000000040000000",
        )
        check(
            "100010001",
            1,
            "80008000",
        )

        check(
            "200000000000000010000000000000001",
            67,
            "4000000000000000",
        )
        check(
            "20000000100000001",
            35,
            "40000000",
        )
        check(
            "200010001",
            19,
            "4000",
        )

        check(
            "10000000000000000",
            1,
            "8000000000000000",
        )
        check(
            "100000000",
            1,
            "80000000",
        )
        check(
            "10000",
            1,
            "8000",
        )
        check(
            "fedcba9876543210fedcba98765432100",
            4,
            "fedcba9876543210fedcba9876543210",
        )

        check(
            "888877776666555544443333222211110000",
            16,
            "88887777666655554444333322221111",
        )
    }

    @Test
    fun testConvertI64() {
        fun check(b1: BigUint, i: Long) {
            val b2 = BigUint.fromLong(i)!!
            assertEquals(b1, b2)
            assertEquals(i, b1.toLongOrNull())
        }

        check(BigUint.zero(), 0L)
        check(BigUint.one(), 1L)
        check(Long.MAX_VALUE.toBigUint()!!, Long.MAX_VALUE)

        check(BigUint.new(emptyList()), 0L)
        check(BigUint.new(listOf(1u)), 1L)
        check(BigUint.new(listOf(N1)), (1L shl 32) - 1L)
        check(BigUint.new(listOf(0u, 1u)), 1L shl 32)
        check(BigUint.new(listOf(N1, N1 shr 1)), Long.MAX_VALUE)

        assertNull(Long.MIN_VALUE.toBigUint())
        assertNull(BigUint.new(listOf(N1, N1)).toLongOrNull())
        assertNull(BigUint.new(listOf(0u, 0u, 1u)).toLongOrNull())
        assertNull(BigUint.new(listOf(N1, N1, N1)).toLongOrNull())
    }

    @Test
    fun testConvertI128() {
        fun check(b1: BigUint, i: Pair<ULong, ULong>) {
            val digits = u32FromU128(i)
            val b2 = BigUint.fromSlice(listOf(digits[3], digits[2], digits[1], digits[0]))
            assertEquals(b1, b2)
            assertTrue(b1.bits() <= 127uL)
        }

        check(BigUint.zero(), Pair(0uL, 0uL))
        check(BigUint.one(), Pair(0uL, 1uL))
        check(BigUint.new(listOf(N1, N1, N1, N1 shr 1)), Pair(Long.MAX_VALUE.toULong(), ULong.MAX_VALUE))

        check(BigUint.new(emptyList()), Pair(0uL, 0uL))
        check(BigUint.new(listOf(1u)), Pair(0uL, 1uL))
        check(BigUint.new(listOf(N1)), Pair(0uL, (1uL shl 32) - 1uL))
        check(BigUint.new(listOf(0u, 1u)), Pair(0uL, 1uL shl 32))
        check(BigUint.new(listOf(N1, N1, N1, N1 shr 1)), Pair(Long.MAX_VALUE.toULong(), ULong.MAX_VALUE))

        assertTrue(BigUint.new(listOf(N1, N1, N1, N1)).bits() > 127uL)
        assertTrue(BigUint.new(listOf(0u, 0u, 0u, 0u, 1u)).bits() > 127uL)
        assertTrue(BigUint.new(listOf(N1, N1, N1, N1, N1)).bits() > 127uL)
    }

    @Test
    fun testConvertU64() {
        fun check(b1: BigUint, u: ULong) {
            val b2 = BigUint.fromULong(u)
            assertEquals(b1, b2)
            assertEquals(u, b1.toULongOrNull())
        }

        check(BigUint.zero(), 0uL)
        check(BigUint.one(), 1uL)
        check(ULong.MIN_VALUE.toBigUint(), ULong.MIN_VALUE)
        check(ULong.MAX_VALUE.toBigUint(), ULong.MAX_VALUE)

        check(BigUint.new(emptyList()), 0uL)
        check(BigUint.new(listOf(1u)), 1uL)
        check(BigUint.new(listOf(N1)), (1uL shl 32) - 1uL)
        check(BigUint.new(listOf(0u, 1u)), 1uL shl 32)
        check(BigUint.new(listOf(N1, N1)), ULong.MAX_VALUE)

        assertNull(BigUint.new(listOf(0u, 0u, 1u)).toULongOrNull())
        assertNull(BigUint.new(listOf(N1, N1, N1)).toULongOrNull())
    }

    @Test
    fun testConvertU128() {
        fun check(b1: BigUint, u: Pair<ULong, ULong>) {
            val digits = u32FromU128(u)
            val b2 = BigUint.fromSlice(listOf(digits[3], digits[2], digits[1], digits[0]))
            assertEquals(b1, b2)
            val d = b1.toU32Digits()
            val reconstructed =
                u32ToU128(
                    d.getOrElse(3) { 0u },
                    d.getOrElse(2) { 0u },
                    d.getOrElse(1) { 0u },
                    d.getOrElse(0) { 0u },
                )
            assertEquals(u, reconstructed)
        }

        check(BigUint.zero(), Pair(0uL, 0uL))
        check(BigUint.one(), Pair(0uL, 1uL))
        check(BigUint.new(listOf(N1, N1, N1, N1)), Pair(ULong.MAX_VALUE, ULong.MAX_VALUE))

        check(BigUint.new(emptyList()), Pair(0uL, 0uL))
        check(BigUint.new(listOf(1u)), Pair(0uL, 1uL))
        check(BigUint.new(listOf(N1)), Pair(0uL, (1uL shl 32) - 1uL))
        check(BigUint.new(listOf(0u, 1u)), Pair(0uL, 1uL shl 32))
        check(BigUint.new(listOf(N1, N1, N1, N1)), Pair(ULong.MAX_VALUE, ULong.MAX_VALUE))

        assertTrue(BigUint.new(listOf(0u, 0u, 0u, 0u, 1u)).bits() > 128uL)
        assertTrue(BigUint.new(listOf(N1, N1, N1, N1, N1)).bits() > 128uL)
    }

    @Test
    fun testConvertF32() {
        fun check(b1: BigUint, f: Float) {
            val b2 = BigUint.fromFloat(f)!!
            assertEquals(b1, b2)
            assertEquals(f, b1.toFloatOrNull())
        }

        check(BigUint.zero(), 0.0f)
        check(BigUint.one(), 1.0f)
        check(BigUint.fromUInt(UShort.MAX_VALUE.toUInt()), 2.0f.pow(16) - 1.0f)
        check(BigUint.fromULong(1uL shl 32), 2.0f.pow(32))
        check(BigUint.fromSlice(listOf(0u, 0u, 1u)), 2.0f.pow(64))
        check(
            (BigUint.one() shl 100) + (BigUint.one() shl 123),
            (2.0f.pow(100) + 2.0f.pow(123)),
        )
        check(BigUint.one() shl 127, 2.0f.pow(127))
        check(
            BigUint.fromULong((1uL shl 24) - 1uL) shl (128 - 24),
            Float.fromBits(Float.MAX_VALUE.toRawBits()),
        )

        // keeping all 24 digits with the bits at different offsets to the BigDigits
        val x = 0b00000000101111011111011011011101u
        var f = Float.fromBits(x.toFloat().toRawBits())
        var b = BigUint.fromUInt(x)
        repeat(64) {
            check(b, f)
            f *= 2.0f
            b = b shl 1
        }

        // this number when rounded to f64 then f32 isn't the same as when rounded straight to f32
        val n = 0b0000000000111111111111111111111111011111111111111111111111111111uL
        val expectedF32 = Float.fromBits(0x5a7fffff)
        assertNotEquals(Float.fromBits(n.toDouble().toFloat().toRawBits()), expectedF32)
        assertEquals(expectedF32, BigUint.fromULong(n).toFloatOrNull())

        // test rounding up with the bits at different offsets to the BigDigits
        f = Float.fromBits(((1uL shl 25) - 1uL).toFloat().toRawBits())
        b = BigUint.fromULong(1uL shl 25)
        repeat(64) {
            assertEquals(f, b.toFloatOrNull())
            f *= 2.0f
            b = b shl 1
        }

        // test correct ties-to-even rounding
        val weird = (BigInt.one() shl 100) + (BigInt.one() shl (100 - 24))
        val weirdPlusOne = weird + 1u
        val weirdF32 = Float.fromBits((2.0f.pow(100) + 2.0f.pow(100 - 24)).toRawBits())
        val weirdPlusOneF32 = Float.fromBits(weirdF32.toRawBits() + 1)
        assertNotEquals(weirdF32, weirdPlusOneF32)

        assertEquals(weirdF32, weird.toFloatOrNull())
        assertEquals(weirdPlusOneF32, weirdPlusOne.toFloatOrNull())

        // rounding
        assertNull(BigUint.fromFloat(-1.0f))
        assertEquals(BigUint.zero(), BigUint.fromFloat(-0.99999f))
        assertEquals(BigUint.zero(), BigUint.fromFloat(-0.5f))
        assertEquals(BigUint.zero(), BigUint.fromFloat(-0.0f))
        assertEquals(BigUint.zero(), BigUint.fromFloat(Float.MIN_VALUE / 2.0f))
        assertEquals(BigUint.zero(), BigUint.fromFloat(Float.fromBits(0x00800000)))
        assertEquals(BigUint.zero(), BigUint.fromFloat(0.5f))
        assertEquals(BigUint.zero(), BigUint.fromFloat(0.99999f))
        assertEquals(BigUint.fromUInt(2u), BigUint.fromFloat(E.toFloat()))
        assertEquals(BigUint.fromUInt(3u), BigUint.fromFloat(PI.toFloat()))

        // special float values
        assertNull(BigUint.fromFloat(Float.NaN))
        assertNull(BigUint.fromFloat(Float.POSITIVE_INFINITY))
        assertNull(BigUint.fromFloat(Float.NEGATIVE_INFINITY))
        assertNull(BigUint.fromFloat(-Float.MAX_VALUE))

        // largest BigUint that will round to a finite f32 value
        val bigNum = (BigUint.one() shl 128) - 1u - (BigUint.one() shl (128 - 25))
        assertEquals(Float.fromBits(Float.MAX_VALUE.toRawBits()), bigNum.toFloatOrNull())
        assertEquals(Float.POSITIVE_INFINITY, (bigNum + 1u).toFloatOrNull())

        assertEquals(Float.POSITIVE_INFINITY, ((BigUint.one() shl 128) - 1u).toFloatOrNull())
        assertEquals(Float.POSITIVE_INFINITY, (BigUint.one() shl 128).toFloatOrNull())
    }

    @Test
    fun testConvertF64() {
        fun check(b1: BigUint, f: Double) {
            val b2 = BigUint.fromDouble(f)!!
            assertEquals(b1, b2)
            assertEquals(f, b1.toDoubleOrNull())
        }

        check(BigUint.zero(), 0.0)
        check(BigUint.one(), 1.0)
        check(BigUint.fromUInt(UInt.MAX_VALUE), 2.0.pow(32) - 1.0)
        check(BigUint.fromULong(1uL shl 32), 2.0.pow(32))
        check(BigUint.fromSlice(listOf(0u, 0u, 1u)), 2.0.pow(64))
        check(
            (BigUint.one() shl 100) + (BigUint.one() shl 152),
            2.0.pow(100) + 2.0.pow(152),
        )
        check(BigUint.one() shl 1023, 2.0.pow(1023))
        check(BigUint.fromULong((1uL shl 53) - 1uL) shl (1024 - 53), Double.MAX_VALUE)

        // keeping all 53 digits with the bits at different offsets to the BigDigits
        val x = 0b0000000000011110111110110111111101110111101111011111011011011101uL
        var f = x.toDouble()
        var b = BigUint.fromULong(x)
        repeat(128) {
            check(b, f)
            f *= 2.0
            b = b shl 1
        }

        // test rounding up with the bits at different offsets to the BigDigits
        f = ((1uL shl 54) - 1uL).toDouble()
        b = BigUint.fromULong(1uL shl 54)
        repeat(128) {
            assertEquals(f, b.toDoubleOrNull())
            f *= 2.0
            b = b shl 1
        }

        // test correct ties-to-even rounding
        val weird = (BigInt.one() shl 100) + (BigInt.one() shl (100 - 53))
        val weirdPlusOne = weird + 1u
        val weirdF64 = 2.0.pow(100) + 2.0.pow(100 - 53)
        val weirdPlusOneF64 = Double.fromBits(weirdF64.toRawBits() + 1L)
        assertNotEquals(weirdF64, weirdPlusOneF64)

        assertEquals(weirdF64, weird.toDoubleOrNull())
        assertEquals(weirdPlusOneF64, weirdPlusOne.toDoubleOrNull())

        // rounding
        assertNull(BigUint.fromDouble(-1.0))
        assertEquals(BigUint.zero(), BigUint.fromDouble(-0.99999))
        assertEquals(BigUint.zero(), BigUint.fromDouble(-0.5))
        assertEquals(BigUint.zero(), BigUint.fromDouble(-0.0))
        assertEquals(BigUint.zero(), BigUint.fromDouble(Double.MIN_VALUE / 2.0))
        assertEquals(BigUint.zero(), BigUint.fromDouble(Double.fromBits(0x0010000000000000L)))
        assertEquals(BigUint.zero(), BigUint.fromDouble(0.5))
        assertEquals(BigUint.zero(), BigUint.fromDouble(0.99999))
        assertEquals(BigUint.fromUInt(2u), BigUint.fromDouble(E))
        assertEquals(BigUint.fromUInt(3u), BigUint.fromDouble(PI))

        // special float values
        assertNull(BigUint.fromDouble(Double.NaN))
        assertNull(BigUint.fromDouble(Double.POSITIVE_INFINITY))
        assertNull(BigUint.fromDouble(Double.NEGATIVE_INFINITY))
        assertNull(BigUint.fromDouble(-Double.MAX_VALUE))

        // largest BigUint that will round to a finite f64 value
        val bigNum = (BigUint.one() shl 1024) - 1u - (BigUint.one() shl (1024 - 54))
        assertEquals(Double.MAX_VALUE, bigNum.toDoubleOrNull())
        assertEquals(Double.POSITIVE_INFINITY, (bigNum + 1u).toDoubleOrNull())

        assertEquals(Double.POSITIVE_INFINITY, ((BigUint.one() shl 1024) - 1u).toDoubleOrNull())
        assertEquals(Double.POSITIVE_INFINITY, (BigUint.one() shl 1024).toDoubleOrNull())
    }

    @Test
    fun testConvertToBigint() {
        fun check(n: BigUint, ans: BigInt) {
            assertEquals(ans, n.toBigInt())
            assertEquals(n, n.toBigInt().toBigUint())
        }
        check(BigUint.zero(), BigInt.zero())
        check(
            BigUint.new(listOf(1u, 2u, 3u)),
            BigInt.fromBiguint(Sign.Plus, BigUint.new(listOf(1u, 2u, 3u))),
        )
    }

    @Test
    fun testConvertFromUint() {
        assertEquals(BigUint.zero(), BigUint.fromUInt(0u))
        assertEquals(BigUint.one(), BigUint.fromUInt(1u))
        assertEquals(
            BigUint.fromSlice(listOf(UByte.MAX_VALUE.toUInt())) - BigUint.one(),
            BigUint.fromUInt(UByte.MAX_VALUE - 1u),
        )
        assertEquals(
            BigUint.fromSlice(listOf(UByte.MAX_VALUE.toUInt())),
            BigUint.fromUInt(UByte.MAX_VALUE.toUInt()),
        )

        assertEquals(
            BigUint.fromSlice(listOf(UShort.MAX_VALUE.toUInt())) - BigUint.one(),
            BigUint.fromUInt(UShort.MAX_VALUE - 1u),
        )
        assertEquals(
            BigUint.fromSlice(listOf(UShort.MAX_VALUE.toUInt())),
            BigUint.fromUInt(UShort.MAX_VALUE.toUInt()),
        )

        assertEquals(
            BigUint.fromSlice(listOf(UInt.MAX_VALUE)) - BigUint.one(),
            BigUint.fromUInt(UInt.MAX_VALUE - 1u),
        )
        assertEquals(
            BigUint.fromSlice(listOf(UInt.MAX_VALUE)),
            BigUint.fromUInt(UInt.MAX_VALUE),
        )

        assertEquals(BigUint.zero(), BigUint.fromULong(0uL))
        assertEquals(BigUint.one(), BigUint.fromULong(1uL))
        assertEquals(
            BigUint.fromSlice(listOf(UInt.MAX_VALUE, UInt.MAX_VALUE)) - BigUint.one(),
            BigUint.fromULong(ULong.MAX_VALUE - 1uL),
        )
        assertEquals(
            BigUint.fromSlice(listOf(UInt.MAX_VALUE, UInt.MAX_VALUE)),
            BigUint.fromULong(ULong.MAX_VALUE),
        )
    }

    @Test
    fun testAdd() {
        for (elm in SUM_TRIPLES) {
            val (aVec, bVec, cVec) = elm
            val a = BigUint.fromSlice(aVec)
            val b = BigUint.fromSlice(bVec)
            val c = BigUint.fromSlice(cVec)

            assertOp(a, b, c, BigUint::clone, BigUint::clone) { x, y -> x + y }
            assertOp(b, a, c, BigUint::clone, BigUint::clone) { x, y -> x + y }
            assertAssignOp(a, b, c, BigUint::clone, BigUint::clone) { x, y -> x.plusAssign(y) }
            assertAssignOp(b, a, c, BigUint::clone, BigUint::clone) { x, y -> x.plusAssign(y) }
        }
    }

    @Test
    fun testSub() {
        for (elm in SUM_TRIPLES) {
            val (aVec, bVec, cVec) = elm
            val a = BigUint.fromSlice(aVec)
            val b = BigUint.fromSlice(bVec)
            val c = BigUint.fromSlice(cVec)

            assertOp(c, a, b, BigUint::clone, BigUint::clone) { x, y -> x - y }
            assertOp(c, b, a, BigUint::clone, BigUint::clone) { x, y -> x - y }
            assertAssignOp(c, a, b, BigUint::clone, BigUint::clone) { x, y -> x.minusAssign(y) }
            assertAssignOp(c, b, a, BigUint::clone, BigUint::clone) { x, y -> x.minusAssign(y) }
        }
    }

    @Test
    fun testSubFailOnUnderflow() {
        val a = BigUint.zero()
        val b = BigUint.one()
        assertFailsWith<IllegalArgumentException> {
            a - b
        }
    }

    @Test
    fun testMul() {
        for (elm in MUL_TRIPLES) {
            val (aVec, bVec, cVec) = elm
            val a = BigUint.fromSlice(aVec)
            val b = BigUint.fromSlice(bVec)
            val c = BigUint.fromSlice(cVec)

            assertOp(a, b, c, BigUint::clone, BigUint::clone) { x, y -> x * y }
            assertOp(b, a, c, BigUint::clone, BigUint::clone) { x, y -> x * y }
            assertAssignOp(a, b, c, BigUint::clone, BigUint::clone) { x, y -> x.timesAssign(y) }
            assertAssignOp(b, a, c, BigUint::clone, BigUint::clone) { x, y -> x.timesAssign(y) }
        }

        for (elm in DIV_REM_QUADRUPLES) {
            val a = BigUint.fromSlice(elm[0])
            val b = BigUint.fromSlice(elm[1])
            val c = BigUint.fromSlice(elm[2])
            val d = BigUint.fromSlice(elm[3])

            assertEquals(a, b * c + d)
            assertEquals(a, c * b + d)
        }
    }

    @Test
    fun testDivRem() {
        for (elm in MUL_TRIPLES) {
            val (aVec, bVec, cVec) = elm
            val a = BigUint.fromSlice(aVec)
            val b = BigUint.fromSlice(bVec)
            val c = BigUint.fromSlice(cVec)

            if (!a.isZero()) {
                assertOp(c, a, b, BigUint::clone, BigUint::clone) { x, y -> x / y }
                assertOp(c, a, BigUint.zero(), BigUint::clone, BigUint::clone) { x, y -> x % y }
                assertAssignOp(c, a, b, BigUint::clone, BigUint::clone) { x, y -> x.divAssign(y) }
                assertAssignOp(c, a, BigUint.zero(), BigUint::clone, BigUint::clone) { x, y -> x.remAssign(y) }
                assertEquals(Pair(b.clone(), BigUint.zero()), c.divRem(a))
            }
            if (!b.isZero()) {
                assertOp(c, b, a, BigUint::clone, BigUint::clone) { x, y -> x / y }
                assertOp(c, b, BigUint.zero(), BigUint::clone, BigUint::clone) { x, y -> x % y }
                assertAssignOp(c, b, a, BigUint::clone, BigUint::clone) { x, y -> x.divAssign(y) }
                assertAssignOp(c, b, BigUint.zero(), BigUint::clone, BigUint::clone) { x, y -> x.remAssign(y) }
                assertEquals(Pair(a.clone(), BigUint.zero()), c.divRem(b))
            }
        }

        for (elm in DIV_REM_QUADRUPLES) {
            val a = BigUint.fromSlice(elm[0])
            val b = BigUint.fromSlice(elm[1])
            val c = BigUint.fromSlice(elm[2])
            val d = BigUint.fromSlice(elm[3])

            if (!b.isZero()) {
                assertOp(a, b, c, BigUint::clone, BigUint::clone) { x, y -> x / y }
                assertOp(a, b, d, BigUint::clone, BigUint::clone) { x, y -> x % y }
                assertAssignOp(a, b, c, BigUint::clone, BigUint::clone) { x, y -> x.divAssign(y) }
                assertAssignOp(a, b, d, BigUint::clone, BigUint::clone) { x, y -> x.remAssign(y) }
                assertEquals(Pair(c, d), a.divRem(b))
            }
        }
    }

    @Test
    fun testDivRemBigMultiple() {
        val a = BigUint.fromUInt(3u).pow(100u)
        val a2 = a * a

        val (div, rem) = a2.divRem(a)
        assertEquals(a, div)
        assertTrue(rem.isZero())

        val (div2, rem2) = (a2 - 1u).divRem(a)
        assertEquals(a - 1u, div2)
        assertEquals(a - 1u, rem2)
    }

    @Test
    fun testDivCeil() {
        fun check(a: BigUint, b: BigUint, d: BigUint, m: BigUint) {
            if (m.isZero()) {
                assertEquals(d, a.divCeil(b))
            } else {
                assertEquals(d + 1u, a.divCeil(b))
            }
        }

        for (elm in MUL_TRIPLES) {
            val (aVec, bVec, cVec) = elm
            val a = BigUint.fromSlice(aVec)
            val b = BigUint.fromSlice(bVec)
            val c = BigUint.fromSlice(cVec)

            if (!a.isZero()) {
                check(c, a, b, BigUint.zero())
            }
            if (!b.isZero()) {
                check(c, b, a, BigUint.zero())
            }
        }

        for (elm in DIV_REM_QUADRUPLES) {
            val a = BigUint.fromSlice(elm[0])
            val b = BigUint.fromSlice(elm[1])
            val c = BigUint.fromSlice(elm[2])
            val d = BigUint.fromSlice(elm[3])

            if (!b.isZero()) {
                check(a, b, c, d)
            }
        }
    }

    @Test
    fun testDivRemEuclid() {
        fun check(a: BigUint, b: BigUint, d: BigUint, m: BigUint) {
            assertEquals(d, a.divEuclid(b))
            assertEquals(m, a.remEuclid(b))
        }

        for (elm in MUL_TRIPLES) {
            val (aVec, bVec, cVec) = elm
            val a = BigUint.fromSlice(aVec)
            val b = BigUint.fromSlice(bVec)
            val c = BigUint.fromSlice(cVec)

            if (!a.isZero()) {
                check(c, a, b, BigUint.zero())
            }
            if (!b.isZero()) {
                check(c, b, a, BigUint.zero())
            }
        }

        for (elm in DIV_REM_QUADRUPLES) {
            val a = BigUint.fromSlice(elm[0])
            val b = BigUint.fromSlice(elm[1])
            val c = BigUint.fromSlice(elm[2])
            val d = BigUint.fromSlice(elm[3])

            if (!b.isZero()) {
                check(a, b, c, d)
            }
        }
    }

    @Test
    fun testCheckedAdd() {
        for (elm in SUM_TRIPLES) {
            val (aVec, bVec, cVec) = elm
            val a = BigUint.fromSlice(aVec)
            val b = BigUint.fromSlice(bVec)
            val c = BigUint.fromSlice(cVec)

            assertEquals(c, a.checkedAdd(b))
            assertEquals(c, b.checkedAdd(a))
        }
    }

    @Test
    fun testCheckedSub() {
        for (elm in SUM_TRIPLES) {
            val (aVec, bVec, cVec) = elm
            val a = BigUint.fromSlice(aVec)
            val b = BigUint.fromSlice(bVec)
            val c = BigUint.fromSlice(cVec)

            assertEquals(b, c.checkedSub(a))
            assertEquals(a, c.checkedSub(b))

            if (a > c) {
                assertNull(a.checkedSub(c))
            }
            if (b > c) {
                assertNull(b.checkedSub(c))
            }
        }
    }

    @Test
    fun testCheckedMul() {
        for (elm in MUL_TRIPLES) {
            val (aVec, bVec, cVec) = elm
            val a = BigUint.fromSlice(aVec)
            val b = BigUint.fromSlice(bVec)
            val c = BigUint.fromSlice(cVec)

            assertEquals(c, a.checkedMul(b))
            assertEquals(c, b.checkedMul(a))
        }

        for (elm in DIV_REM_QUADRUPLES) {
            val a = BigUint.fromSlice(elm[0])
            val b = BigUint.fromSlice(elm[1])
            val c = BigUint.fromSlice(elm[2])
            val d = BigUint.fromSlice(elm[3])

            assertEquals(a, b.checkedMul(c)!! + d)
            assertEquals(a, c.checkedMul(b)!! + d)
        }
    }

    @Test
    fun testMulOverflow() {
        val s =
            "5311379928167670986895882065524686273295931177270319231994441382" +
                "0040355986085224273916250223263671004753755210595137000079652876" +
                "0829212940754539968588340162273730474622005920097370111"
        val a = BigUint.fromStrRadix(s, 10u).getOrThrow()
        val b = a.clone()
        a.checkedMul(b)
    }

    @Test
    fun testMulOverflow2() {
        for (i in 1..12) {
            val bits = 1u shl i
            val x = (BigUint.one() shl bits.toInt()) - 1u
            val x2 = (BigUint.one() shl (2 * bits.toInt())) - x - x - 1u
            assertEquals(x2, x * x)
        }
    }

    @Test
    fun testCheckedDiv() {
        for (elm in MUL_TRIPLES) {
            val (aVec, bVec, cVec) = elm
            val a = BigUint.fromSlice(aVec)
            val b = BigUint.fromSlice(bVec)
            val c = BigUint.fromSlice(cVec)

            if (!a.isZero()) {
                assertEquals(b, c.checkedDiv(a))
            }
            if (!b.isZero()) {
                assertEquals(a, c.checkedDiv(b))
            }

            assertNull(c.checkedDiv(BigUint.zero()))
        }
    }

    @Test
    fun testGcd() {
        fun check(a: UInt, b: UInt, c: UInt) {
            val bigA = BigUint.fromUInt(a)
            val bigB = BigUint.fromUInt(b)
            val bigC = BigUint.fromUInt(c)

            assertEquals(bigC, bigA.gcd(bigB))
            assertEquals(bigC, bigA.gcdLcm(bigB).first)
        }

        check(10u, 2u, 2u)
        check(10u, 3u, 1u)
        check(0u, 3u, 3u)
        check(3u, 3u, 3u)
        check(56u, 42u, 14u)
    }

    @Test
    fun testLcm() {
        fun check(a: UInt, b: UInt, c: UInt) {
            val bigA = BigUint.fromUInt(a)
            val bigB = BigUint.fromUInt(b)
            val bigC = BigUint.fromUInt(c)

            assertEquals(bigC, bigA.lcm(bigB))
            assertEquals(bigC, bigA.gcdLcm(bigB).second)
        }

        check(0u, 0u, 0u)
        check(1u, 0u, 0u)
        check(0u, 1u, 0u)
        check(1u, 1u, 1u)
        check(8u, 9u, 72u)
        check(11u, 5u, 55u)
        check(99u, 17u, 1683u)
    }

    @Test
    fun testIsMultipleOf() {
        assertTrue(BigUint.fromUInt(0u).isMultipleOf(BigUint.fromUInt(0u)))
        assertTrue(BigUint.fromUInt(6u).isMultipleOf(BigUint.fromUInt(6u)))
        assertTrue(BigUint.fromUInt(6u).isMultipleOf(BigUint.fromUInt(3u)))
        assertTrue(BigUint.fromUInt(6u).isMultipleOf(BigUint.fromUInt(1u)))

        assertFalse(BigUint.fromUInt(42u).isMultipleOf(BigUint.fromUInt(5u)))
        assertFalse(BigUint.fromUInt(5u).isMultipleOf(BigUint.fromUInt(3u)))
        assertFalse(BigUint.fromUInt(42u).isMultipleOf(BigUint.fromUInt(0u)))
    }

    @Test
    fun testNextMultipleOf() {
        assertEquals(
            BigUint.fromUInt(16u),
            BigUint.fromUInt(16u).nextMultipleOf(BigUint.fromUInt(8u)),
        )
        assertEquals(
            BigUint.fromUInt(24u),
            BigUint.fromUInt(23u).nextMultipleOf(BigUint.fromUInt(8u)),
        )
    }

    @Test
    fun testPrevMultipleOf() {
        assertEquals(
            BigUint.fromUInt(16u),
            BigUint.fromUInt(16u).prevMultipleOf(BigUint.fromUInt(8u)),
        )
        assertEquals(
            BigUint.fromUInt(16u),
            BigUint.fromUInt(23u).prevMultipleOf(BigUint.fromUInt(8u)),
        )
    }

    @Test
    fun testIsEven() {
        val one = BigUint.fromStrRadix("1", 10u).getOrThrow()
        val two = BigUint.fromStrRadix("2", 10u).getOrThrow()
        val thousand = BigUint.fromStrRadix("1000", 10u).getOrThrow()
        val big = BigUint.fromStrRadix("1000000000000000000000", 10u).getOrThrow()
        val bigger = BigUint.fromStrRadix("1000000000000000000001", 10u).getOrThrow()
        assertTrue(one.isOdd())
        assertTrue(two.isEven())
        assertTrue(thousand.isEven())
        assertTrue(big.isEven())
        assertTrue(bigger.isOdd())
        assertTrue((one shl 64).isEven())
        assertTrue(((one shl 64) + one).isOdd())
    }

    @Test
    fun testToStrRadix() {
        val r = toStrPairs()
        for ((n, rs) in r) {
            for ((radix, str) in rs) {
                assertEquals(str, n.toStrRadix(radix))
            }
        }
    }

    @Test
    fun testFromAndToRadix() {
        val groundTruth: List<Triple<String, UInt, List<Int>>> =
            listOf(
                Triple("0", 42u, listOf(0)),
                Triple(
                    "ffffeeffbb",
                    2u,
                    listOf(1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1) +
                        listOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
                ),
                Triple(
                    "ffffeeffbb",
                    3u,
                    listOf(2, 2, 1, 1, 2, 1, 1, 2, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 1, 0, 0, 2, 2, 0, 1),
                ),
                Triple(
                    "ffffeeffbb",
                    4u,
                    listOf(3, 2, 3, 2, 3, 3, 3, 3, 2, 3, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3),
                ),
                Triple(
                    "ffffeeffbb",
                    5u,
                    listOf(0, 4, 3, 3, 1, 4, 2, 4, 1, 4, 4, 2, 3, 0, 0, 1, 2, 1),
                ),
                Triple(
                    "ffffeeffbb",
                    6u,
                    listOf(5, 5, 4, 5, 5, 0, 0, 1, 2, 5, 3, 0, 1, 0, 2, 2),
                ),
                Triple(
                    "ffffeeffbb",
                    7u,
                    listOf(4, 2, 3, 6, 0, 1, 6, 1, 6, 2, 0, 3, 2, 4, 1),
                ),
                Triple(
                    "ffffeeffbb",
                    8u,
                    listOf(3, 7, 6, 7, 7, 5, 3, 7, 7, 7, 7, 7, 7, 1),
                ),
                Triple("ffffeeffbb", 9u, listOf(8, 4, 5, 7, 0, 0, 3, 2, 0, 3, 0, 8, 3)),
                Triple("ffffeeffbb", 10u, listOf(5, 9, 5, 3, 1, 5, 0, 1, 5, 9, 9, 0, 1)),
                Triple("ffffeeffbb", 11u, listOf(10, 7, 6, 5, 2, 0, 3, 3, 3, 4, 9, 3)),
                Triple("ffffeeffbb", 12u, listOf(11, 8, 5, 10, 1, 10, 3, 1, 1, 9, 5, 1)),
                Triple("ffffeeffbb", 13u, listOf(0, 5, 7, 4, 6, 5, 6, 11, 8, 12, 7)),
                Triple("ffffeeffbb", 14u, listOf(11, 4, 4, 11, 8, 4, 6, 0, 3, 11, 3)),
                Triple("ffffeeffbb", 15u, listOf(5, 11, 13, 2, 1, 10, 2, 0, 9, 13, 1)),
                Triple("ffffeeffbb", 16u, listOf(11, 11, 15, 15, 14, 14, 15, 15, 15, 15)),
                Triple("ffffeeffbb", 17u, listOf(0, 2, 14, 12, 2, 14, 8, 10, 4, 9)),
                Triple("ffffeeffbb", 18u, listOf(17, 15, 5, 13, 10, 16, 16, 13, 9, 5)),
                Triple("ffffeeffbb", 19u, listOf(14, 13, 2, 8, 9, 0, 1, 14, 7, 3)),
                Triple("ffffeeffbb", 20u, listOf(15, 19, 3, 14, 0, 17, 19, 18, 2, 2)),
                Triple("ffffeeffbb", 21u, listOf(11, 5, 4, 13, 5, 18, 9, 1, 8, 1)),
                Triple("ffffeeffbb", 22u, listOf(21, 3, 7, 21, 15, 12, 17, 0, 20)),
                Triple("ffffeeffbb", 23u, listOf(21, 21, 6, 9, 10, 7, 21, 0, 14)),
                Triple("ffffeeffbb", 24u, listOf(11, 10, 19, 14, 22, 11, 17, 23, 9)),
                Triple("ffffeeffbb", 25u, listOf(20, 18, 21, 22, 21, 14, 3, 5, 7)),
                Triple("ffffeeffbb", 26u, listOf(13, 15, 24, 11, 17, 6, 23, 6, 5)),
                Triple("ffffeeffbb", 27u, listOf(17, 16, 7, 0, 21, 0, 3, 24, 3)),
                Triple("ffffeeffbb", 28u, listOf(11, 16, 11, 15, 14, 18, 13, 25, 2)),
                Triple("ffffeeffbb", 29u, listOf(6, 8, 7, 19, 14, 13, 21, 5, 2)),
                Triple("ffffeeffbb", 30u, listOf(5, 13, 18, 11, 10, 7, 8, 20, 1)),
                Triple("ffffeeffbb", 31u, listOf(22, 26, 15, 19, 8, 27, 29, 8, 1)),
                Triple("ffffeeffbb", 32u, listOf(27, 29, 31, 29, 30, 31, 31, 31)),
                Triple("ffffeeffbb", 33u, listOf(32, 20, 27, 12, 1, 12, 26, 25)),
                Triple("ffffeeffbb", 34u, listOf(17, 9, 16, 33, 13, 25, 31, 20)),
                Triple("ffffeeffbb", 35u, listOf(25, 32, 2, 25, 11, 4, 3, 17)),
                Triple("ffffeeffbb", 36u, listOf(35, 34, 5, 6, 32, 3, 1, 14)),
                Triple("ffffeeffbb", 100u, listOf(95, 35, 51, 10, 95, 9, 1)),
                Triple("ffffeeffbb", 128u, listOf(59, 127, 59, 127, 127, 31)),
                Triple("ffffeeffbb", 255u, listOf(170, 225, 247, 9, 5, 1)),
                Triple("ffffeeffbb", 256u, listOf(187, 255, 238, 255, 255)),
            )

        for ((hexStr, radix, leInts) in groundTruth) {
            val inbaseRadixLe = leInts.map { it.toUByte() }
            val bigint = BigUint.parseBytes(hexStr.encodeToByteArray(), 16u)!!
            assertEquals(inbaseRadixLe, bigint.toRadixLe(radix))
            val inbaseBe = bigint.toRadixBe(radix).asReversed()
            assertEquals(inbaseRadixLe, inbaseBe)
            assertEquals(bigint, BigUint.fromRadixLe(inbaseRadixLe, radix))
            val inbaseRadixBe = inbaseRadixLe.asReversed()
            assertEquals(bigint, BigUint.fromRadixBe(inbaseRadixBe, radix))
        }

        val fullSweep = BigUint.parseBytes("ffffeeffbb".encodeToByteArray(), 16u)!!
        for (radix in 2u..256u) {
            val le = fullSweep.toRadixLe(radix)
            val be = fullSweep.toRadixBe(radix)
            assertEquals(le, be.asReversed())
            assertEquals(fullSweep, BigUint.fromRadixLe(le, radix))
            assertEquals(fullSweep, BigUint.fromRadixBe(be, radix))
        }

        assertNull(BigUint.fromRadixLe(listOf<UByte>(10u, 100u, 10u), 50u))
        assertEquals(BigUint.zero(), BigUint.fromRadixLe(emptyList(), 2u))
        assertEquals(BigUint.zero(), BigUint.fromRadixBe(emptyList(), 2u))
    }

    @Test
    fun testFromStrRadix() {
        val r = toStrPairs()
        for ((n, rs) in r) {
            for ((radix, str) in rs) {
                assertEquals(n, BigUint.fromStrRadix(str, radix).getOrThrow())
            }
        }

        assertNull(BigUint.fromStrRadix("Z", 10u).getOrNull())
        assertNull(BigUint.fromStrRadix("_", 2u).getOrNull())
        assertNull(BigUint.fromStrRadix("_1", 2u).getOrNull())
        assertEquals(BigUint.fromSlice(listOf(1u)), BigUint.fromStrRadix("+1", 10u).getOrNull())
        assertNull(BigUint.fromStrRadix("++1", 10u).getOrNull())
        assertNull(BigUint.fromStrRadix("-1", 10u).getOrNull())
        assertNull(BigUint.fromStrRadix("0+2", 10u).getOrNull())
        assertEquals(BigUint.fromSlice(listOf(3u)), BigUint.fromStrRadix("1_1", 2u).getOrNull())
        assertEquals(BigUint.fromSlice(listOf(0xffu)), BigUint.fromStrRadix("1111_1111", 2u).getOrNull())
    }

    @Test
    fun testAllStrRadix() {
        val n = BigUint.new((0u..<10u).toList())
        for (radix in 2u..<37u) {
            val s = n.toStrRadix(radix)
            val x = BigUint.fromStrRadix(s, radix)
            assertEquals(n, x.getOrThrow())

            val sUpper = s.uppercase()
            val xUpper = BigUint.fromStrRadix(sUpper, radix)
            assertEquals(n, xUpper.getOrThrow())
        }
    }

    @Test
    fun testBigStr() {
        for (n in 2u..20u) {
            val x = BigUint.fromUInt(n).pow(1_000u)
            val s = x.toString()
            val y = BigUint.fromStrRadix(s, 10u).getOrThrow()
            assertEquals(x, y)
        }
    }

    @Test
    fun testLowerHex() {
        val a = BigUint.parseBytes("A".encodeToByteArray(), 16u)!!
        val hello = BigUint.parseBytes("22405534230753963835153736737".encodeToByteArray(), 10u)!!

        assertEquals("a", a.toLowerHexString())
        assertEquals("48656c6c6f20776f726c6421", hello.toLowerHexString())
    }

    @Test
    fun testUpperHex() {
        val a = BigUint.parseBytes("A".encodeToByteArray(), 16u)!!
        val hello = BigUint.parseBytes("22405534230753963835153736737".encodeToByteArray(), 10u)!!

        assertEquals("A", a.toUpperHexString())
        assertEquals("48656C6C6F20776F726C6421", hello.toUpperHexString())
    }

    @Test
    fun testBinary() {
        val a = BigUint.parseBytes("A".encodeToByteArray(), 16u)!!
        val hello = BigUint.parseBytes("224055342307539".encodeToByteArray(), 10u)!!

        assertEquals("1010", a.toBinaryString())
        assertEquals(
            "110010111100011011110011000101101001100011010011",
            hello.toBinaryString(),
        )
    }

    @Test
    fun testOctal() {
        val a = BigUint.parseBytes("A".encodeToByteArray(), 16u)!!
        val hello = BigUint.parseBytes("22405534230753963835153736737".encodeToByteArray(), 10u)!!

        assertEquals("12", a.toOctalString())
        assertEquals("22062554330674403566756233062041", hello.toOctalString())
    }

    @Test
    fun testDisplay() {
        val a = BigUint.parseBytes("A".encodeToByteArray(), 16u)!!
        val hello = BigUint.parseBytes("22405534230753963835153736737".encodeToByteArray(), 10u)!!

        assertEquals("10", a.toString())
        assertEquals("22405534230753963835153736737", hello.toString())
    }

    @Test
    fun testFactor() {
        fun factor(n: UInt): BigUint {
            val f = BigUint.one()
            for (i in 2u..n) {
                val bu = BigUint.fromUInt(i)
                f.timesAssign(bu)
            }
            return f
        }

        fun check(n: UInt, s: String) {
            val fact = factor(n)
            val ans = BigUint.fromStrRadix(s, 10u).getOrThrow()
            assertEquals(ans, fact)
        }

        check(3u, "6")
        check(10u, "3628800")
        check(20u, "2432902008176640000")
        check(30u, "265252859812191058636308480000000")
    }

    @Test
    fun testBits() {
        assertEquals(0uL, BigUint.new(listOf(0u, 0u, 0u, 0u)).bits())
        assertEquals(0uL, BigUint.fromUInt(0u).bits())
        assertEquals(1uL, BigUint.fromUInt(1u).bits())
        assertEquals(2uL, BigUint.fromUInt(3u).bits())
        val n = BigUint.fromStrRadix("4000000000", 16u).getOrThrow()
        assertEquals(39uL, n.bits())
        val one = BigUint.one()
        assertEquals(427uL, (one shl 426).bits())
    }

    @Test
    fun testIterSum() {
        val result = BigUint.fromUInt(1234567u)
        val data =
            listOf(
                BigUint.fromUInt(1000000u),
                BigUint.fromUInt(200000u),
                BigUint.fromUInt(30000u),
                BigUint.fromUInt(4000u),
                BigUint.fromUInt(500u),
                BigUint.fromUInt(60u),
                BigUint.fromUInt(7u),
            )

        assertEquals(result, data.sumBigUint())
    }

    @Test
    fun testIterProduct() {
        val data =
            listOf(
                BigUint.fromUInt(1001u),
                BigUint.fromUInt(1002u),
                BigUint.fromUInt(1003u),
                BigUint.fromUInt(1004u),
                BigUint.fromUInt(1005u),
            )
        val result = data[0] * data[1] * data[2] * data[3] * data[4]

        assertEquals(result, data.productBigUint())
    }

    @Test
    fun testIterSumGeneric() {
        val result = BigUint.fromUInt(1234567u)
        val data = listOf(1000000u, 200000u, 30000u, 4000u, 500u, 60u, 7u)

        assertEquals(result, data.map { it.toBigUint() }.sumBigUint())
    }

    @Test
    fun testIterProductGeneric() {
        val data = listOf(1001u, 1002u, 1003u, 1004u, 1005u)
        val result =
            data[0].toBigUint() *
                data[1].toBigUint() *
                data[2].toBigUint() *
                data[3].toBigUint() *
                data[4].toBigUint()

        assertEquals(result, data.map { it.toBigUint() }.productBigUint())
    }

    @Test
    fun testPow() {
        val one = BigUint.fromUInt(1u)
        val two = BigUint.fromUInt(2u)
        val four = BigUint.fromUInt(4u)
        val eight = BigUint.fromUInt(8u)
        val tentwentyfour = BigUint.fromUInt(1024u)
        val twentyfourtyeight = BigUint.fromUInt(2048u)

        assertEquals(one, two.pow(0u))
        assertEquals(two, two.pow(1u))
        assertEquals(four, two.pow(2u))
        assertEquals(eight, two.pow(3u))
        assertEquals(tentwentyfour, two.pow(10u))
        assertEquals(twentyfourtyeight, two.pow(11u))

        val pow1e1000 = BigUint.fromUInt(10u).pow(1_000u)
        val manual1e1000 = List(1_000) { BigUint.fromUInt(10u) }.productBigUint()
        assertEquals(pow1e1000, manual1e1000)
    }

    @Test
    fun testTrailingZeros() {
        assertNull(BigUint.fromUInt(0u).trailingZeros())
        assertEquals(0uL, BigUint.fromUInt(1u).trailingZeros())
        assertEquals(1uL, BigUint.fromUInt(2u).trailingZeros())
        val x = BigUint.one() shl 128
        assertEquals(128uL, x.trailingZeros())
    }

    @Test
    fun testTrailingOnes() {
        assertEquals(0uL, BigUint.fromUInt(0u).trailingOnes())
        assertEquals(1uL, BigUint.fromUInt(1u).trailingOnes())
        assertEquals(0uL, BigUint.fromUInt(2u).trailingOnes())
        assertEquals(2uL, BigUint.fromUInt(3u).trailingOnes())
        val x = (BigUint.fromUInt(3u) shl 128) bitOr BigUint.fromUInt(3u)
        assertEquals(2uL, x.trailingOnes())
        val y = (BigUint.one() shl 128) - BigUint.one()
        assertEquals(128uL, y.trailingOnes())
    }

    @Test
    fun testCountOnes() {
        assertEquals(0uL, BigUint.fromUInt(0u).countOnes())
        assertEquals(1uL, BigUint.fromUInt(1u).countOnes())
        assertEquals(1uL, BigUint.fromUInt(2u).countOnes())
        assertEquals(2uL, BigUint.fromUInt(3u).countOnes())
        val x = (BigUint.fromUInt(3u) shl 128) bitOr BigUint.fromUInt(3u)
        assertEquals(4uL, x.countOnes())
    }

    @Test
    fun testBit() {
        assertFalse(BigUint.fromUInt(0u).bit(0uL))
        assertFalse(BigUint.fromUInt(0u).bit(100uL))
        assertFalse(BigUint.fromUInt(42u).bit(4uL))
        assertTrue(BigUint.fromUInt(42u).bit(5uL))
        val x = (BigUint.fromUInt(3u) shl 128) bitOr BigUint.fromUInt(3u)
        assertTrue(x.bit(129uL))
        assertFalse(x.bit(130uL))
    }

    @Test
    fun testSetBit() {
        val x = BigUint.fromUInt(3u)
        x.setBit(128uL, true)
        x.setBit(129uL, true)
        assertEquals((BigUint.fromUInt(3u) shl 128) bitOr BigUint.fromUInt(3u), x)
        x.setBit(0uL, false)
        x.setBit(128uL, false)
        x.setBit(130uL, false)
        assertEquals((BigUint.fromUInt(2u) shl 128) bitOr BigUint.fromUInt(2u), x)
        x.setBit(129uL, false)
        x.setBit(1uL, false)
        assertEquals(BigUint.zero(), x)
    }
}
