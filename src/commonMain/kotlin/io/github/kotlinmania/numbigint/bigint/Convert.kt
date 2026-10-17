// port-lint: source bigint/convert.rs

package io.github.kotlinmania.numbigint.bigint

import io.github.kotlinmania.numbigint.BigInt
import io.github.kotlinmania.numbigint.BigIntParseResult
import io.github.kotlinmania.numbigint.BigIntToBigUintResult
import io.github.kotlinmania.numbigint.BigIntToLongResult
import io.github.kotlinmania.numbigint.BigIntToULongResult
import io.github.kotlinmania.numbigint.BigUint
import io.github.kotlinmania.numbigint.ParseBigIntError
import io.github.kotlinmania.numbigint.Sign
import io.github.kotlinmania.numbigint.TryFromBigIntError
import io.github.kotlinmania.numbigint.toDoubleOrNull
import io.github.kotlinmania.numbigint.toFloatOrNull
import io.github.kotlinmania.numbigint.toLongOrNull
import io.github.kotlinmania.numbigint.toULongOrNull

internal interface Err

fun fromStr(s: String): BigIntParseResult = fromStrRadix(s, 10u)

internal interface FromStrRadixErr

/**
 * Creates and initializes a `BigInt`.
 */
fun fromStrRadix(s0: String, radix: UInt): BigIntParseResult {
    var s = s0
    val sign =
        if (s.startsWith("-")) {
            val tail = s.drop(1)
            if (!tail.startsWith("+")) {
                s = tail
            }
            Sign.Minus
        } else {
            Sign.Plus
        }
    val parsed = BigUint.fromStrRadix(s, radix)
    return when {
        parsed.value != null -> BigIntParseResult(BigInt.fromBiguint(sign, parsed.value), null)
        parsed.error != null -> BigIntParseResult(null, parsed.error)
        else -> error("BigUintParseResult invariant violated")
    }
}

fun toI64(value: BigInt): Long? {
    return when (value.sign()) {
        Sign.Plus -> {
            value.data.toLongOrNull()
        }

        Sign.NoSign -> {
            0L
        }

        Sign.Minus -> {
            val n = value.data.toULongOrNull() ?: return null
            val minMagnitude = 1uL shl 63
            when {
                n < minMagnitude -> -n.toLong()
                n == minMagnitude -> Long.MIN_VALUE
                else -> null
            }
        }
    }
}

fun toI128(value: BigInt): Long? = toI64(value)

fun toU64(value: BigInt): ULong? =
    when (value.sign()) {
        Sign.Plus -> value.data.toULongOrNull()
        Sign.NoSign -> 0uL
        Sign.Minus -> null
    }

fun toU128(value: BigInt): ULong? = toU64(value)

fun toF32(value: BigInt): Float? {
    val n = value.data.toFloatOrNull() ?: return null
    return if (value.sign() == Sign.Minus) -n else n
}

fun toF64(value: BigInt): Double? {
    val n = value.data.toDoubleOrNull() ?: return null
    return if (value.sign() == Sign.Minus) -n else n
}

fun tryFromBigintToULong(value: BigInt): BigIntToULongResult =
    toU64(value)?.let { BigIntToULongResult(it, null) }
        ?: BigIntToULongResult(null, TryFromBigIntError(null))

fun tryFromBigintToLong(value: BigInt): BigIntToLongResult =
    toI64(value)?.let { BigIntToLongResult(it, null) }
        ?: BigIntToLongResult(null, TryFromBigIntError(null))

fun fromI64(n: Long): BigInt? = from(n)

fun fromI128(n: Long): BigInt? = from(n)

fun fromU64(n: ULong): BigInt? = from(n)

fun fromU128(n: ULong): BigInt? = from(n)

fun fromF32(n: Float): BigInt? = fromF64(Float.fromBits(n.toRawBits()).toDouble())

fun fromF64(n: Double): BigInt? =
    if (n >= 0.0) {
        BigUint.fromDouble(n)?.let { from(it) }
    } else {
        BigUint.fromDouble(-n)?.let { -from(it) }
    }

fun from(n: Long): BigInt =
    if (n >= 0L) {
        from(n.toULong())
    } else {
        val u = ULong.MAX_VALUE - n.toULong() + 1uL
        BigInt.fromBiguint(Sign.Minus, BigUint.fromULong(u))
    }

fun from(n: Int): BigInt = from(n.toLong())

fun from(n: ULong): BigInt =
    if (n > 0uL) {
        BigInt.fromBiguint(Sign.Plus, BigUint.fromULong(n))
    } else {
        BigInt.ZERO
    }

fun from(n: UInt): BigInt = from(n.toULong())

fun from(n: BigUint): BigInt =
    if (n.isZero()) {
        BigInt.ZERO
    } else {
        BigInt.fromBiguint(Sign.Plus, n)
    }

fun from(x: Boolean): BigInt = if (x) BigInt.one() else BigInt.ZERO

fun fromBool(x: Boolean): BigInt = from(x)

fun toBigint(value: BigInt): BigInt? = value.clone()

fun toBigint(value: BigUint): BigInt? = if (value.isZero()) BigInt.ZERO else from(value.clone())

fun toBiguint(value: BigInt): BigUint? =
    when (value.sign()) {
        Sign.Plus -> value.data.clone()
        Sign.NoSign -> BigUint.ZERO
        Sign.Minus -> null
    }

internal interface Error

fun tryFrom(value: BigInt): BigIntToBigUintResult =
    toBiguint(value)?.let { BigIntToBigUintResult(it, null) }
        ?: BigIntToBigUintResult(null, TryFromBigIntError(null))

fun tryFrom(value: BigInt, consume: Boolean): BigIntToBigUintResult =
    if (value.sign() == Sign.Minus) {
        BigIntToBigUintResult(null, TryFromBigIntError(if (consume) value else null))
    } else {
        BigIntToBigUintResult(value.data, null)
    }

fun tryFromBigintToBiguint(value: BigInt): BigIntToBigUintResult = tryFrom(value, true)

fun fromSignedBytesBe(digits: List<UByte>): BigInt {
    val first = digits.firstOrNull() ?: return BigInt.ZERO
    val sign = if (first > 0x7fu) Sign.Minus else Sign.Plus

    return if (sign == Sign.Minus) {
        // two's-complement the content to retrieve the magnitude
        val mutDigits = digits.toMutableList()
        twosComplementBe(mutDigits)
        BigInt.fromBiguint(sign, BigUint.fromBytesBe(mutDigits))
    } else {
        BigInt.fromBiguint(sign, BigUint.fromBytesBe(digits))
    }
}

fun fromSignedBytesLe(digits: List<UByte>): BigInt {
    val last = digits.lastOrNull() ?: return BigInt.ZERO
    val sign = if (last > 0x7fu) Sign.Minus else Sign.Plus

    return if (sign == Sign.Minus) {
        // two's-complement the content to retrieve the magnitude
        val mutDigits = digits.toMutableList()
        twosComplementLe(mutDigits)
        BigInt.fromBiguint(sign, BigUint.fromBytesLe(mutDigits))
    } else {
        BigInt.fromBiguint(sign, BigUint.fromBytesLe(digits))
    }
}

fun toSignedBytesBe(x: BigInt): List<UByte> {
    val bytes = x.data.toBytesBe().toMutableList()
    val firstByte = bytes.firstOrNull() ?: 0u
    if (
        firstByte > 0x7fu &&
        !(firstByte == 0x80.toUByte() && bytes.drop(1).all { it == 0.toUByte() } && x.sign() == Sign.Minus)
    ) {
        // msb used by magnitude, extend by 1 byte
        bytes.add(0, 0u)
    }
    if (x.sign() == Sign.Minus) {
        twosComplementBe(bytes)
    }
    return bytes
}

fun toSignedBytesLe(x: BigInt): List<UByte> {
    val bytes = x.data.toBytesLe().toMutableList()
    val lastByte = bytes.lastOrNull() ?: 0u
    if (
        lastByte > 0x7fu &&
        !(lastByte == 0x80.toUByte() && bytes.asReversed().drop(1).all { it == 0.toUByte() } && x.sign() == Sign.Minus)
    ) {
        // msb used by magnitude, extend by 1 byte
        bytes.add(0u)
    }
    if (x.sign() == Sign.Minus) {
        twosComplementLe(bytes)
    }
    return bytes
}

/**
 * Perform in-place two's complement of the given binary representation,
 * in little-endian byte order.
 */
fun twosComplementLe(digits: MutableList<UByte>) {
    twosComplement(digits)
}

/**
 * Perform in-place two's complement of the given binary representation
 * in big-endian byte order.
 */
fun twosComplementBe(digits: MutableList<UByte>) {
    var carry = true
    var i = digits.lastIndex
    while (i >= 0) {
        digits[i] = digits[i].toUInt().inv().toUByte()
        if (carry) {
            val next = digits[i].toUInt() + 1u
            digits[i] = next.toUByte()
            carry = next > UByte.MAX_VALUE.toUInt()
        }
        i -= 1
    }
}

/**
 * Perform in-place two's complement of the given digit iterator
 * starting from the least significant byte.
 */
fun twosComplement(digits: MutableList<UByte>) {
    var carry = true
    for (i in digits.indices) {
        digits[i] = digits[i].toUInt().inv().toUByte()
        if (carry) {
            val next = digits[i].toUInt() + 1u
            digits[i] = next.toUByte()
            carry = next > UByte.MAX_VALUE.toUInt()
        }
    }
}

fun parseBigint(s: String, radix: UInt): BigIntParseResult {
    if (s.isEmpty()) {
        return BigIntParseResult(null, ParseBigIntError.empty())
    }
    return fromStrRadix(s, radix)
}
