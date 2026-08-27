// port-lint: source bigint/shift.rs
package io.github.kotlinmania.numbigint.bigint

import io.github.kotlinmania.numbigint.BigInt
import io.github.kotlinmania.numbigint.plus
import io.github.kotlinmania.numbigint.shiftLeft
import io.github.kotlinmania.numbigint.shiftRight

fun shl(self: BigInt, rhs: ULong): BigInt = BigInt.fromBiguint(self.sign(), self.data.shiftLeft(rhs))

fun shlAssign(self: BigInt, rhs: ULong) {
    self.cloneFrom(shl(self, rhs))
}

fun shl(self: BigInt, rhs: UInt): BigInt = shl(self, rhs.toULong())

fun shlAssign(self: BigInt, rhs: UInt) {
    shlAssign(self, rhs.toULong())
}

fun shl(self: BigInt, rhs: Long): BigInt {
    require(rhs >= 0) { "negative shift count" }
    return shl(self, rhs.toULong())
}

fun shlAssign(self: BigInt, rhs: Long) {
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

fun shr(self: BigInt, rhs: ULong): BigInt {
    val roundDown = shrRoundDown(self, rhs)
    val shifted = self.data.shiftRight(rhs)
    val data = if (roundDown) shifted + 1u else shifted
    return BigInt.fromBiguint(self.sign(), data)
}

fun shrAssign(self: BigInt, rhs: ULong) {
    self.cloneFrom(shr(self, rhs))
}

fun shr(self: BigInt, rhs: UInt): BigInt = shr(self, rhs.toULong())

fun shrAssign(self: BigInt, rhs: UInt) {
    shrAssign(self, rhs.toULong())
}

fun shr(self: BigInt, rhs: Long): BigInt {
    require(rhs >= 0) { "negative shift count" }
    return shr(self, rhs.toULong())
}

fun shrAssign(self: BigInt, rhs: Long) {
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

// Negative values need a rounding adjustment if there are any ones in the
// bits that are getting shifted out.
fun shrRoundDown(i: BigInt, shift: ULong): Boolean =
    if (i.isNegative()) {
        val zeros = checkNotNull(i.trailingZeros()) { "negative values are non-zero" }
        shift > 0uL && zeros < shift
    } else {
        false
    }
