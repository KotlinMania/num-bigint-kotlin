// port-lint: source bigint.rs
package io.github.kotlinmania.numbigint

import io.github.kotlinmania.numbigint.bigint.plus
import io.github.kotlinmania.numbigint.bigint.plusAssign
import io.github.kotlinmania.numbigint.bigint.minus
import io.github.kotlinmania.numbigint.bigint.minusAssign
import io.github.kotlinmania.numbigint.bigint.times
import io.github.kotlinmania.numbigint.bigint.timesAssign

/**
 * A `Sign` is a `BigInt`'s composing element.
 */
enum class Sign {
    Minus,
    NoSign,
    Plus,
}

operator fun Sign.unaryMinus(): Sign {
    return when (this) {
        Sign.Minus -> Sign.Plus
        Sign.NoSign -> Sign.NoSign
        Sign.Plus -> Sign.Minus
    }
}

operator fun Sign.times(other: Sign): Sign {
    return when {
        this == Sign.NoSign || other == Sign.NoSign -> Sign.NoSign
        this == other -> Sign.Plus
        else -> Sign.Minus
    }
}

/**
 * A big signed integer type.
 */
class BigInt internal constructor(
    private var signValue: Sign,
    internal var data: BigUint,
) : Comparable<BigInt> {
    fun clone(): BigInt {
        return BigInt(signValue, data.clone())
    }

    fun cloneFrom(other: BigInt) {
        signValue = other.signValue
        data.cloneFrom(other.data)
    }

    override fun hashCode(): Int {
        check((signValue != Sign.NoSign) xor data.isZero())
        var result = signValue.hashCode()
        if (signValue != Sign.NoSign) {
            result = 31 * result + data.hashCode()
        }
        return result
    }

    override fun equals(other: Any?): Boolean {
        check((signValue != Sign.NoSign) xor data.isZero())
        return other is BigInt &&
            ((other.signValue != Sign.NoSign) xor other.data.isZero()) &&
            signValue == other.signValue &&
            (signValue == Sign.NoSign || data == other.data)
    }

    override fun compareTo(other: BigInt): Int {
        check((signValue != Sign.NoSign) xor data.isZero())
        check((other.signValue != Sign.NoSign) xor other.data.isZero())
        val signCompare = signValue.compareTo(other.signValue)
        if (signCompare != 0) {
            return signCompare
        }

        return when (signValue) {
            Sign.NoSign -> 0
            Sign.Plus -> data.compareTo(other.data)
            Sign.Minus -> other.data.compareTo(data)
        }
    }

    override fun toString(): String {
        return if (isNegative()) "-${data.toStrRadix(10u)}" else data.toStrRadix(10u)
    }

    fun toDebugString(): String {
        return toString()
    }

    fun toBinaryString(): String {
        return if (isNegative()) "-${data.toStrRadix(2u)}" else data.toStrRadix(2u)
    }

    fun toOctalString(): String {
        return if (isNegative()) "-${data.toStrRadix(8u)}" else data.toStrRadix(8u)
    }

    fun toLowerHexString(): String {
        return if (isNegative()) "-${data.toStrRadix(16u)}" else data.toStrRadix(16u)
    }

    fun toUpperHexString(): String {
        val s = data.toStrRadix(16u).uppercase()
        return if (isNegative()) "-$s" else s
    }

    // !-2 = !...f fe = ...0 01 = +1
    // !-1 = !...f ff = ...0 00 =  0
    // ! 0 = !...0 00 = ...f ff = -1
    // !+1 = !...0 01 = ...f fe = -2
    operator fun not(): BigInt {
        val result = clone()
        when (result.signValue) {
            Sign.NoSign, Sign.Plus -> {
                result.data.plusAssign(1u)
                result.signValue = Sign.Minus
            }
            Sign.Minus -> {
                result.data.minusAssign(1u)
                result.signValue = if (result.data.isZero()) Sign.NoSign else Sign.Plus
            }
        }
        return result
    }

    fun setZero() {
        data.setZero()
        signValue = Sign.NoSign
    }

    fun isZero(): Boolean {
        return signValue == Sign.NoSign
    }

    fun setOne() {
        data.setOne()
        signValue = Sign.Plus
    }

    fun isOne(): Boolean {
        return signValue == Sign.Plus && data.isOne()
    }

    fun abs(): BigInt {
        return when (signValue) {
            Sign.Plus, Sign.NoSign -> clone()
            Sign.Minus -> from(data.clone())
        }
    }

    fun absSub(other: BigInt): BigInt {
        return if (this <= other) ZERO else this - other
    }

    fun signum(): BigInt {
        return when (signValue) {
            Sign.Plus -> one()
            Sign.Minus -> -one()
            Sign.NoSign -> ZERO
        }
    }

    fun isPositive(): Boolean {
        return signValue == Sign.Plus
    }

    fun isNegative(): Boolean {
        return signValue == Sign.Minus
    }

    operator fun unaryMinus(): BigInt {
        val result = clone()
        result.signValue = -result.signValue
        return result
    }

    fun divRem(other: BigInt): Pair<BigInt, BigInt> {
        // r.sign == self.sign
        val (dUi, rUi) = data.divRem(other.data)
        val d = fromBiguint(signValue, dUi)
        val r = fromBiguint(signValue, rUi)
        return if (other.isNegative()) Pair(-d, r) else Pair(d, r)
    }

    fun divFloor(other: BigInt): BigInt {
        val (dUi, m) = data.divModFloor(other.data)
        val d = from(dUi)
        return when (Pair(signValue, other.signValue)) {
            Pair(Sign.Plus, Sign.Plus), Pair(Sign.NoSign, Sign.Plus), Pair(Sign.Minus, Sign.Minus) -> d
            Pair(Sign.Plus, Sign.Minus), Pair(Sign.NoSign, Sign.Minus), Pair(Sign.Minus, Sign.Plus) -> {
                if (m.isZero()) -d else -d - 1u
            }
            else -> throw ArithmeticException("attempt to divide by zero")
        }
    }

    fun modFloor(other: BigInt): BigInt {
        // m.sign == other.sign
        val mUi = data.modFloor(other.data)
        val m = fromBiguint(other.signValue, mUi)
        return when (Pair(signValue, other.signValue)) {
            Pair(Sign.Plus, Sign.Plus), Pair(Sign.NoSign, Sign.Plus), Pair(Sign.Minus, Sign.Minus) -> m
            Pair(Sign.Plus, Sign.Minus), Pair(Sign.NoSign, Sign.Minus), Pair(Sign.Minus, Sign.Plus) -> {
                if (m.isZero()) m else other - m
            }
            else -> throw ArithmeticException("attempt to divide by zero")
        }
    }

    fun divModFloor(other: BigInt): Pair<BigInt, BigInt> {
        // m.sign == other.sign
        val (dUi, mUi) = data.divModFloor(other.data)
        val d = from(dUi)
        val m = fromBiguint(other.signValue, mUi)
        return when (Pair(signValue, other.signValue)) {
            Pair(Sign.Plus, Sign.Plus), Pair(Sign.NoSign, Sign.Plus), Pair(Sign.Minus, Sign.Minus) -> Pair(d, m)
            Pair(Sign.Plus, Sign.Minus), Pair(Sign.NoSign, Sign.Minus), Pair(Sign.Minus, Sign.Plus) -> {
                if (m.isZero()) Pair(-d, m) else Pair(-d - 1u, other - m)
            }
            else -> throw ArithmeticException("attempt to divide by zero")
        }
    }

    fun divCeil(other: BigInt): BigInt {
        val (dUi, m) = data.divModFloor(other.data)
        val d = from(dUi)
        return when (Pair(signValue, other.signValue)) {
            Pair(Sign.Plus, Sign.Minus), Pair(Sign.NoSign, Sign.Minus), Pair(Sign.Minus, Sign.Plus) -> -d
            Pair(Sign.Plus, Sign.Plus), Pair(Sign.NoSign, Sign.Plus), Pair(Sign.Minus, Sign.Minus) -> {
                if (m.isZero()) d else d + 1u
            }
            else -> throw ArithmeticException("attempt to divide by zero")
        }
    }

    /**
     * Calculates the greatest common divisor of the number and `other`.
     *
     * The result is always positive.
     */
    fun gcd(other: BigInt): BigInt {
        return from(data.gcd(other.data))
    }

    /**
     * Calculates the lowest common multiple of the number and `other`.
     */
    fun lcm(other: BigInt): BigInt {
        return from(data.lcm(other.data))
    }

    /**
     * Calculates the greatest common divisor and lowest common multiple together.
     */
    fun gcdLcm(other: BigInt): Pair<BigInt, BigInt> {
        val (gcd, lcm) = data.gcdLcm(other.data)
        return Pair(from(gcd), from(lcm))
    }

    /**
     * Deprecated, use [isMultipleOf] instead.
     */
    fun divides(other: BigInt): Boolean {
        return isMultipleOf(other)
    }

    /**
     * Returns `true` if the number is a multiple of `other`.
     */
    fun isMultipleOf(other: BigInt): Boolean {
        return data.isMultipleOf(other.data)
    }

    /**
     * Returns `true` if the number is divisible by `2`.
     */
    fun isEven(): Boolean {
        return data.isEven()
    }

    /**
     * Returns `true` if the number is not divisible by `2`.
     */
    fun isOdd(): Boolean {
        return data.isOdd()
    }

    /**
     * Rounds up to nearest multiple of argument.
     */
    fun nextMultipleOf(other: BigInt): BigInt {
        val m = modFloor(other)
        return if (m.isZero()) clone() else this + (other - m)
    }

    /**
     * Rounds down to nearest multiple of argument.
     */
    fun prevMultipleOf(other: BigInt): BigInt {
        return this - modFloor(other)
    }

    fun dec() {
        minusAssign(1u)
    }

    fun inc() {
        this.plusAssign(1u)
    }

    fun normalize() {
        data.normalize()
        if (data.isZero()) {
            signValue = Sign.NoSign
        }
    }

    /**
     * Reinitializes a `BigInt`.
     *
     * The base 2^32 digits are ordered least significant digit first.
     */
    fun assignFromSlice(sign: Sign, slice: List<UInt>) {
        if (sign == Sign.NoSign) {
            setZero()
        } else {
            data.assignFromSlice(slice)
            signValue = if (data.isZero()) Sign.NoSign else sign
        }
    }

    /**
     * Returns the sign and the byte representation of the `BigInt` in big-endian byte order.
     */
    fun toBytesBe(): Pair<Sign, List<UByte>> {
        return Pair(signValue, data.toBytesBe())
    }

    /**
     * Returns the sign and the byte representation of the `BigInt` in little-endian byte order.
     */
    fun toBytesLe(): Pair<Sign, List<UByte>> {
        return Pair(signValue, data.toBytesLe())
    }

    /**
     * Returns the sign and the `UInt` digits representation of the `BigInt` ordered least
     * significant digit first.
     */
    fun toU32Digits(): Pair<Sign, List<UInt>> {
        return Pair(signValue, data.toU32Digits())
    }

    /**
     * Returns the sign and the `ULong` digits representation of the `BigInt` ordered least
     * significant digit first.
     */
    fun toU64Digits(): Pair<Sign, List<ULong>> {
        return Pair(signValue, data.toU64Digits())
    }

    /**
     * Returns an iterator of `UInt` digits representation of the `BigInt` ordered least
     * significant digit first.
     */
    fun iterU32Digits(): U32Digits {
        return data.iterU32Digits()
    }

    /**
     * Returns an iterator of `ULong` digits representation of the `BigInt` ordered least
     * significant digit first.
     */
    fun iterU64Digits(): U64Digits {
        return data.iterU64Digits()
    }

    /**
     * Returns the two's-complement byte representation of the `BigInt` in big-endian byte order.
     */
    fun toSignedBytesBe(): List<UByte> {
        val v = toSignedBytesLe().toMutableList()
        v.reverse()
        return v
    }

    /**
     * Returns the two's-complement byte representation of the `BigInt` in little-endian byte order.
     */
    fun toSignedBytesLe(): List<UByte> {
        return when (signValue) {
            Sign.NoSign -> listOf(0u)
            Sign.Plus -> {
                val bytes = data.toBytesLe().toMutableList()
                if (bytes.last().toUInt() and 0x80u != 0u) {
                    bytes.add(0u)
                }
                bytes
            }
            Sign.Minus -> {
                val magnitudeMinusOne = data - 1u
                val bytes = magnitudeMinusOne.toBytesLe().toMutableList()
                var i = 0
                while (i < bytes.size) {
                    bytes[i] = bytes[i].inv()
                    i += 1
                }
                if (bytes.last().toUInt() and 0x80u == 0u) {
                    bytes.add(0xFFu)
                }
                bytes
            }
        }
    }

    /**
     * Returns the integer formatted as a string in the given radix.
     * `radix` must be in the range `2...36`.
     */
    fun toStrRadix(radix: UInt): String {
        val v = toStrRadixReversed(data, radix).toMutableList()

        if (isNegative()) {
            v.add('-'.code.toUByte())
        }

        v.reverse()
        return v.map { it.toInt().toChar() }.joinToString("")
    }

    /**
     * Returns the integer in the requested base in big-endian digit order.
     * The output is not given in a human readable alphabet but as a zero
     * based `UByte` number.
     * `radix` must be in the range `2...256`.
     */
    fun toRadixBe(radix: UInt): Pair<Sign, List<UByte>> {
        return Pair(signValue, data.toRadixBe(radix))
    }

    /**
     * Returns the integer in the requested base in little-endian digit order.
     * The output is not given in a human readable alphabet but as a zero
     * based `UByte` number.
     * `radix` must be in the range `2...256`.
     */
    fun toRadixLe(radix: UInt): Pair<Sign, List<UByte>> {
        return Pair(signValue, data.toRadixLe(radix))
    }

    /**
     * Returns the sign of the `BigInt` as a `Sign`.
     */
    fun sign(): Sign {
        return signValue
    }

    /**
     * Returns the magnitude of the `BigInt` as a `BigUint`.
     */
    fun magnitude(): BigUint {
        return data
    }

    /**
     * Convert this `BigInt` into its `Sign` and `BigUint` magnitude,
     * the reverse of `fromBiguint`.
     */
    fun intoParts(): Pair<Sign, BigUint> {
        return Pair(signValue, data)
    }

    /**
     * Determines the fewest bits necessary to express the `BigInt`,
     * not including the sign.
     */
    fun bits(): ULong {
        return data.bits()
    }

    /**
     * Converts this `BigInt` into a `BigUint`, if it's not negative.
     */
    fun toBigUint(): BigUint? {
        return when (signValue) {
            Sign.Plus -> data.clone()
            Sign.NoSign -> BigUint.ZERO
            Sign.Minus -> null
        }
    }

    fun checkedAdd(v: BigInt): BigInt? {
        return this + v
    }

    fun checkedSub(v: BigInt): BigInt? {
        return this - v
    }

    fun checkedMul(v: BigInt): BigInt? {
        return this * v
    }

    fun checkedDiv(v: BigInt): BigInt? {
        if (v.isZero()) {
            return null
        }
        return this / v
    }

    /**
     * Returns `self ^ exponent`.
     */
    fun pow(exponent: UInt): BigInt {
        return powBigInt(this, exponent)
    }

    /**
     * Returns `(self ^ exponent) mod modulus`.
     *
     * Note that this rounds like [modFloor], not like the `%` operator,
     * which makes a difference when given a negative `self` or `modulus`.
     * The result will be in the interval `[0, modulus)` for `modulus > 0`,
     * or in the interval `(modulus, 0]` for `modulus < 0`.
     *
     * Panics if the exponent is negative or the modulus is zero.
     */
    fun modpow(exponent: BigInt, modulus: BigInt): BigInt {
        return modpowBigInt(this, exponent, modulus)
    }

    /**
     * Returns the modular multiplicative inverse if it exists, otherwise `null`.
     *
     * This solves for `x` such that `self * x == 1 (mod modulus)`.
     * Note that this rounds like [modFloor], not like the `%` operator,
     * which makes a difference when given a negative `self` or `modulus`.
     * The solution will be in the interval `[0, modulus)` for `modulus > 0`,
     * or in the interval `(modulus, 0]` for `modulus < 0`,
     * and it exists if and only if `gcd(self, modulus) == 1`.
     */
    fun modinv(modulus: BigInt): BigInt? {
        val result = data.modinv(modulus.data) ?: return null
        // The sign of the result follows the modulus, like `modFloor`.
        val (sign, mag) = when (Pair(isNegative(), modulus.isNegative())) {
            Pair(false, false) -> Pair(Sign.Plus, result)
            Pair(true, false) -> Pair(Sign.Plus, modulus.data - result)
            Pair(false, true) -> Pair(Sign.Minus, modulus.data - result)
            else -> Pair(Sign.Minus, result)
        }
        return fromBiguint(sign, mag)
    }

    /**
     * Returns the truncated principal square root of `self`.
     */
    fun sqrt(): BigInt {
        require(!isNegative()) { "square root is imaginary" }
        return fromBiguint(signValue, data.sqrt())
    }

    /**
     * Returns the truncated principal cube root of `self`.
     */
    fun cbrt(): BigInt {
        return fromBiguint(signValue, data.cbrt())
    }

    /**
     * Returns the truncated principal `n`th root of `self`.
     */
    fun nthRoot(n: UInt): BigInt {
        require(!(isNegative() && n % 2u == 0u)) { "root of degree $n is imaginary" }
        return fromBiguint(signValue, data.nthRoot(n))
    }

    /**
     * Returns the number of least-significant bits that are zero,
     * or `null` if the entire number is zero.
     */
    fun trailingZeros(): ULong? {
        return data.trailingZeros()
    }

    /**
     * Returns whether the bit in position `bit` is set,
     * using the two's complement for negative numbers.
     */
    fun bit(bit: ULong): Boolean {
        return if (isNegative()) {
            val magnitudeMinusOne = data - 1u
            !magnitudeMinusOne.bit(bit)
        } else {
            data.bit(bit)
        }
    }

    /**
     * Sets or clears the bit in the given position,
     * using the two's complement for negative numbers.
     *
     * Note that setting or clearing a bit for positive or negative numbers,
     * respectively, greater than the current bit length may need a reallocation
     * to store the new digits.
     */
    fun setBit(bit: ULong, value: Boolean) {
        when (signValue) {
            Sign.Plus -> data.setBit(bit, value)
            Sign.Minus -> {
                val magnitudeMinusOne = data - 1u
                magnitudeMinusOne.setBit(bit, !value)
                data = magnitudeMinusOne + 1u
            }
            Sign.NoSign -> {
                if (value) {
                    data.setBit(bit, true)
                    signValue = Sign.Plus
                } else {
                    // Clearing a bit for zero is a no-op.
                }
            }
        }
        // The top bit may have been cleared, so normalize.
        normalize()
    }

    companion object {
        /**
         * A constant `BigInt` with value 0, useful for static initialization.
         */
        val ZERO: BigInt
            get() = BigInt(Sign.NoSign, BigUint.ZERO)

        /**
         * Creates and initializes a `BigInt`.
         *
         * The base 2^32 digits are ordered least significant digit first.
         */
        fun new(sign: Sign, digits: List<UInt>): BigInt {
            return fromBiguint(sign, BigUint.new(digits))
        }

        /**
         * Creates and initializes a `BigInt`.
         *
         * The base 2^32 digits are ordered least significant digit first.
         */
        fun fromBiguint(sign0: Sign, data0: BigUint): BigInt {
            var sign = sign0
            val data = data0
            if (sign == Sign.NoSign) {
                data.assignFromSlice(emptyList())
            } else if (data.isZero()) {
                sign = Sign.NoSign
            }

            return BigInt(sign, data)
        }

        /**
         * Creates and initializes a `BigInt`.
         *
         * The base 2^32 digits are ordered least significant digit first.
         */
        fun fromSlice(sign: Sign, slice: List<UInt>): BigInt {
            return fromBiguint(sign, BigUint.fromSlice(slice))
        }

        /**
         * Creates and initializes a `BigInt`.
         *
         * The bytes are in big-endian byte order.
         */
        fun fromBytesBe(sign: Sign, bytes: List<UByte>): BigInt {
            return fromBiguint(sign, BigUint.fromBytesBe(bytes))
        }

        /**
         * Creates and initializes a `BigInt`.
         *
         * The bytes are in little-endian byte order.
         */
        fun fromBytesLe(sign: Sign, bytes: List<UByte>): BigInt {
            return fromBiguint(sign, BigUint.fromBytesLe(bytes))
        }

        /**
         * Creates and initializes a `BigInt` from an array of bytes in
         * two's complement binary representation.
         *
         * The digits are in big-endian base 2^8.
         */
        fun fromSignedBytesBe(digits: List<UByte>): BigInt {
            val v = digits.toMutableList()
            v.reverse()
            return fromSignedBytesLe(v)
        }

        /**
         * Creates and initializes a `BigInt` from an array of bytes in two's complement.
         *
         * The digits are in little-endian base 2^8.
         */
        fun fromSignedBytesLe(digits: List<UByte>): BigInt {
            if (digits.isEmpty()) {
                return ZERO
            }
            val negative = digits.last().toUInt() and 0x80u != 0u
            if (!negative) {
                return fromBiguint(Sign.Plus, BigUint.fromBytesLe(digits))
            }
            val inverted = digits.map { it.inv() }.toMutableList()
            val magnitude = BigUint.fromBytesLe(inverted) + 1u
            return fromBiguint(Sign.Minus, magnitude)
        }

        /**
         * Creates and initializes a `BigInt`.
         */
        fun parseBytes(buf: ByteArray, radix: UInt): BigInt? {
            val s = runCatching { buf.decodeToString(throwOnInvalidSequence = true) }.getOrNull()
                ?: return null
            return fromStrRadix(s, radix).getOrNull()
        }

        /**
         * Creates and initializes a `BigInt`. Each `UByte` of the input slice is
         * interpreted as one digit of the number and must therefore be less than `radix`.
         *
         * The bytes are in big-endian byte order.
         * `radix` must be in the range `2...256`.
         */
        fun fromRadixBe(sign: Sign, buf: List<UByte>, radix: UInt): BigInt? {
            val u = BigUint.fromRadixBe(buf, radix) ?: return null
            return fromBiguint(sign, u)
        }

        /**
         * Creates and initializes a `BigInt`. Each `UByte` of the input slice is
         * interpreted as one digit of the number and must therefore be less than `radix`.
         *
         * The bytes are in little-endian byte order.
         * `radix` must be in the range `2...256`.
         */
        fun fromRadixLe(sign: Sign, buf: List<UByte>, radix: UInt): BigInt? {
            val u = BigUint.fromRadixLe(buf, radix) ?: return null
            return fromBiguint(sign, u)
        }

        fun zero(): BigInt = ZERO

        fun one(): BigInt {
            return BigInt(Sign.Plus, BigUint.one())
        }

        fun fromStrRadix(s0: String, radix: UInt): Result<BigInt> {
            var s = s0
            var sign = Sign.Plus
            if (s.startsWith("-")) {
                sign = Sign.Minus
                s = s.drop(1)
            }
            return BigUint.fromStrRadix(s, radix).map { fromBiguint(sign, it) }
        }

        fun from(value: BigUint): BigInt {
            return fromBiguint(Sign.Plus, value)
        }

        fun from(value: UInt): BigInt {
            return from(value.toBigUint())
        }

        fun from(value: ULong): BigInt {
            return from(value.toBigUint())
        }

        fun from(value: Int): BigInt {
            return if (value >= 0) {
                from(value.toUInt())
            } else {
                fromBiguint(Sign.Minus, value.toUInt().inv().toBigUint() + 1u)
            }
        }

        fun from(value: Long): BigInt {
            return if (value >= 0) {
                from(value.toULong())
            } else {
                fromBiguint(Sign.Minus, value.toULong().inv().toBigUint() + 1u)
            }
        }
    }
}

fun zeroBigInt(): BigInt = BigInt.zero()

fun oneBigInt(): BigInt = BigInt.one()

operator fun BigInt.div(other: BigInt): BigInt {
    val (q, _) = divRem(other)
    return q
}

operator fun BigInt.divAssign(other: BigInt) {
    val next = this / other
    cloneFrom(next)
}

operator fun BigInt.div(other: UInt): BigInt {
    return this / BigInt.from(other)
}

operator fun BigInt.rem(other: BigInt): BigInt {
    val (_, r) = divRem(other)
    return r
}

operator fun BigInt.remAssign(other: BigInt) {
    val next = this % other
    cloneFrom(next)
}

operator fun BigInt.rem(other: UInt): BigInt {
    return this % BigInt.from(other)
}

fun BigInt.toBigInt(): BigInt? {
    return clone()
}

fun BigUint.toBigInt(): BigInt {
    return BigInt.from(this)
}

fun Int.toBigInt(): BigInt {
    return BigInt.from(this)
}

fun UInt.toBigInt(): BigInt {
    return BigInt.from(this)
}

fun Long.toBigInt(): BigInt {
    return BigInt.from(this)
}

fun ULong.toBigInt(): BigInt {
    return BigInt.from(this)
}

internal fun powBigInt(self: BigInt, exp: UInt): BigInt {
    if (exp == 0u) {
        return BigInt.one()
    }
    val unsigned = self.data.pow(exp)
    val sign = if (self.isNegative() && exp % 2u == 1u) Sign.Minus else Sign.Plus
    return BigInt.fromBiguint(sign, unsigned)
}

internal fun modpowBigInt(base: BigInt, exponent: BigInt, modulus: BigInt): BigInt {
    require(!exponent.isNegative()) { "negative exponent" }
    require(!modulus.isZero()) { "attempt to calculate with zero modulus!" }
    val unsigned = base.modFloor(modulus).data.modpow(exponent.data, modulus.data)
    return BigInt.fromBiguint(modulus.sign(), unsigned).modFloor(modulus)
}
