// port-lint: source bigint/shift.rs
package io.github.kotlinmania.numbigint.bigint

import io.github.kotlinmania.numbigint.BigInt
import io.github.kotlinmania.numbigint.Sign
import io.github.kotlinmania.numbigint.plus
import io.github.kotlinmania.numbigint.plusAssign
import io.github.kotlinmania.numbigint.shiftLeft
import io.github.kotlinmania.numbigint.shiftLeftAssign
import io.github.kotlinmania.numbigint.shiftRight
import io.github.kotlinmania.numbigint.shiftRightAssign

fun shl(self: BigInt, rhs: UByte): BigInt = shl(self, rhs.toULong())

fun shlAssign(self: BigInt, rhs: UByte) {
    shlAssign(self, rhs.toULong())
}

fun shl(self: BigInt, rhs: UShort): BigInt = shl(self, rhs.toULong())

fun shlAssign(self: BigInt, rhs: UShort) {
    shlAssign(self, rhs.toULong())
}

fun shl(self: BigInt, rhs: UInt): BigInt = shl(self, rhs.toULong())

fun shlAssign(self: BigInt, rhs: UInt) {
    shlAssign(self, rhs.toULong())
}

fun shl(self: BigInt, rhs: ULong): BigInt = BigInt.fromBiguint(self.sign(), self.data.shiftLeft(rhs))

fun shlAssign(self: BigInt, rhs: ULong) {
    self.data.shiftLeftAssign(rhs)
}

fun shl(self: BigInt, rhs: Byte): BigInt {
    require(rhs >= 0) { "negative shift count" }
    return shl(self, rhs.toULong())
}

fun shlAssign(self: BigInt, rhs: Byte) {
    require(rhs >= 0) { "negative shift count" }
    shlAssign(self, rhs.toULong())
}

fun shl(self: BigInt, rhs: Short): BigInt {
    require(rhs >= 0) { "negative shift count" }
    return shl(self, rhs.toULong())
}

fun shlAssign(self: BigInt, rhs: Short) {
    require(rhs >= 0) { "negative shift count" }
    shlAssign(self, rhs.toULong())
}

fun shl(self: BigInt, rhs: Int): BigInt {
    require(rhs >= 0) { "negative shift count" }
    return shl(self, rhs.toUInt())
}

fun shlAssign(self: BigInt, rhs: Int) {
    require(rhs >= 0) { "negative shift count" }
    shlAssign(self, rhs.toUInt())
}

fun shl(self: BigInt, rhs: Long): BigInt {
    require(rhs >= 0) { "negative shift count" }
    return shl(self, rhs.toULong())
}

fun shlAssign(self: BigInt, rhs: Long) {
    require(rhs >= 0) { "negative shift count" }
    shlAssign(self, rhs.toULong())
}

fun shr(self: BigInt, rhs: UByte): BigInt = shr(self, rhs.toULong())

fun shrAssign(self: BigInt, rhs: UByte) {
    shrAssign(self, rhs.toULong())
}

fun shr(self: BigInt, rhs: UShort): BigInt = shr(self, rhs.toULong())

fun shrAssign(self: BigInt, rhs: UShort) {
    shrAssign(self, rhs.toULong())
}

fun shr(self: BigInt, rhs: UInt): BigInt = shr(self, rhs.toULong())

fun shrAssign(self: BigInt, rhs: UInt) {
    shrAssign(self, rhs.toULong())
}

fun shr(self: BigInt, rhs: ULong): BigInt {
    val roundDown = shrRoundDown(self, rhs)
    val shifted = self.data.shiftRight(rhs)
    val data = if (roundDown) shifted + 1u.toUByte() else shifted
    return BigInt.fromBiguint(self.sign(), data)
}

fun shrAssign(self: BigInt, rhs: ULong) {
    val roundDown = shrRoundDown(self, rhs)
    self.data.shiftRightAssign(rhs)
    if (roundDown) {
        self.data += 1u.toUByte()
    } else if (self.data.isZero()) {
        self.signValue = Sign.NoSign
    }
}

fun shr(self: BigInt, rhs: Byte): BigInt {
    require(rhs >= 0) { "negative shift count" }
    return shr(self, rhs.toULong())
}

fun shrAssign(self: BigInt, rhs: Byte) {
    require(rhs >= 0) { "negative shift count" }
    shrAssign(self, rhs.toULong())
}

fun shr(self: BigInt, rhs: Short): BigInt {
    require(rhs >= 0) { "negative shift count" }
    return shr(self, rhs.toULong())
}

fun shrAssign(self: BigInt, rhs: Short) {
    require(rhs >= 0) { "negative shift count" }
    shrAssign(self, rhs.toULong())
}

fun shr(self: BigInt, rhs: Int): BigInt {
    require(rhs >= 0) { "negative shift count" }
    return shr(self, rhs.toUInt())
}

fun shrAssign(self: BigInt, rhs: Int) {
    require(rhs >= 0) { "negative shift count" }
    shrAssign(self, rhs.toUInt())
}

fun shr(self: BigInt, rhs: Long): BigInt {
    require(rhs >= 0) { "negative shift count" }
    return shr(self, rhs.toULong())
}

fun shrAssign(self: BigInt, rhs: Long) {
    require(rhs >= 0) { "negative shift count" }
    shrAssign(self, rhs.toULong())
}

// Negative values need a rounding adjustment if there are any ones in the
// bits that are getting shifted out.
fun shrRoundDown(i: BigInt, shift: ULong): Boolean =
    if (i.isNegative()) {
        val zeros = checkNotNull(i.trailingZeros()) { "negative values are non-zero" }
        shift > 0uL && zeros < shift
    } else {
        false
    }
