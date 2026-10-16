// port-lint: tests tests/bigint.rs
package io.github.kotlinmania.numbigint

import io.github.kotlinmania.numbigint.bigint.minus
import io.github.kotlinmania.numbigint.bigint.minusAssign
import io.github.kotlinmania.numbigint.bigint.plus
import io.github.kotlinmania.numbigint.bigint.plusAssign
import io.github.kotlinmania.numbigint.bigint.productBigInt
import io.github.kotlinmania.numbigint.bigint.sumBigInt
import io.github.kotlinmania.numbigint.bigint.times
import io.github.kotlinmania.numbigint.bigint.timesAssign
import kotlin.math.E
import kotlin.math.PI
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun hash(x: Any): Int = x.hashCode()

class BigIntTest {
    @Test
    fun testFromBytesBe() {
        fun check(s: String, result: String) {
            assertEquals(
                BigInt.fromBytesBe(Sign.Plus, s.encodeToByteArray().map { it.toUByte() }),
                BigInt.parseBytes(result.encodeToByteArray(), 10u)!!,
            )
        }
        check("A", "65")
        check("AA", "16705")
        check("AB", "16706")
        check("Hello world!", "22405534230753963835153736737")
        assertEquals(BigInt.zero(), BigInt.fromBytesBe(Sign.Plus, emptyList()))
        assertEquals(BigInt.zero(), BigInt.fromBytesBe(Sign.Minus, emptyList()))
    }

    @Test
    fun testToBytesBe() {
        fun check(s: String, result: String) {
            val b = BigInt.parseBytes(result.encodeToByteArray(), 10u)!!
            val (sign, v) = b.toBytesBe()
            assertEquals(Pair(Sign.Plus, s.encodeToByteArray().map { it.toUByte() }), Pair(sign, v))
        }
        check("A", "65")
        check("AA", "16705")
        check("AB", "16706")
        check("Hello world!", "22405534230753963835153736737")
        val b = BigInt.zero()
        assertEquals(Pair(Sign.NoSign, listOf<UByte>(0u)), b.toBytesBe())

        // Test with leading/trailing zero bytes and a full BigDigit of value 0
        val b2 = BigInt.fromStrRadix("00010000000000000200", 16u).getOrThrow()
        assertEquals(Pair(Sign.Plus, listOf<UByte>(1u, 0u, 0u, 0u, 0u, 0u, 0u, 2u, 0u)), b2.toBytesBe())
    }

    @Test
    fun testFromBytesLe() {
        fun check(s: String, result: String) {
            assertEquals(
                BigInt.fromBytesLe(Sign.Plus, s.encodeToByteArray().map { it.toUByte() }),
                BigInt.parseBytes(result.encodeToByteArray(), 10u)!!,
            )
        }
        check("A", "65")
        check("AA", "16705")
        check("BA", "16706")
        check("!dlrow olleH", "22405534230753963835153736737")
        assertEquals(BigInt.zero(), BigInt.fromBytesLe(Sign.Plus, emptyList()))
        assertEquals(BigInt.zero(), BigInt.fromBytesLe(Sign.Minus, emptyList()))
    }

    @Test
    fun testToBytesLe() {
        fun check(s: String, result: String) {
            val b = BigInt.parseBytes(result.encodeToByteArray(), 10u)!!
            val (sign, v) = b.toBytesLe()
            assertEquals(Pair(Sign.Plus, s.encodeToByteArray().map { it.toUByte() }), Pair(sign, v))
        }
        check("A", "65")
        check("AA", "16705")
        check("BA", "16706")
        check("!dlrow olleH", "22405534230753963835153736737")
        val b = BigInt.zero()
        assertEquals(Pair(Sign.NoSign, listOf<UByte>(0u)), b.toBytesLe())

        // Test with leading/trailing zero bytes and a full BigDigit of value 0
        val b2 = BigInt.fromStrRadix("00010000000000000200", 16u).getOrThrow()
        assertEquals(Pair(Sign.Plus, listOf<UByte>(0u, 2u, 0u, 0u, 0u, 0u, 0u, 0u, 1u)), b2.toBytesLe())
    }

    @Test
    fun testToSignedBytesLe() {
        fun check(s: String, result: List<UByte>) {
            val b = BigInt.parseBytes(s.encodeToByteArray(), 10u)!!
            assertEquals(result, b.toSignedBytesLe())
        }

        check("0", listOf(0u))
        check("32767", listOf(0xffu, 0x7fu))
        check("-1", listOf(0xffu))
        check("16777216", listOf(0u, 0u, 0u, 1u))
        check("-100", listOf(156u))
        check("-8388608", listOf(0u, 0u, 0x80u))
        check("-192", listOf(0x40u, 0xffu))
        check("128", listOf(0x80u, 0u))
    }

    @Test
    fun testFromSignedBytesLe() {
        fun check(s: List<UByte>, result: String) {
            val b = BigInt.parseBytes(result.encodeToByteArray(), 10u)!!
            assertEquals(b, BigInt.fromSignedBytesLe(s))
        }

        check(emptyList(), "0")
        check(listOf(0u), "0")
        check(List(10) { 0u }, "0")
        check(listOf(0xffu, 0x7fu), "32767")
        check(listOf(0xffu), "-1")
        check(listOf(0u, 0u, 0u, 1u), "16777216")
        check(listOf(156u), "-100")
        check(listOf(0u, 0u, 0x80u), "-8388608")
        check(List(10) { 0xffu }, "-1")
        check(listOf(0x40u, 0xffu), "-192")
    }

    @Test
    fun testToSignedBytesBe() {
        fun check(s: String, result: List<UByte>) {
            val b = BigInt.parseBytes(s.encodeToByteArray(), 10u)!!
            assertEquals(result, b.toSignedBytesBe())
        }

        check("0", listOf(0u))
        check("32767", listOf(0x7fu, 0xffu))
        check("-1", listOf(255u))
        check("16777216", listOf(1u, 0u, 0u, 0u))
        check("-100", listOf(156u))
        check("-8388608", listOf(128u, 0u, 0u))
        check("-192", listOf(0xffu, 0x40u))
        check("128", listOf(0u, 0x80u))
    }

    @Test
    fun testFromSignedBytesBe() {
        fun check(s: List<UByte>, result: String) {
            val b = BigInt.parseBytes(result.encodeToByteArray(), 10u)!!
            assertEquals(b, BigInt.fromSignedBytesBe(s))
        }

        check(emptyList(), "0")
        check(listOf(0u), "0")
        check(List(10) { 0u }, "0")
        check(listOf(127u, 255u), "32767")
        check(listOf(255u), "-1")
        check(listOf(1u, 0u, 0u, 0u), "16777216")
        check(listOf(156u), "-100")
        check(listOf(128u, 0u, 0u), "-8388608")
        check(List(10) { 255u }, "-1")
        check(listOf(0xffu, 0x40u), "-192")
    }

    @Test
    fun testSignedBytesBeRoundTrip() {
        for (i in -0x1FFFF until 0x20000) {
            val n = BigInt.from(i)
            assertEquals(n, BigInt.fromSignedBytesBe(n.toSignedBytesBe()))
        }
    }

    @Test
    fun testSignedBytesLeRoundTrip() {
        for (i in -0x1FFFF until 0x20000) {
            val n = BigInt.from(i)
            assertEquals(n, BigInt.fromSignedBytesLe(n.toSignedBytesLe()))
        }
    }

    @Test
    fun testCmp() {
        val vs = listOf(listOf(2u), listOf(1u, 1u), listOf(2u, 1u), listOf(1u, 1u, 1u))
        val nums = mutableListOf<BigInt>()
        for (s in vs.reversed()) {
            nums.add(BigInt.fromSlice(Sign.Minus, s))
        }
        nums.add(BigInt.zero())
        nums.addAll(vs.map { s -> BigInt.fromSlice(Sign.Plus, s) })

        for ((i, ni) in nums.withIndex()) {
            for ((j0, nj) in nums.subList(i, nums.size).withIndex()) {
                val j = i + j0
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
        val a = BigInt.new(Sign.NoSign, emptyList())
        val b = BigInt.new(Sign.NoSign, listOf(0u))
        val c = BigInt.new(Sign.Plus, listOf(1u))
        val d = BigInt.new(Sign.Plus, listOf(1u, 0u, 0u, 0u, 0u, 0u))
        val e = BigInt.new(Sign.Plus, listOf(0u, 0u, 0u, 0u, 0u, 1u))
        val f = BigInt.new(Sign.Minus, listOf(1u))
        assertEquals(hash(a), hash(b))
        assertNotEquals(hash(b), hash(c))
        assertEquals(hash(c), hash(d))
        assertNotEquals(hash(d), hash(e))
        assertNotEquals(hash(c), hash(f))
    }

    @Test
    fun testConvertI64() {
        fun check(b1: BigInt, i: Long) {
            val b2 = BigInt.fromLong(i)!!
            assertEquals(b1, b2)
            assertEquals(i, b1.toLongOrNull())
        }

        check(BigInt.zero(), 0L)
        check(BigInt.one(), 1L)
        check(Long.MIN_VALUE.toBigInt(), Long.MIN_VALUE)
        check(Long.MAX_VALUE.toBigInt(), Long.MAX_VALUE)

        assertNull((Long.MAX_VALUE.toULong() + 1uL).toBigInt().toLongOrNull())

        assertNull(BigInt.fromBiguint(Sign.Plus, BigUint.new(listOf(1u, 2u, 3u, 4u, 5u))).toLongOrNull())

        assertNull(BigInt.fromBiguint(Sign.Minus, BigUint.new(listOf(1u, 0u, 0u, 1u shl 31))).toLongOrNull())

        assertNull(BigInt.fromBiguint(Sign.Minus, BigUint.new(listOf(1u, 2u, 3u, 4u, 5u))).toLongOrNull())
    }

    @Test
    fun testConvertI128() {
        fun check(b1: BigInt, expected: BigInt) {
            assertEquals(b1, expected)
        }

        check(BigInt.zero(), BigInt.new(Sign.NoSign, emptyList()))
        check(BigInt.one(), BigInt.new(Sign.Plus, listOf(1u)))
        check(
            BigInt.new(Sign.Minus, listOf(0u, 0u, 0u, 1u shl 31)),
            -(BigInt.one() shl 127),
        )
        check(
            BigInt.new(Sign.Plus, listOf(N1, N1, N1, N1 shr 1)),
            (BigInt.one() shl 127) - 1u,
        )
    }

    @Test
    fun testConvertU64() {
        fun check(b1: BigInt, u: ULong) {
            val b2 = BigInt.fromULong(u)!!
            assertEquals(b1, b2)
            assertEquals(u, b1.toULongOrNull())
        }

        check(BigInt.zero(), 0uL)
        check(BigInt.one(), 1uL)
        check(ULong.MIN_VALUE.toBigInt(), ULong.MIN_VALUE)
        check(ULong.MAX_VALUE.toBigInt(), ULong.MAX_VALUE)

        assertNull(BigInt.fromBiguint(Sign.Plus, BigUint.new(listOf(1u, 2u, 3u, 4u, 5u))).toULongOrNull())

        val maxValue = BigUint.fromULong(ULong.MAX_VALUE)
        assertNull(BigInt.fromBiguint(Sign.Minus, maxValue).toULongOrNull())
        assertNull(BigInt.fromBiguint(Sign.Minus, BigUint.new(listOf(1u, 2u, 3u, 4u, 5u))).toULongOrNull())
    }

    @Test
    fun testConvertU128() {
        fun check(b1: BigInt, expected: BigInt) {
            assertEquals(b1, expected)
        }

        check(BigInt.zero(), BigInt.new(Sign.NoSign, emptyList()))
        check(BigInt.one(), BigInt.new(Sign.Plus, listOf(1u)))
        check(
            BigInt.new(Sign.Plus, listOf(N1, N1, N1, N1)),
            (BigInt.one() shl 128) - 1u,
        )
    }

    @Test
    fun testConvertF32() {
        fun check(b1: BigInt, f: Float) {
            val b2 = BigInt.fromFloat(f)!!
            assertEquals(b1, b2)
            assertEquals(f, b1.toFloatOrNull())
            val negB1 = -b1
            val negB2 = BigInt.fromFloat(-f)!!
            assertEquals(negB1, negB2)
            assertEquals(-f + 0.0f, negB1.toFloatOrNull())
        }

        check(BigInt.zero(), 0.0f)
        check(BigInt.one(), 1.0f)
        check(BigInt.from(UShort.MAX_VALUE.toUInt()), 2.0f.pow(16) - 1.0f)
        check(BigInt.from(1uL shl 32), 2.0f.pow(32))
        check(BigInt.fromSlice(Sign.Plus, listOf(0u, 0u, 1u)), 2.0f.pow(64))
        check(
            (BigInt.one() shl 100) + (BigInt.one() shl 123),
            2.0f.pow(100) + 2.0f.pow(123),
        )
        check(BigInt.one() shl 127, 2.0f.pow(127))
        check(
            BigInt.from((1uL shl 24) - 1uL) shl (128 - 24),
            Float.fromBits(Float.MAX_VALUE.toRawBits()),
        )

        // keeping all 24 digits with the bits at different offsets to the BigDigits
        val x = 0b00000000101111011111011011011101u
        var f = Float.fromBits(x.toFloat().toRawBits())
        var b = BigInt.from(x)
        repeat(64) {
            check(b, f)
            f *= 2.0f
            b = b shl 1
        }

        // this number when rounded to f64 then f32 isn't the same as when rounded straight to f32
        var n: Long = 0b0000000000111111111111111111111111011111111111111111111111111111L
        val expectedF32 = Float.fromBits(0x5a7fffff)
        assertNotEquals(Float.fromBits(n.toDouble().toFloat().toRawBits()), expectedF32)
        assertEquals(expectedF32, BigInt.from(n).toFloatOrNull())
        n = -n
        assertNotEquals(Float.fromBits(n.toDouble().toFloat().toRawBits()), -expectedF32)
        assertEquals(-expectedF32, BigInt.from(n).toFloatOrNull())

        // test rounding up with the bits at different offsets to the BigDigits
        f = Float.fromBits(((1uL shl 25) - 1uL).toFloat().toRawBits())
        b = BigInt.from(1uL shl 25)
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
        assertEquals(BigInt.from(-3), BigInt.fromFloat(-PI.toFloat()))
        assertEquals(BigInt.from(-2), BigInt.fromFloat(-E.toFloat()))
        assertEquals(BigInt.zero(), BigInt.fromFloat(-0.99999f))
        assertEquals(BigInt.zero(), BigInt.fromFloat(-0.5f))
        assertEquals(BigInt.zero(), BigInt.fromFloat(-0.0f))
        assertEquals(BigInt.zero(), BigInt.fromFloat(Float.MIN_VALUE / 2.0f))
        assertEquals(BigInt.zero(), BigInt.fromFloat(Float.fromBits(0x00800000)))
        assertEquals(BigInt.zero(), BigInt.fromFloat(0.5f))
        assertEquals(BigInt.zero(), BigInt.fromFloat(0.99999f))
        assertEquals(BigInt.from(2u), BigInt.fromFloat(E.toFloat()))
        assertEquals(BigInt.from(3u), BigInt.fromFloat(PI.toFloat()))

        // special float values
        assertNull(BigInt.fromFloat(Float.NaN))
        assertNull(BigInt.fromFloat(Float.POSITIVE_INFINITY))
        assertNull(BigInt.fromFloat(Float.NEGATIVE_INFINITY))

        // largest BigInt that will round to a finite f32 value
        val bigNum = (BigInt.one() shl 128) - 1u - (BigInt.one() shl (128 - 25))
        val f32Max = Float.fromBits(Float.MAX_VALUE.toRawBits())
        assertEquals(f32Max, bigNum.toFloatOrNull())
        assertEquals(Float.POSITIVE_INFINITY, (bigNum + 1u).toFloatOrNull())
        assertEquals(-f32Max, (-bigNum).toFloatOrNull())
        assertEquals(Float.NEGATIVE_INFINITY, ((-bigNum) - 1u).toFloatOrNull())

        assertEquals(Float.POSITIVE_INFINITY, ((BigInt.one() shl 128) - 1u).toFloatOrNull())
        assertEquals(Float.POSITIVE_INFINITY, (BigInt.one() shl 128).toFloatOrNull())
        assertEquals(Float.NEGATIVE_INFINITY, (-((BigInt.one() shl 128) - 1u)).toFloatOrNull())
        assertEquals(Float.NEGATIVE_INFINITY, (-(BigInt.one() shl 128)).toFloatOrNull())
    }

    @Test
    fun testConvertF64() {
        fun check(b1: BigInt, f: Double) {
            val b2 = BigInt.fromDouble(f)!!
            assertEquals(b1, b2)
            assertEquals(f, b1.toDoubleOrNull())
            val negB1 = -b1
            val negB2 = BigInt.fromDouble(-f)!!
            assertEquals(negB1, negB2)
            assertEquals(-f + 0.0, negB1.toDoubleOrNull())
        }

        check(BigInt.zero(), 0.0)
        check(BigInt.one(), 1.0)
        check(BigInt.from(UInt.MAX_VALUE), 2.0.pow(32) - 1.0)
        check(BigInt.from(1uL shl 32), 2.0.pow(32))
        check(BigInt.fromSlice(Sign.Plus, listOf(0u, 0u, 1u)), 2.0.pow(64))
        check(
            (BigInt.one() shl 100) + (BigInt.one() shl 152),
            2.0.pow(100) + 2.0.pow(152),
        )
        check(BigInt.one() shl 1023, 2.0.pow(1023))
        check(BigInt.from((1uL shl 53) - 1uL) shl (1024 - 53), Double.MAX_VALUE)

        // keeping all 53 digits with the bits at different offsets to the BigDigits
        val x = 0b0000000000011110111110110111111101110111101111011111011011011101uL
        var f = x.toDouble()
        var b = BigInt.from(x)
        repeat(128) {
            check(b, f)
            f *= 2.0
            b = b shl 1
        }

        // test rounding up with the bits at different offsets to the BigDigits
        f = ((1uL shl 54) - 1uL).toDouble()
        b = BigInt.from(1uL shl 54)
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
        assertEquals(BigInt.from(-3), BigInt.fromDouble(-PI))
        assertEquals(BigInt.from(-2), BigInt.fromDouble(-E))
        assertEquals(BigInt.zero(), BigInt.fromDouble(-0.99999))
        assertEquals(BigInt.zero(), BigInt.fromDouble(-0.5))
        assertEquals(BigInt.zero(), BigInt.fromDouble(-0.0))
        assertEquals(BigInt.zero(), BigInt.fromDouble(Double.MIN_VALUE / 2.0))
        assertEquals(BigInt.zero(), BigInt.fromDouble(Double.fromBits(0x0010000000000000L)))
        assertEquals(BigInt.zero(), BigInt.fromDouble(0.5))
        assertEquals(BigInt.zero(), BigInt.fromDouble(0.99999))
        assertEquals(BigInt.from(2u), BigInt.fromDouble(E))
        assertEquals(BigInt.from(3u), BigInt.fromDouble(PI))

        // special float values
        assertNull(BigInt.fromDouble(Double.NaN))
        assertNull(BigInt.fromDouble(Double.POSITIVE_INFINITY))
        assertNull(BigInt.fromDouble(Double.NEGATIVE_INFINITY))

        // largest BigInt that will round to a finite f64 value
        val bigNum = (BigInt.one() shl 1024) - 1u - (BigInt.one() shl (1024 - 54))
        assertEquals(Double.MAX_VALUE, bigNum.toDoubleOrNull())
        assertEquals(Double.POSITIVE_INFINITY, (bigNum + 1u).toDoubleOrNull())
        assertEquals(-Double.MAX_VALUE, (-bigNum).toDoubleOrNull())
        assertEquals(Double.NEGATIVE_INFINITY, ((-bigNum) - 1u).toDoubleOrNull())

        assertEquals(Double.POSITIVE_INFINITY, ((BigInt.one() shl 1024) - 1u).toDoubleOrNull())
        assertEquals(Double.POSITIVE_INFINITY, (BigInt.one() shl 1024).toDoubleOrNull())
        assertEquals(Double.NEGATIVE_INFINITY, (-((BigInt.one() shl 1024) - 1u)).toDoubleOrNull())
        assertEquals(Double.NEGATIVE_INFINITY, (-(BigInt.one() shl 1024)).toDoubleOrNull())
    }

    @Test
    fun testConvertToBiguint() {
        fun check(n: BigInt, ans1: BigUint) {
            assertEquals(ans1, n.toBigUint()!!)
            assertEquals(n, n.toBigUint()!!.toBigInt())
        }
        val zero = BigInt.zero()
        val unsignedZero = BigUint.zero()
        val positive = BigInt.fromBiguint(Sign.Plus, BigUint.new(listOf(1u, 2u, 3u)))
        val negative = -positive

        check(zero, unsignedZero)
        check(positive, BigUint.new(listOf(1u, 2u, 3u)))

        assertNull(negative.toBigUint())
    }

    @Test
    fun testConvertFromUint() {
        assertEquals(BigInt.zero(), BigInt.from(0u))
        assertEquals(BigInt.one(), BigInt.from(1u))
        assertEquals(
            BigInt.fromSlice(Sign.Plus, listOf(UByte.MAX_VALUE.toUInt())) - BigInt.one(),
            BigInt.from(UByte.MAX_VALUE - 1u),
        )
        assertEquals(
            BigInt.fromSlice(Sign.Plus, listOf(UByte.MAX_VALUE.toUInt())),
            BigInt.from(UByte.MAX_VALUE.toUInt()),
        )

        assertEquals(
            BigInt.fromSlice(Sign.Plus, listOf(UShort.MAX_VALUE.toUInt())) - BigInt.one(),
            BigInt.from(UShort.MAX_VALUE - 1u),
        )
        assertEquals(
            BigInt.fromSlice(Sign.Plus, listOf(UShort.MAX_VALUE.toUInt())),
            BigInt.from(UShort.MAX_VALUE.toUInt()),
        )

        assertEquals(
            BigInt.fromSlice(Sign.Plus, listOf(UInt.MAX_VALUE)) - BigInt.one(),
            BigInt.from(UInt.MAX_VALUE - 1u),
        )
        assertEquals(
            BigInt.fromSlice(Sign.Plus, listOf(UInt.MAX_VALUE)),
            BigInt.from(UInt.MAX_VALUE),
        )

        assertEquals(
            BigInt.fromSlice(Sign.Plus, listOf(UInt.MAX_VALUE, UInt.MAX_VALUE)) - BigInt.one(),
            BigInt.from(ULong.MAX_VALUE - 1uL),
        )
        assertEquals(
            BigInt.fromSlice(Sign.Plus, listOf(UInt.MAX_VALUE, UInt.MAX_VALUE)),
            BigInt.from(ULong.MAX_VALUE),
        )
    }

    @Test
    fun testConvertFromInt() {
        assertEquals(BigInt.fromSlice(Sign.Minus, listOf(1u shl 7)), BigInt.from(Byte.MIN_VALUE.toInt()))
        assertEquals(
            BigInt.fromSlice(Sign.Minus, listOf(1u shl 7)) + BigInt.one(),
            BigInt.from(Byte.MIN_VALUE + 1),
        )
        assertEquals(-BigInt.one(), BigInt.from(-1))
        assertEquals(BigInt.zero(), BigInt.from(0))
        assertEquals(BigInt.one(), BigInt.from(1))
        assertEquals(
            BigInt.fromSlice(Sign.Plus, listOf(Byte.MAX_VALUE.toUInt())) - BigInt.one(),
            BigInt.from(Byte.MAX_VALUE - 1),
        )
        assertEquals(
            BigInt.fromSlice(Sign.Plus, listOf(Byte.MAX_VALUE.toUInt())),
            BigInt.from(Byte.MAX_VALUE.toInt()),
        )

        assertEquals(BigInt.fromSlice(Sign.Minus, listOf(1u shl 15)), BigInt.from(Short.MIN_VALUE.toInt()))
        assertEquals(
            BigInt.fromSlice(Sign.Minus, listOf(1u shl 15)) + BigInt.one(),
            BigInt.from(Short.MIN_VALUE + 1),
        )
        assertEquals(
            BigInt.fromSlice(Sign.Plus, listOf(Short.MAX_VALUE.toUInt())) - BigInt.one(),
            BigInt.from(Short.MAX_VALUE - 1),
        )
        assertEquals(
            BigInt.fromSlice(Sign.Plus, listOf(Short.MAX_VALUE.toUInt())),
            BigInt.from(Short.MAX_VALUE.toInt()),
        )

        assertEquals(BigInt.fromSlice(Sign.Minus, listOf(1u shl 31)), BigInt.from(Int.MIN_VALUE))
        assertEquals(
            BigInt.fromSlice(Sign.Minus, listOf(1u shl 31)) + BigInt.one(),
            BigInt.from(Int.MIN_VALUE + 1),
        )
        assertEquals(
            BigInt.fromSlice(Sign.Plus, listOf(Int.MAX_VALUE.toUInt())) - BigInt.one(),
            BigInt.from(Int.MAX_VALUE - 1),
        )
        assertEquals(
            BigInt.fromSlice(Sign.Plus, listOf(Int.MAX_VALUE.toUInt())),
            BigInt.from(Int.MAX_VALUE),
        )

        assertEquals(BigInt.fromSlice(Sign.Minus, listOf(0u, 1u shl 31)), BigInt.from(Long.MIN_VALUE))
        assertEquals(
            BigInt.fromSlice(Sign.Minus, listOf(0u, 1u shl 31)) + BigInt.one(),
            BigInt.from(Long.MIN_VALUE + 1L),
        )
        assertEquals(
            BigInt.fromSlice(Sign.Plus, listOf(UInt.MAX_VALUE, Int.MAX_VALUE.toUInt())) - BigInt.one(),
            BigInt.from(Long.MAX_VALUE - 1L),
        )
        assertEquals(
            BigInt.fromSlice(Sign.Plus, listOf(UInt.MAX_VALUE, Int.MAX_VALUE.toUInt())),
            BigInt.from(Long.MAX_VALUE),
        )
    }

    @Test
    fun testConvertFromBiguint() {
        assertEquals(BigInt.zero(), BigInt.from(BigUint.zero()))
        assertEquals(BigInt.one(), BigInt.from(BigUint.one()))
        assertEquals(
            BigInt.fromSlice(Sign.Plus, listOf(1u, 2u, 3u)),
            BigInt.from(BigUint.fromSlice(listOf(1u, 2u, 3u))),
        )
    }

    @Test
    fun testAdd() {
        for ((aVec, bVec, cVec) in SUM_TRIPLES) {
            val a = BigInt.fromSlice(Sign.Plus, aVec)
            val b = BigInt.fromSlice(Sign.Plus, bVec)
            val c = BigInt.fromSlice(Sign.Plus, cVec)
            val (na, nb, nc) = Triple(-a, -b, -c)

            assertOp(a, b, c, BigInt::clone, BigInt::clone) { x, y -> x + y }
            assertOp(b, a, c, BigInt::clone, BigInt::clone) { x, y -> x + y }
            assertOp(c, na, b, BigInt::clone, BigInt::clone) { x, y -> x + y }
            assertOp(c, nb, a, BigInt::clone, BigInt::clone) { x, y -> x + y }
            assertOp(a, nc, nb, BigInt::clone, BigInt::clone) { x, y -> x + y }
            assertOp(b, nc, na, BigInt::clone, BigInt::clone) { x, y -> x + y }
            assertOp(na, nb, nc, BigInt::clone, BigInt::clone) { x, y -> x + y }
            assertOp(a, na, BigInt.zero(), BigInt::clone, BigInt::clone) { x, y -> x + y }

            assertAssignOp(a, b, c, BigInt::clone, BigInt::clone) { x, y -> x.plusAssign(y) }
            assertAssignOp(b, a, c, BigInt::clone, BigInt::clone) { x, y -> x.plusAssign(y) }
            assertAssignOp(c, na, b, BigInt::clone, BigInt::clone) { x, y -> x.plusAssign(y) }
            assertAssignOp(c, nb, a, BigInt::clone, BigInt::clone) { x, y -> x.plusAssign(y) }
            assertAssignOp(a, nc, nb, BigInt::clone, BigInt::clone) { x, y -> x.plusAssign(y) }
            assertAssignOp(b, nc, na, BigInt::clone, BigInt::clone) { x, y -> x.plusAssign(y) }
            assertAssignOp(na, nb, nc, BigInt::clone, BigInt::clone) { x, y -> x.plusAssign(y) }
            assertAssignOp(a, na, BigInt.zero(), BigInt::clone, BigInt::clone) { x, y -> x.plusAssign(y) }
        }
    }

    @Test
    fun testSub() {
        for ((aVec, bVec, cVec) in SUM_TRIPLES) {
            val a = BigInt.fromSlice(Sign.Plus, aVec)
            val b = BigInt.fromSlice(Sign.Plus, bVec)
            val c = BigInt.fromSlice(Sign.Plus, cVec)
            val (na, nb, nc) = Triple(-a, -b, -c)

            assertOp(c, a, b, BigInt::clone, BigInt::clone) { x, y -> x - y }
            assertOp(c, b, a, BigInt::clone, BigInt::clone) { x, y -> x - y }
            assertOp(nb, a, nc, BigInt::clone, BigInt::clone) { x, y -> x - y }
            assertOp(na, b, nc, BigInt::clone, BigInt::clone) { x, y -> x - y }
            assertOp(b, na, c, BigInt::clone, BigInt::clone) { x, y -> x - y }
            assertOp(a, nb, c, BigInt::clone, BigInt::clone) { x, y -> x - y }
            assertOp(nc, na, nb, BigInt::clone, BigInt::clone) { x, y -> x - y }
            assertOp(a, a, BigInt.zero(), BigInt::clone, BigInt::clone) { x, y -> x - y }

            assertAssignOp(c, a, b, BigInt::clone, BigInt::clone) { x, y -> x.minusAssign(y) }
            assertAssignOp(c, b, a, BigInt::clone, BigInt::clone) { x, y -> x.minusAssign(y) }
            assertAssignOp(nb, a, nc, BigInt::clone, BigInt::clone) { x, y -> x.minusAssign(y) }
            assertAssignOp(na, b, nc, BigInt::clone, BigInt::clone) { x, y -> x.minusAssign(y) }
            assertAssignOp(b, na, c, BigInt::clone, BigInt::clone) { x, y -> x.minusAssign(y) }
            assertAssignOp(a, nb, c, BigInt::clone, BigInt::clone) { x, y -> x.minusAssign(y) }
            assertAssignOp(nc, na, nb, BigInt::clone, BigInt::clone) { x, y -> x.minusAssign(y) }
            assertAssignOp(a, a, BigInt.zero(), BigInt::clone, BigInt::clone) { x, y -> x.minusAssign(y) }
        }
    }

    @Test
    fun testMul() {
        for ((aVec, bVec, cVec) in MUL_TRIPLES) {
            val a = BigInt.fromSlice(Sign.Plus, aVec)
            val b = BigInt.fromSlice(Sign.Plus, bVec)
            val c = BigInt.fromSlice(Sign.Plus, cVec)
            val (na, nb, nc) = Triple(-a, -b, -c)

            assertOp(a, b, c, BigInt::clone, BigInt::clone) { x, y -> x * y }
            assertOp(b, a, c, BigInt::clone, BigInt::clone) { x, y -> x * y }
            assertOp(na, nb, c, BigInt::clone, BigInt::clone) { x, y -> x * y }

            assertOp(na, b, nc, BigInt::clone, BigInt::clone) { x, y -> x * y }
            assertOp(nb, a, nc, BigInt::clone, BigInt::clone) { x, y -> x * y }

            assertAssignOp(a, b, c, BigInt::clone, BigInt::clone) { x, y -> x.timesAssign(y) }
            assertAssignOp(b, a, c, BigInt::clone, BigInt::clone) { x, y -> x.timesAssign(y) }
            assertAssignOp(na, nb, c, BigInt::clone, BigInt::clone) { x, y -> x.timesAssign(y) }

            assertAssignOp(na, b, nc, BigInt::clone, BigInt::clone) { x, y -> x.timesAssign(y) }
            assertAssignOp(nb, a, nc, BigInt::clone, BigInt::clone) { x, y -> x.timesAssign(y) }
        }

        for (elm in DIV_REM_QUADRUPLES) {
            val a = BigInt.fromSlice(Sign.Plus, elm[0])
            val b = BigInt.fromSlice(Sign.Plus, elm[1])
            val c = BigInt.fromSlice(Sign.Plus, elm[2])
            val d = BigInt.fromSlice(Sign.Plus, elm[3])

            assertEquals(a, b * c + d)
            assertEquals(a, c * b + d)
        }
    }

    @Test
    fun testDivModFloor() {
        fun checkSub(a: BigInt, b: BigInt, ansD: BigInt, ansM: BigInt) {
            val (d, m) = a.divModFloor(b)
            assertEquals(d, a.divFloor(b))
            assertEquals(m, a.modFloor(b))
            if (!m.isZero()) {
                assertEquals(b.sign(), m.sign())
            }
            assertTrue(m.abs() <= b.abs())
            assertEquals(a, b * d + m)
            assertEquals(ansD, d)
            assertEquals(ansM, m)
        }

        fun check(a: BigInt, b: BigInt, d: BigInt, m: BigInt) {
            if (m.isZero()) {
                checkSub(a, b, d, m)
                checkSub(a, -b, -d, m)
                checkSub(-a, b, -d, m)
                checkSub(-a, -b, d, m)
            } else {
                val one = BigInt.one()
                checkSub(a, b, d, m)
                checkSub(a, -b, -d - one, m - b)
                checkSub(-a, b, -d - one, b - m)
                checkSub(-a, -b, d, -m)
            }
        }

        for ((aVec, bVec, cVec) in MUL_TRIPLES) {
            val a = BigInt.fromSlice(Sign.Plus, aVec)
            val b = BigInt.fromSlice(Sign.Plus, bVec)
            val c = BigInt.fromSlice(Sign.Plus, cVec)

            if (!a.isZero()) {
                check(c, a, b, BigInt.zero())
            }
            if (!b.isZero()) {
                check(c, b, a, BigInt.zero())
            }
        }

        for (elm in DIV_REM_QUADRUPLES) {
            val a = BigInt.fromSlice(Sign.Plus, elm[0])
            val b = BigInt.fromSlice(Sign.Plus, elm[1])
            val c = BigInt.fromSlice(Sign.Plus, elm[2])
            val d = BigInt.fromSlice(Sign.Plus, elm[3])

            if (!b.isZero()) {
                check(a, b, c, d)
            }
        }
    }

    @Test
    fun testDivRem() {
        fun checkSub(a: BigInt, b: BigInt, ansQ: BigInt, ansR: BigInt) {
            val (q, r) = a.divRem(b)
            if (!r.isZero()) {
                assertEquals(a.sign(), r.sign())
            }
            assertTrue(r.abs() <= b.abs())
            assertEquals(a, b * q + r)
            assertEquals(ansQ, q)
            assertEquals(ansR, r)

            assertOp(a, b, ansQ, BigInt::clone, BigInt::clone) { x, y -> x / y }
            assertOp(a, b, ansR, BigInt::clone, BigInt::clone) { x, y -> x % y }
            assertAssignOp(a, b, ansQ, BigInt::clone, BigInt::clone) { x, y -> x.divAssign(y) }
            assertAssignOp(a, b, ansR, BigInt::clone, BigInt::clone) { x, y -> x.remAssign(y) }
        }

        fun check(a: BigInt, b: BigInt, q: BigInt, r: BigInt) {
            checkSub(a, b, q, r)
            checkSub(a, -b, -q, r)
            checkSub(-a, b, -q, -r)
            checkSub(-a, -b, q, -r)
        }

        for ((aVec, bVec, cVec) in MUL_TRIPLES) {
            val a = BigInt.fromSlice(Sign.Plus, aVec)
            val b = BigInt.fromSlice(Sign.Plus, bVec)
            val c = BigInt.fromSlice(Sign.Plus, cVec)

            if (!a.isZero()) {
                check(c, a, b, BigInt.zero())
            }
            if (!b.isZero()) {
                check(c, b, a, BigInt.zero())
            }
        }

        for (elm in DIV_REM_QUADRUPLES) {
            val a = BigInt.fromSlice(Sign.Plus, elm[0])
            val b = BigInt.fromSlice(Sign.Plus, elm[1])
            val c = BigInt.fromSlice(Sign.Plus, elm[2])
            val d = BigInt.fromSlice(Sign.Plus, elm[3])

            if (!b.isZero()) {
                check(a, b, c, d)
            }
        }
    }

    @Test
    fun testDivCeil() {
        fun checkSub(a: BigInt, b: BigInt, ansD: BigInt) {
            assertEquals(ansD, a.divCeil(b))
        }

        fun check(a: BigInt, b: BigInt, d: BigInt, m: BigInt) {
            if (m.isZero()) {
                checkSub(a, b, d)
                checkSub(a, -b, -d)
                checkSub(-a, b, -d)
                checkSub(-a, -b, d)
            } else {
                checkSub(a, b, d + 1)
                checkSub(a, -b, -d)
                checkSub(-a, b, -d)
                checkSub(-a, -b, d + 1)
            }
        }

        for ((aVec, bVec, cVec) in MUL_TRIPLES) {
            val a = BigInt.fromSlice(Sign.Plus, aVec)
            val b = BigInt.fromSlice(Sign.Plus, bVec)
            val c = BigInt.fromSlice(Sign.Plus, cVec)

            if (!a.isZero()) {
                check(c, a, b, BigInt.zero())
            }
            if (!b.isZero()) {
                check(c, b, a, BigInt.zero())
            }
        }

        for (elm in DIV_REM_QUADRUPLES) {
            val a = BigInt.fromSlice(Sign.Plus, elm[0])
            val b = BigInt.fromSlice(Sign.Plus, elm[1])
            val c = BigInt.fromSlice(Sign.Plus, elm[2])
            val d = BigInt.fromSlice(Sign.Plus, elm[3])

            if (!b.isZero()) {
                check(a, b, c, d)
            }
        }
    }

    @Test
    fun testDivRemEuclid() {
        fun checkSub(a: BigInt, b: BigInt, ansD: BigInt, ansM: BigInt) {
            assertEquals(ansD, a.divEuclid(b))
            assertEquals(ansM, a.remEuclid(b))
            assertTrue(ansM >= BigInt.zero())
            assertTrue(ansM < b.abs())
        }

        fun check(a: BigInt, b: BigInt, d: BigInt, m: BigInt) {
            if (m.isZero()) {
                checkSub(a, b, d, m)
                checkSub(a, -b, -d, m)
                checkSub(-a, b, -d, m)
                checkSub(-a, -b, d, m)
            } else {
                val one = BigInt.one()
                checkSub(a, b, d, m)
                checkSub(a, -b, -d, m)
                checkSub(-a, b, -(d + one), b - m)
                checkSub(-a, -b, d + one, b.abs() - m)
            }
        }

        for ((aVec, bVec, cVec) in MUL_TRIPLES) {
            val a = BigInt.fromSlice(Sign.Plus, aVec)
            val b = BigInt.fromSlice(Sign.Plus, bVec)
            val c = BigInt.fromSlice(Sign.Plus, cVec)

            if (!a.isZero()) {
                check(c, a, b, BigInt.zero())
            }
            if (!b.isZero()) {
                check(c, b, a, BigInt.zero())
            }
        }

        for (elm in DIV_REM_QUADRUPLES) {
            val a = BigInt.fromSlice(Sign.Plus, elm[0])
            val b = BigInt.fromSlice(Sign.Plus, elm[1])
            val c = BigInt.fromSlice(Sign.Plus, elm[2])
            val d = BigInt.fromSlice(Sign.Plus, elm[3])

            if (!b.isZero()) {
                check(a, b, c, d)
            }
        }
    }

    @Test
    fun testCheckedAdd() {
        for ((aVec, bVec, cVec) in SUM_TRIPLES) {
            val a = BigInt.fromSlice(Sign.Plus, aVec)
            val b = BigInt.fromSlice(Sign.Plus, bVec)
            val c = BigInt.fromSlice(Sign.Plus, cVec)

            assertEquals(c, a.checkedAdd(b)!!)
            assertEquals(c, b.checkedAdd(a)!!)
            assertEquals(b, c.checkedAdd(-a)!!)
            assertEquals(a, c.checkedAdd(-b)!!)
            assertEquals(-b, a.checkedAdd(-c)!!)
            assertEquals(-a, b.checkedAdd(-c)!!)
            assertEquals(-c, (-a).checkedAdd(-b)!!)
            assertEquals(BigInt.zero(), a.checkedAdd(-a)!!)
        }
    }

    @Test
    fun testCheckedSub() {
        for ((aVec, bVec, cVec) in SUM_TRIPLES) {
            val a = BigInt.fromSlice(Sign.Plus, aVec)
            val b = BigInt.fromSlice(Sign.Plus, bVec)
            val c = BigInt.fromSlice(Sign.Plus, cVec)

            assertEquals(b, c.checkedSub(a)!!)
            assertEquals(a, c.checkedSub(b)!!)
            assertEquals(-c, (-b).checkedSub(a)!!)
            assertEquals(-c, (-a).checkedSub(b)!!)
            assertEquals(c, b.checkedSub(-a)!!)
            assertEquals(c, a.checkedSub(-b)!!)
            assertEquals(-b, (-c).checkedSub(-a)!!)
            assertEquals(BigInt.zero(), a.checkedSub(a)!!)
        }
    }

    @Test
    fun testCheckedMul() {
        for ((aVec, bVec, cVec) in MUL_TRIPLES) {
            val a = BigInt.fromSlice(Sign.Plus, aVec)
            val b = BigInt.fromSlice(Sign.Plus, bVec)
            val c = BigInt.fromSlice(Sign.Plus, cVec)

            assertEquals(c, a.checkedMul(b)!!)
            assertEquals(c, b.checkedMul(a)!!)

            assertEquals(-c, (-a).checkedMul(b)!!)
            assertEquals(-c, (-b).checkedMul(a)!!)
        }

        for (elm in DIV_REM_QUADRUPLES) {
            val a = BigInt.fromSlice(Sign.Plus, elm[0])
            val b = BigInt.fromSlice(Sign.Plus, elm[1])
            val c = BigInt.fromSlice(Sign.Plus, elm[2])
            val d = BigInt.fromSlice(Sign.Plus, elm[3])

            assertEquals(a, b.checkedMul(c)!! + d)
            assertEquals(a, c.checkedMul(b)!! + d)
        }
    }

    @Test
    fun testCheckedDiv() {
        for ((aVec, bVec, cVec) in MUL_TRIPLES) {
            val a = BigInt.fromSlice(Sign.Plus, aVec)
            val b = BigInt.fromSlice(Sign.Plus, bVec)
            val c = BigInt.fromSlice(Sign.Plus, cVec)

            if (!a.isZero()) {
                assertEquals(b, c.checkedDiv(a)!!)
                assertEquals(b, (-c).checkedDiv(-a)!!)
                assertEquals(-b, (-c).checkedDiv(a)!!)
            }
            if (!b.isZero()) {
                assertEquals(a, c.checkedDiv(b)!!)
                assertEquals(a, (-c).checkedDiv(-b)!!)
                assertEquals(-a, (-c).checkedDiv(b)!!)
            }

            assertNull(c.checkedDiv(BigInt.zero()))
            assertNull((-c).checkedDiv(BigInt.zero()))
        }
    }

    @Test
    fun testGcd() {
        fun check(a: Int, b: Int, c: Int) {
            val bigA = BigInt.from(a)
            val bigB = BigInt.from(b)
            val bigC = BigInt.from(c)

            assertEquals(bigC, bigA.gcd(bigB))
            assertEquals(bigC, bigA.extendedGcd(bigB).gcd)
            assertEquals(bigC, bigA.gcdLcm(bigB).first)
            assertEquals(bigC, bigA.extendedGcdLcm(bigB).first.gcd)
        }

        check(10, 2, 2)
        check(10, 3, 1)
        check(0, 3, 3)
        check(3, 3, 3)
        check(56, 42, 14)
        check(3, -3, 3)
        check(-6, 3, 3)
        check(-4, -2, 2)
    }

    @Test
    fun testLcm() {
        fun check(a: Int, b: Int, c: Int) {
            val bigA = BigInt.from(a)
            val bigB = BigInt.from(b)
            val bigC = BigInt.from(c)

            assertEquals(bigC, bigA.lcm(bigB))
            assertEquals(bigC, bigA.gcdLcm(bigB).second)
            assertEquals(bigC, bigA.extendedGcdLcm(bigB).second)
        }

        check(0, 0, 0)
        check(1, 0, 0)
        check(0, 1, 0)
        check(1, 1, 1)
        check(-1, 1, 1)
        check(1, -1, 1)
        check(-1, -1, 1)
        check(8, 9, 72)
        check(11, 5, 55)
    }

    @Test
    fun testIsMultipleOf() {
        assertTrue(BigInt.from(0).isMultipleOf(BigInt.from(0)))
        assertTrue(BigInt.from(6).isMultipleOf(BigInt.from(6)))
        assertTrue(BigInt.from(6).isMultipleOf(BigInt.from(3)))
        assertTrue(BigInt.from(6).isMultipleOf(BigInt.from(1)))

        assertFalse(BigInt.from(42).isMultipleOf(BigInt.from(5)))
        assertFalse(BigInt.from(5).isMultipleOf(BigInt.from(3)))
        assertFalse(BigInt.from(42).isMultipleOf(BigInt.from(0)))
    }

    @Test
    fun testNextMultipleOf() {
        assertEquals(BigInt.from(16), BigInt.from(16).nextMultipleOf(BigInt.from(8)))
        assertEquals(BigInt.from(24), BigInt.from(23).nextMultipleOf(BigInt.from(8)))
        assertEquals(BigInt.from(16), BigInt.from(16).nextMultipleOf(BigInt.from(-8)))
        assertEquals(BigInt.from(16), BigInt.from(23).nextMultipleOf(BigInt.from(-8)))
        assertEquals(BigInt.from(-16), BigInt.from(-16).nextMultipleOf(BigInt.from(8)))
        assertEquals(BigInt.from(-16), BigInt.from(-23).nextMultipleOf(BigInt.from(8)))
        assertEquals(BigInt.from(-16), BigInt.from(-16).nextMultipleOf(BigInt.from(-8)))
        assertEquals(BigInt.from(-24), BigInt.from(-23).nextMultipleOf(BigInt.from(-8)))
    }

    @Test
    fun testPrevMultipleOf() {
        assertEquals(BigInt.from(16), BigInt.from(16).prevMultipleOf(BigInt.from(8)))
        assertEquals(BigInt.from(16), BigInt.from(23).prevMultipleOf(BigInt.from(8)))
        assertEquals(BigInt.from(16), BigInt.from(16).prevMultipleOf(BigInt.from(-8)))
        assertEquals(BigInt.from(24), BigInt.from(23).prevMultipleOf(BigInt.from(-8)))
        assertEquals(BigInt.from(-16), BigInt.from(-16).prevMultipleOf(BigInt.from(8)))
        assertEquals(BigInt.from(-24), BigInt.from(-23).prevMultipleOf(BigInt.from(8)))
        assertEquals(BigInt.from(-16), BigInt.from(-16).prevMultipleOf(BigInt.from(-8)))
        assertEquals(BigInt.from(-16), BigInt.from(-23).prevMultipleOf(BigInt.from(-8)))
    }

    @Test
    fun testAbsSub() {
        val zero = BigInt.zero()
        val one = BigInt.one()
        assertEquals(zero, (-one).absSub(one))
        assertEquals(zero, one.absSub(one))
        assertEquals(one, one.absSub(zero))
        val two = BigInt.from(2)
        assertEquals(two, one.absSub(-one))
    }

    @Test
    fun testFromStrRadix() {
        fun check(s: String, ans: Int?) {
            val expected = ans?.let { BigInt.from(it) }
            assertEquals(expected, BigInt.fromStrRadix(s, 10u).getOrNull())
        }
        check("10", 10)
        check("1", 1)
        check("0", 0)
        check("-1", -1)
        check("-10", -10)
        check("+10", 10)
        check("--7", null)
        check("++5", null)
        check("+-9", null)
        check("-+3", null)
        check("Z", null)
        check("_", null)

        // issue 10522, this hit an edge case that caused it to
        // attempt to allocate a vector of size (-1u) == huge.
        val x = BigInt.fromStrRadix("1" + "0".repeat(36), 10u).getOrThrow()
        val y = x.toString()
        assertEquals("1" + "0".repeat(36), y)
    }

    @Test
    fun testLowerHex() {
        val a = BigInt.parseBytes("A".encodeToByteArray(), 16u)!!
        val hello = BigInt.parseBytes("-22405534230753963835153736737".encodeToByteArray(), 10u)!!

        assertEquals("a", a.toLowerHexString())
        assertEquals("-48656c6c6f20776f726c6421", hello.toLowerHexString())
    }

    @Test
    fun testUpperHex() {
        val a = BigInt.parseBytes("A".encodeToByteArray(), 16u)!!
        val hello = BigInt.parseBytes("-22405534230753963835153736737".encodeToByteArray(), 10u)!!

        assertEquals("A", a.toUpperHexString())
        assertEquals("-48656C6C6F20776F726C6421", hello.toUpperHexString())
    }

    @Test
    fun testBinary() {
        val a = BigInt.parseBytes("A".encodeToByteArray(), 16u)!!
        val hello = BigInt.parseBytes("-224055342307539".encodeToByteArray(), 10u)!!

        assertEquals("1010", a.toBinaryString())
        assertEquals("-110010111100011011110011000101101001100011010011", hello.toBinaryString())
    }

    @Test
    fun testOctal() {
        val a = BigInt.parseBytes("A".encodeToByteArray(), 16u)!!
        val hello = BigInt.parseBytes("-22405534230753963835153736737".encodeToByteArray(), 10u)!!

        assertEquals("12", a.toOctalString())
        assertEquals("-22062554330674403566756233062041", hello.toOctalString())
    }

    @Test
    fun testDisplay() {
        val a = BigInt.parseBytes("A".encodeToByteArray(), 16u)!!
        val hello = BigInt.parseBytes("-22405534230753963835153736737".encodeToByteArray(), 10u)!!

        assertEquals("10", a.toString())
        assertEquals("-22405534230753963835153736737", hello.toString())
    }

    @Test
    fun testNeg() {
        assertEquals(BigInt.new(Sign.Minus, listOf(1u, 1u, 1u)), -BigInt.new(Sign.Plus, listOf(1u, 1u, 1u)))
        assertEquals(BigInt.new(Sign.Plus, listOf(1u, 1u, 1u)), -BigInt.new(Sign.Minus, listOf(1u, 1u, 1u)))
        val zero = BigInt.zero()
        assertEquals(zero, -zero)
    }

    @Test
    fun testNegativeShr() {
        assertEquals(BigInt.from(-1), BigInt.from(-1) shr 1)
        assertEquals(BigInt.from(-1), BigInt.from(-2) shr 1)
        assertEquals(BigInt.from(-2), BigInt.from(-3) shr 1)
        assertEquals(BigInt.from(-1), BigInt.from(-3) shr 2)
    }

    @Test
    fun testIterSum() {
        val result = BigInt.from(-1234567)
        val data =
            listOf(
                BigInt.from(-1000000),
                BigInt.from(-200000),
                BigInt.from(-30000),
                BigInt.from(-4000),
                BigInt.from(-500),
                BigInt.from(-60),
                BigInt.from(-7),
            )

        assertEquals(result, data.sumBigInt())
        assertEquals(result, data.asSequence().sumBigInt())
    }

    @Test
    fun testIterProduct() {
        val data =
            listOf(
                BigInt.from(1001),
                BigInt.from(-1002),
                BigInt.from(1003),
                BigInt.from(-1004),
                BigInt.from(1005),
            )
        val result = data[0] * data[1] * data[2] * data[3] * data[4]

        assertEquals(result, data.productBigInt())
        assertEquals(result, data.asSequence().productBigInt())
    }

    @Test
    fun testIterSumGeneric() {
        val result = BigInt.from(-1234567)
        val data = listOf(-1000000, -200000, -30000, -4000, -500, -60, -7)

        assertEquals(result, data.map { it.toBigInt() }.sumBigInt())
        assertEquals(result, data.asSequence().map { it.toBigInt() }.sumBigInt())
    }

    @Test
    fun testIterProductGeneric() {
        val data = listOf(1001, -1002, 1003, -1004, 1005)
        val result =
            data[0].toBigInt() *
                data[1].toBigInt() *
                data[2].toBigInt() *
                data[3].toBigInt() *
                data[4].toBigInt()

        assertEquals(result, data.map { it.toBigInt() }.productBigInt())
        assertEquals(result, data.asSequence().map { it.toBigInt() }.productBigInt())
    }

    @Test
    fun testPow() {
        val one = BigInt.from(1)
        val two = BigInt.from(2)
        val four = BigInt.from(4)
        val eight = BigInt.from(8)
        val minusTwo = BigInt.from(-2)

        assertEquals(one, two.pow(0u))
        assertEquals(two, two.pow(1u))
        assertEquals(four, two.pow(2u))
        assertEquals(eight, two.pow(3u))
        assertEquals(one, minusTwo.pow(0u), "-2^0")
        assertEquals(minusTwo, minusTwo.pow(1u), "-2^1")
        assertEquals(four, minusTwo.pow(2u), "-2^2")
        assertEquals(-eight, minusTwo.pow(3u), "-2^3")

        val pow1e10000 = BigInt.from(10u).pow(10_000u)
        val manual1e10000 = List(10_000) { BigInt.from(10u) }.productBigInt()
        assertEquals(pow1e10000, manual1e10000)
    }

    @Test
    fun testBit() {
        // 12 = (1100)_2
        assertFalse(BigInt.from(0b1100u).bit(0uL))
        assertFalse(BigInt.from(0b1100u).bit(1uL))
        assertTrue(BigInt.from(0b1100u).bit(2uL))
        assertTrue(BigInt.from(0b1100u).bit(3uL))
        assertFalse(BigInt.from(0b1100u).bit(4uL))
        assertFalse(BigInt.from(0b1100u).bit(200uL))
        assertFalse(BigInt.from(0b1100u).bit(ULong.MAX_VALUE))
        // -12 = (...110100)_2
        assertFalse(BigInt.from(-12).bit(0uL))
        assertFalse(BigInt.from(-12).bit(1uL))
        assertTrue(BigInt.from(-12).bit(2uL))
        assertFalse(BigInt.from(-12).bit(3uL))
        assertTrue(BigInt.from(-12).bit(4uL))
        assertTrue(BigInt.from(-12).bit(200uL))
        assertTrue(BigInt.from(-12).bit(ULong.MAX_VALUE))
    }

    @Test
    fun testSetBit() {
        var x: BigInt

        // zero
        x = BigInt.zero()
        x.setBit(200uL, true)
        assertEquals(BigInt.one() shl 200, x)
        x = BigInt.zero()
        x.setBit(200uL, false)
        assertEquals(BigInt.zero(), x)

        // positive numbers
        x = BigInt.fromBiguint(Sign.Plus, BigUint.one() shl 200)
        x.setBit(10uL, true)
        x.setBit(200uL, false)
        assertEquals(BigInt.one() shl 10, x)
        x.setBit(10uL, false)
        x.setBit(5uL, false)
        assertEquals(BigInt.zero(), x)

        // negative numbers
        x = BigInt.from(-12)
        x.setBit(200uL, true)
        assertEquals(BigInt.from(-12), x)
        x.setBit(200uL, false)
        assertEquals(
            BigInt.fromBiguint(Sign.Minus, BigUint.fromUInt(12u) bitOr (BigUint.one() shl 200)),
            x,
        )
        x.setBit(6uL, false)
        assertEquals(
            BigInt.fromBiguint(Sign.Minus, BigUint.fromUInt(76u) bitOr (BigUint.one() shl 200)),
            x,
        )
        x.setBit(6uL, true)
        assertEquals(
            BigInt.fromBiguint(Sign.Minus, BigUint.fromUInt(12u) bitOr (BigUint.one() shl 200)),
            x,
        )
        x.setBit(200uL, true)
        assertEquals(BigInt.from(-12), x)

        x = BigInt.fromBiguint(Sign.Minus, BigUint.one() shl 30)
        x.setBit(10uL, true)
        assertEquals(
            BigInt.fromBiguint(Sign.Minus, (BigUint.one() shl 30) - (BigUint.one() shl 10)),
            x,
        )

        x = BigInt.fromBiguint(Sign.Minus, BigUint.one() shl 200)
        x.setBit(40uL, true)
        assertEquals(
            BigInt.fromBiguint(Sign.Minus, (BigUint.one() shl 200) - (BigUint.one() shl 40)),
            x,
        )

        x = BigInt.fromBiguint(Sign.Minus, (BigUint.one() shl 200) bitOr (BigUint.one() shl 100))
        x.setBit(100uL, false)
        assertEquals(
            BigInt.fromBiguint(Sign.Minus, (BigUint.one() shl 200) bitOr (BigUint.one() shl 101)),
            x,
        )

        x = BigInt.fromBiguint(Sign.Minus, (BigUint.one() shl 63) bitOr (BigUint.one() shl 62))
        x.setBit(62uL, false)
        assertEquals(BigInt.fromBiguint(Sign.Minus, BigUint.one() shl 64), x)

        x = BigInt.fromBiguint(Sign.Minus, (BigUint.one() shl 200) - BigUint.one())
        x.setBit(0uL, false)
        assertEquals(BigInt.fromBiguint(Sign.Minus, BigUint.one() shl 200), x)
    }
}
