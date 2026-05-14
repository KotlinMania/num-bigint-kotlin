// port-lint: source bigint/convert.rs
package io.github.kotlinmania.numbigint.bigint

import io.github.kotlinmania.numbigint.BigInt
import io.github.kotlinmania.numbigint.BigUint
import io.github.kotlinmania.numbigint.ParseBigIntError
import io.github.kotlinmania.numbigint.Sign
import io.github.kotlinmania.numbigint.TryFromBigIntError
import io.github.kotlinmania.numbigint.TryFromBigIntException
import io.github.kotlinmania.numbigint.toDoubleOrNull
import io.github.kotlinmania.numbigint.toLongOrNull
import io.github.kotlinmania.numbigint.toULongOrNull

fun fromStr(s: String): Result<BigInt> {
    return fromStrRadix(s, 10u)
}

/**
 * Creates and initializes a `BigInt`.
 */
fun fromStrRadix(s0: String, radix: UInt): Result<BigInt> {
    var s = s0
    val sign = if (s.startsWith("-")) {
        val tail = s.drop(1)
        if (!tail.startsWith("+")) {
            s = tail
        }
        Sign.Minus
    } else {
        Sign.Plus
    }
    return BigUint.fromStrRadix(s, radix).map { BigInt.fromBiguint(sign, it) }
}

fun toI64(value: BigInt): Long? {
    return when (value.sign()) {
        Sign.Plus -> value.data.toLongOrNull()
        Sign.NoSign -> 0L
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

fun toU64(value: BigInt): ULong? {
    return when (value.sign()) {
        Sign.Plus -> value.data.toULongOrNull()
        Sign.NoSign -> 0uL
        Sign.Minus -> null
    }
}

fun toF32(value: BigInt): Float? {
    return toF64(value)?.toFloat()
}

fun toF64(value: BigInt): Double? {
    val n = value.data.toDoubleOrNull() ?: return null
    return if (value.sign() == Sign.Minus) -n else n
}

fun tryFromBigintToULong(value: BigInt): Result<ULong> {
    return toU64(value)?.let { Result.success(it) }
        ?: Result.failure(TryFromBigIntException(TryFromBigIntError(Unit)))
}

fun tryFromBigintToLong(value: BigInt): Result<Long> {
    return toI64(value)?.let { Result.success(it) }
        ?: Result.failure(TryFromBigIntException(TryFromBigIntError(Unit)))
}

fun fromI64(n: Long): BigInt? {
    return BigInt.from(n)
}

fun fromU64(n: ULong): BigInt? {
    return BigInt.from(n)
}

fun fromF64(n: Double): BigInt? {
    return if (n >= 0.0) {
        BigUint.fromDouble(n)?.let { BigInt.from(it) }
    } else {
        BigUint.fromDouble(-n)?.let { -BigInt.from(it) }
    }
}

fun fromBool(x: Boolean): BigInt {
    return if (x) BigInt.one() else BigInt.ZERO
}

fun toBigint(value: BigInt): BigInt? {
    return value.clone()
}

fun toBigint(value: BigUint): BigInt? {
    return if (value.isZero()) BigInt.ZERO else BigInt.from(value.clone())
}

fun toBiguint(value: BigInt): BigUint? {
    return value.toBigUint()
}

fun tryFromBigintToBiguint(value: BigInt): Result<BigUint> {
    return value.toBigUint()?.let { Result.success(it) }
        ?: Result.failure(TryFromBigIntException(TryFromBigIntError(value)))
}

fun fromSignedBytesBe(digits: List<UByte>): BigInt {
    return BigInt.fromSignedBytesBe(digits)
}

fun fromSignedBytesLe(digits: List<UByte>): BigInt {
    return BigInt.fromSignedBytesLe(digits)
}

fun toSignedBytesBe(x: BigInt): List<UByte> {
    return x.toSignedBytesBe()
}

fun toSignedBytesLe(x: BigInt): List<UByte> {
    return x.toSignedBytesLe()
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

fun parseBigint(s: String, radix: UInt): Result<BigInt> {
    if (s.isEmpty()) {
        return Result.failure(ParseBigIntError.empty())
    }
    return fromStrRadix(s, radix)
}
