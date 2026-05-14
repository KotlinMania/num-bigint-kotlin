// port-lint: source biguint.rs
package io.github.kotlinmania.numbigint

import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * A big unsigned integer type.
 */
class BigUint internal constructor(
    internal val data: MutableList<BigDigit>,
) : Comparable<BigUint> {
    fun clone(): BigUint {
        return BigUint(data.toMutableList())
    }

    fun cloneFrom(other: BigUint) {
        data.clear()
        data.addAll(other.data)
    }

    override fun hashCode(): Int {
        check(data.lastOrNull() != 0u)
        return data.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        check(data.lastOrNull() != 0u)
        return other is BigUint &&
            other.data.lastOrNull() != 0u &&
            data == other.data
    }

    override fun compareTo(other: BigUint): Int {
        return cmpSlice(data, other.data)
    }

    override fun toString(): String {
        return toStrRadix(10u)
    }

    fun toDebugString(): String {
        return toString()
    }

    fun toLowerHexString(): String {
        return toStrRadix(16u)
    }

    fun toUpperHexString(): String {
        return toStrRadix(16u).uppercase()
    }

    fun toBinaryString(): String {
        return toStrRadix(2u)
    }

    fun toOctalString(): String {
        return toStrRadix(8u)
    }

    fun setZero() {
        data.clear()
    }

    fun isZero(): Boolean {
        return data.isEmpty()
    }

    fun setOne() {
        data.clear()
        data.add(1u)
    }

    fun isOne(): Boolean {
        return data.size == 1 && data[0] == 1u
    }

    fun divRem(other: BigUint): Pair<BigUint, BigUint> {
        return divRemRef(this, other)
    }

    fun divFloor(other: BigUint): BigUint {
        val (d, _) = divRemRef(this, other)
        return d
    }

    fun modFloor(other: BigUint): BigUint {
        val (_, m) = divRemRef(this, other)
        return m
    }

    fun divModFloor(other: BigUint): Pair<BigUint, BigUint> {
        return divRemRef(this, other)
    }

    fun divCeil(other: BigUint): BigUint {
        val (d, m) = divRemRef(this, other)
        return if (m.isZero()) d else d + 1u
    }

    /**
     * Calculates the greatest common divisor of the number and `other`.
     *
     * The result is always positive.
     */
    fun gcd(other: BigUint): BigUint {
        fun twos(x: BigUint): ULong = x.trailingZeros() ?: 0uL

        // Stein's algorithm
        if (isZero()) {
            return other.clone()
        }
        if (other.isZero()) {
            return clone()
        }
        var m = clone()
        var n = other.clone()

        // find common factors of 2
        val shift = minOf(twos(n), twos(m))

        // divide m and n by 2 until odd
        // m inside loop
        n = n.shiftRight(twos(n))

        while (!m.isZero()) {
            m = m.shiftRight(twos(m))
            if (n > m) {
                val tmp = n
                n = m
                m = tmp
            }
            m.minusAssign(n)
        }

        return n.shiftLeft(shift)
    }

    /**
     * Calculates the lowest common multiple of the number and `other`.
     */
    fun lcm(other: BigUint): BigUint {
        return if (isZero() && other.isZero()) {
            ZERO
        } else {
            this / gcd(other) * other
        }
    }

    /**
     * Calculates the greatest common divisor and lowest common multiple together.
     */
    fun gcdLcm(other: BigUint): Pair<BigUint, BigUint> {
        val gcd = gcd(other)
        val lcm = if (gcd.isZero()) {
            ZERO
        } else {
            this / gcd * other
        }
        return Pair(gcd, lcm)
    }

    /**
     * Deprecated, use [isMultipleOf] instead.
     */
    fun divides(other: BigUint): Boolean {
        return isMultipleOf(other)
    }

    /**
     * Returns `true` if the number is a multiple of `other`.
     */
    fun isMultipleOf(other: BigUint): Boolean {
        if (other.isZero()) {
            return isZero()
        }
        return (this % other).isZero()
    }

    /**
     * Returns `true` if the number is divisible by `2`.
     */
    fun isEven(): Boolean {
        // Considering only the last digit.
        return data.firstOrNull()?.let { it and 1u == 0u } ?: true
    }

    /**
     * Returns `true` if the number is not divisible by `2`.
     */
    fun isOdd(): Boolean {
        return !isEven()
    }

    /**
     * Rounds up to nearest multiple of argument.
     */
    fun nextMultipleOf(other: BigUint): BigUint {
        val m = modFloor(other)
        return if (m.isZero()) clone() else this + (other - m)
    }

    /**
     * Rounds down to nearest multiple of argument.
     */
    fun prevMultipleOf(other: BigUint): BigUint {
        return this - modFloor(other)
    }

    fun dec(): BigUint {
        minusAssign(1u)
        return this
    }

    fun inc(): BigUint {
        plusAssign(1u)
        return this
    }

    /**
     * Creates and initializes a `BigUint`.
     *
     * The base 2^32 digits are ordered least significant digit first.
     */
    fun assignFromSlice(slice: List<UInt>) {
        data.clear()
        data.addAll(slice)
        normalize()
    }

    /**
     * Returns the byte representation of the `BigUint` in big-endian byte order.
     */
    fun toBytesBe(): List<UByte> {
        val v = toBytesLe().toMutableList()
        v.reverse()
        return v
    }

    /**
     * Returns the byte representation of the `BigUint` in little-endian byte order.
     */
    fun toBytesLe(): List<UByte> {
        return if (isZero()) {
            listOf(0u)
        } else {
            toBitwiseDigitsLe(this, 8u)
        }
    }

    /**
     * Returns the `UInt` digits representation of the `BigUint` ordered least significant digit
     * first.
     */
    fun toU32Digits(): List<UInt> {
        return iterU32Digits().asSequence().toList()
    }

    /**
     * Returns the `ULong` digits representation of the `BigUint` ordered least significant digit
     * first.
     */
    fun toU64Digits(): List<ULong> {
        return iterU64Digits().asSequence().toList()
    }

    /**
     * Returns an iterator of `UInt` digits representation of the `BigUint` ordered least
     * significant digit first.
     */
    fun iterU32Digits(): U32Digits {
        return U32Digits(data)
    }

    /**
     * Returns an iterator of `ULong` digits representation of the `BigUint` ordered least
     * significant digit first.
     */
    fun iterU64Digits(): U64Digits {
        return U64Digits(data)
    }

    /**
     * Returns the integer formatted as a string in the given radix.
     * `radix` must be in the range `2...36`.
     */
    fun toStrRadix(radix: UInt): String {
        val v = toStrRadixReversed(this, radix).toMutableList()
        v.reverse()
        return v.map { it.toInt().toChar() }.joinToString("")
    }

    /**
     * Returns the integer in the requested base in big-endian digit order.
     * The output is not given in a human readable alphabet but as a zero
     * based `UByte` number.
     * `radix` must be in the range `2...256`.
     */
    fun toRadixBe(radix: UInt): List<UByte> {
        val v = toRadixLe(radix).toMutableList()
        v.reverse()
        return v
    }

    /**
     * Returns the integer in the requested base in little-endian digit order.
     * The output is not given in a human readable alphabet but as a zero
     * based `UByte` number.
     * `radix` must be in the range `2...256`.
     */
    fun toRadixLe(radix: UInt): List<UByte> {
        return toRadixLeDigits(this, radix)
    }

    /**
     * Determines the fewest bits necessary to express the `BigUint`.
     */
    fun bits(): ULong {
        if (isZero()) {
            return 0uL
        }
        val zeros = data.last().countLeadingZeroBits().toULong()
        return data.size.toULong() * BIG_DIGIT_BITS.toULong() - zeros
    }

    /**
     * Strips off trailing zero bigdigits; comparisons require the last element in the vector to
     * be nonzero.
     */
    internal fun normalize() {
        if (data.lastOrNull() == 0u) {
            val len = data.indexOfLast { it != 0u } + 1
            while (data.size > len) {
                data.removeAt(data.lastIndex)
            }
        }
    }

    /**
     * Returns a normalized `BigUint`.
     */
    internal fun normalized(): BigUint {
        normalize()
        return this
    }

    /**
     * Returns `self ^ exponent`.
     */
    fun pow(exponent: UInt): BigUint {
        return powBigUint(this, exponent)
    }

    /**
     * Returns `(self ^ exponent) % modulus`.
     *
     * Panics if the modulus is zero.
     */
    fun modpow(exponent: BigUint, modulus: BigUint): BigUint {
        return modpowBigUint(this, exponent, modulus)
    }

    /**
     * Returns the modular multiplicative inverse if it exists, otherwise `null`.
     *
     * This solves for `x` in the interval `[0, modulus)` such that `self * x == 1 (mod modulus)`.
     * The solution exists if and only if `gcd(self, modulus) == 1`.
     */
    fun modinv(modulus: BigUint): BigUint? {
        // Based on the modular inverse pseudocode for the extended Euclidean algorithm.
        // Optimization note: consider Binary or Lehmer's GCD algorithms.
        require(!modulus.isZero()) { "attempt to calculate with zero modulus!" }
        if (modulus.isOne()) {
            return zero()
        }

        var r0: BigUint
        var r1 = this % modulus
        var t0: BigUint
        var t1: BigUint

        // Lift and simplify the first iteration to avoid some initial allocations.
        if (r1.isZero()) {
            return null
        } else if (r1.isOne()) {
            return r1
        } else {
            val (q, r2) = modulus.divRem(r1)
            if (r2.isZero()) {
                return null
            }
            r0 = r1
            r1 = r2
            t0 = one()
            t1 = modulus - q
        }

        while (!r1.isZero()) {
            val (q, r2) = r0.divRem(r1)
            r0 = r1
            r1 = r2

            // Equivalent expression: t2 = (t0 - q * t1) % modulus.
            val qt1 = q * t1 % modulus
            val t2 = if (t0 < qt1) {
                t0 + (modulus - qt1)
            } else {
                t0 - qt1
            }
            t0 = t1
            t1 = t2
        }

        return if (r0.isOne()) t0 else null
    }

    /**
     * Returns the truncated principal square root of `self`.
     */
    fun sqrt(): BigUint {
        if (isZero() || isOne()) {
            return clone()
        }
        toULongOrNull()?.let { return it.sqrt().toBigUint() }
        val bits = bits()
        val maxBits = bits / 2uL + 1uL
        val guess = toDoubleOrNull()?.takeIf { it.isFinite() }?.let {
            fromDouble(sqrt(it)) ?: one().shiftLeft(maxBits)
        } ?: run {
            val extraBits = bits - (DOUBLE_MAX_EXPONENT.toULong() - 1uL)
            val rootScale = (extraBits + 1uL) / 2uL
            val scale = rootScale * 2uL
            shiftRight(scale).sqrt().shiftLeft(rootScale)
        }
        return fixpoint(guess, maxBits) { s ->
            val q = this / s
            val t = s + q
            t.shiftRight(1uL)
        }
    }

    /**
     * Returns the truncated principal cube root of `self`.
     */
    fun cbrt(): BigUint {
        if (isZero() || isOne()) {
            return clone()
        }
        toULongOrNull()?.let { return it.cbrt().toBigUint() }
        val bits = bits()
        val maxBits = bits / 3uL + 1uL
        val guess = toDoubleOrNull()?.takeIf { it.isFinite() }?.let {
            fromDouble(it.pow(1.0 / 3.0)) ?: one().shiftLeft(maxBits)
        } ?: run {
            val extraBits = bits - (DOUBLE_MAX_EXPONENT.toULong() - 1uL)
            val rootScale = (extraBits + 2uL) / 3uL
            val scale = rootScale * 3uL
            shiftRight(scale).cbrt().shiftLeft(rootScale)
        }
        return fixpoint(guess, maxBits) { s ->
            val q = this / (s * s)
            val t = s.shiftLeft(1uL) + q
            t / 3u
        }
    }

    /**
     * Returns the truncated principal `n`th root of `self`.
     */
    fun nthRoot(n: UInt): BigUint {
        require(n > 0u) { "root degree n must be at least 1" }
        if (isZero() || isOne()) {
            return clone()
        }
        when (n) {
            1u -> return clone()
            2u -> return sqrt()
            3u -> return cbrt()
        }

        // The root of non-zero values less than 2^n can only be 1.
        val bits = bits()
        val n64 = n.toULong()
        if (bits <= n64) {
            return one()
        }

        toULongOrNull()?.let { return it.nthRoot(n).toBigUint() }

        val maxBits = bits / n64 + 1uL
        val guess = toDoubleOrNull()?.takeIf { it.isFinite() }?.let {
            fromDouble(exp(ln(it) / n.toDouble())) ?: one().shiftLeft(maxBits)
        } ?: run {
            val extraBits = bits - (DOUBLE_MAX_EXPONENT.toULong() - 1uL)
            val rootScale = divCeil(extraBits, n64)
            val scale = rootScale * n64
            if (scale < bits && bits - scale > n64) {
                shiftRight(scale).nthRoot(n).shiftLeft(rootScale)
            } else {
                one().shiftLeft(maxBits)
            }
        }

        val nMin1 = n - 1u
        return fixpoint(guess, maxBits) { s ->
            val q = this / s.pow(nMin1)
            val t = nMin1 * s + q
            t / n
        }
    }

    /**
     * Returns the number of least-significant bits that are zero,
     * or `null` if the entire number is zero.
     */
    fun trailingZeros(): ULong? {
        val i = data.indexOfFirst { it != 0u }
        if (i < 0) {
            return null
        }
        val zeros = data[i].countTrailingZeroBits().toULong()
        return i.toULong() * BIG_DIGIT_BITS.toULong() + zeros
    }

    /**
     * Returns the number of least-significant bits that are ones.
     */
    fun trailingOnes(): ULong {
        val i = data.indexOfFirst { it.inv() != 0u }
        return if (i >= 0) {
            val ones = data[i].countTrailingZeroBits().toULong()
            i.toULong() * BIG_DIGIT_BITS.toULong() + ones
        } else {
            data.size.toULong() * BIG_DIGIT_BITS.toULong()
        }
    }

    /**
     * Returns the number of one bits.
     */
    fun countOnes(): ULong {
        return data.fold(0uL) { sum, digit -> sum + digit.countOneBits().toULong() }
    }

    /**
     * Returns whether the bit in the given position is set.
     */
    fun bit(bit: ULong): Boolean {
        val bitsPerDigit = BIG_DIGIT_BITS.toULong()
        val digitIndex = bit / bitsPerDigit
        if (digitIndex <= Int.MAX_VALUE.toULong()) {
            data.getOrNull(digitIndex.toInt())?.let { digit ->
                val bitMask = 1u shl (bit % bitsPerDigit).toInt()
                return digit and bitMask != 0u
            }
        }
        return false
    }

    /**
     * Sets or clears the bit in the given position.
     *
     * Note that setting a bit greater than the current bit length may need a reallocation
     * to store the new digits.
     */
    fun setBit(bit: ULong, value: Boolean) {
        // Note: we're saturating `digitIndex` and `newLen`; any such case is guaranteed to
        // fail allocation, and that's more consistent than adding our own overflow panics.
        val bitsPerDigit = BIG_DIGIT_BITS.toULong()
        val digitIndexLong = bit / bitsPerDigit
        check(digitIndexLong <= Int.MAX_VALUE.toULong())
        val digitIndex = digitIndexLong.toInt()
        val bitMask = 1u shl (bit % bitsPerDigit).toInt()
        if (value) {
            while (digitIndex >= data.size) {
                data.add(0u)
            }
            data[digitIndex] = data[digitIndex] or bitMask
        } else if (digitIndex < data.size) {
            data[digitIndex] = data[digitIndex] and bitMask.inv()
            // the top bit may have been cleared, so normalize
            normalize()
        }
    }

    companion object {
        /**
         * A constant `BigUint` with value 0, useful for static initialization.
         */
        val ZERO: BigUint
            get() = BigUint(mutableListOf())

        /**
         * Creates and initializes a `BigUint`.
         *
         * The base 2^32 digits are ordered least significant digit first.
         */
        fun new(digits: List<UInt>): BigUint {
            val big = ZERO
            big.data.addAll(digits)
            big.normalize()
            return big
        }

        /**
         * Creates and initializes a `BigUint`.
         *
         * The base 2^32 digits are ordered least significant digit first.
         */
        fun fromSlice(slice: List<UInt>): BigUint {
            val big = ZERO
            big.assignFromSlice(slice)
            return big
        }

        /**
         * Creates and initializes a `BigUint`.
         *
         * The bytes are in big-endian byte order.
         */
        fun fromBytesBe(bytes: List<UByte>): BigUint {
            return if (bytes.isEmpty()) {
                ZERO
            } else {
                val v = bytes.toMutableList()
                v.reverse()
                fromBytesLe(v)
            }
        }

        /**
         * Creates and initializes a `BigUint`.
         *
         * The bytes are in little-endian byte order.
         */
        fun fromBytesLe(bytes: List<UByte>): BigUint {
            return if (bytes.isEmpty()) {
                ZERO
            } else {
                fromBitwiseDigitsLe(bytes, 8u)
            }
        }

        /**
         * Creates and initializes a `BigUint`. The input slice must contain
         * ASCII or UTF-8 characters in `[0-9a-zA-Z]`.
         * `radix` must be in the range `2...36`.
         *
         * The function [fromStrRadix] provides the same logic for `String` buffers.
         */
        fun parseBytes(buf: ByteArray, radix: UInt): BigUint? {
            val s = runCatching { buf.decodeToString(throwOnInvalidSequence = true) }.getOrNull()
                ?: return null
            return fromStrRadix(s, radix).getOrNull()
        }

        /**
         * Creates and initializes a `BigUint`. Each `UByte` of the input slice is
         * interpreted as one digit of the number and must therefore be less than `radix`.
         *
         * The bytes are in big-endian byte order.
         * `radix` must be in the range `2...256`.
         */
        fun fromRadixBe(buf: List<UByte>, radix: UInt): BigUint? {
            return fromRadixBeDigits(buf, radix)
        }

        /**
         * Creates and initializes a `BigUint`. Each `UByte` of the input slice is
         * interpreted as one digit of the number and must therefore be less than `radix`.
         *
         * The bytes are in little-endian byte order.
         * `radix` must be in the range `2...256`.
         */
        fun fromRadixLe(buf: List<UByte>, radix: UInt): BigUint? {
            return fromRadixLeDigits(buf, radix)
        }

        fun zero(): BigUint = ZERO

        fun one(): BigUint {
            return BigUint(mutableListOf(1u))
        }

        fun fromStrRadix(s: String, radix: UInt): Result<BigUint> {
            return fromStrRadixImpl(s, radix)
        }

        fun fromULong(n: ULong): BigUint {
            return fromULongImpl(n)
        }

        fun fromUInt(n: UInt): BigUint {
            return fromULong(n.toULong())
        }

        fun fromDouble(n: Double): BigUint? {
            return fromDoubleImpl(n)
        }
    }
}

fun zero(): BigUint = BigUint.zero()

fun one(): BigUint = BigUint.one()

fun biguintFromVec(digits: MutableList<BigDigit>): BigUint {
    return BigUint(digits).normalized()
}

fun cmpSlice(a: List<BigDigit>, b: List<BigDigit>): Int {
    check(a.lastOrNull() != 0u)
    check(b.lastOrNull() != 0u)
    val lenCompare = a.size.compareTo(b.size)
    if (lenCompare != 0) {
        return lenCompare
    }
    for (i in a.indices.reversed()) {
        val cmp = a[i].compareTo(b[i])
        if (cmp != 0) {
            return cmp
        }
    }
    return 0
}

fun fixpoint(x0: BigUint, maxBits: ULong, f: (BigUint) -> BigUint): BigUint {
    var x = x0
    var xn = f(x)

    // If the value increased, then the initial guess must have been low.
    // Repeat until we reverse course.
    while (x < xn) {
        // Sometimes an increase will go way too far, especially with large
        // powers, and then take a long time to walk back. We know an upper
        // bound based on bit size, so saturate on that.
        x = if (xn.bits() > maxBits) {
            one().shiftLeft(maxBits)
        } else {
            xn
        }
        xn = f(x)
    }

    // Now keep repeating while the estimate is decreasing.
    while (x > xn) {
        x = xn
        xn = f(x)
    }
    return x
}

/**
 * A generic trait for converting a value to a `BigUint`.
 */
interface ToBigUint {
    /**
     * Converts the value of `self` to a `BigUint`.
     */
    fun toBigUint(): BigUint?
}

internal interface IntDigits {
    fun digits(): List<BigDigit>
    fun digitsMut(): MutableList<BigDigit>
    fun normalize()
    fun capacity(): Int
    fun len(): Int
}

internal fun BigUint.asIntDigits(): IntDigits {
    return BigUintDigits(this)
}

private class BigUintDigits(
    private val value: BigUint,
) : IntDigits {
    override fun digits(): List<BigDigit> {
        return value.data
    }

    override fun digitsMut(): MutableList<BigDigit> {
        return value.data
    }

    override fun normalize() {
        value.normalize()
    }

    override fun capacity(): Int {
        return value.data.size
    }

    override fun len(): Int {
        return value.data.size
    }
}

/**
 * Convert a `UInt` chunk with length either 1 or 2 to a single `ULong` digit.
 */
internal fun u32ChunkToU64(chunk: List<UInt>): ULong {
    // raw could have odd length
    var digit = chunk[0].toULong()
    chunk.getOrNull(1)?.let { hi ->
        digit = digit or (hi.toULong() shl 32)
    }
    return digit
}

private fun divCeil(a: ULong, b: ULong): ULong {
    val (d, m) = Pair(a / b, a % b)
    return if (m == 0uL) d else d + 1uL
}

private const val DOUBLE_MAX_EXPONENT: Int = 1024
