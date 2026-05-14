// port-lint: source bigint/multiplication.rs
package io.github.kotlinmania.numbigint.bigint

import io.github.kotlinmania.numbigint.BigInt
import io.github.kotlinmania.numbigint.Sign
import io.github.kotlinmania.numbigint.times

fun mul(self: Sign, other: Sign): Sign {
    return when {
        self == Sign.NoSign || other == Sign.NoSign -> Sign.NoSign
        self == other -> Sign.Plus
        else -> Sign.Minus
    }
}

fun mul(self: BigInt, other: BigInt): BigInt {
    return BigInt.fromBiguint(mul(self.sign(), other.sign()), self.data * other.data)
}

fun mulAssign(self: BigInt, other: BigInt) {
    self.cloneFrom(mul(self, other))
}

fun mul(self: BigInt, other: UInt): BigInt {
    return BigInt.fromBiguint(self.sign(), self.data * other)
}

fun mulAssign(self: BigInt, other: UInt) {
    self.cloneFrom(mul(self, other))
}

fun mul(self: UInt, other: BigInt): BigInt {
    return mul(other, self)
}

fun mul(self: BigInt, other: ULong): BigInt {
    return BigInt.fromBiguint(self.sign(), self.data * other)
}

fun mulAssign(self: BigInt, other: ULong) {
    self.cloneFrom(mul(self, other))
}

fun mul(self: ULong, other: BigInt): BigInt {
    return mul(other, self)
}

fun mul(self: BigInt, other: Int): BigInt {
    return mul(self, BigInt.from(other))
}

fun mulAssign(self: BigInt, other: Int) {
    self.cloneFrom(mul(self, other))
}

fun mul(self: Int, other: BigInt): BigInt {
    return mul(BigInt.from(self), other)
}

fun mul(self: BigInt, other: Long): BigInt {
    return mul(self, BigInt.from(other))
}

fun mulAssign(self: BigInt, other: Long) {
    self.cloneFrom(mul(self, other))
}

fun mul(self: Long, other: BigInt): BigInt {
    return mul(BigInt.from(self), other)
}

operator fun BigInt.times(other: BigInt): BigInt {
    return mul(this, other)
}

operator fun BigInt.timesAssign(other: BigInt) {
    mulAssign(this, other)
}

operator fun BigInt.times(other: UInt): BigInt {
    return mul(this, other)
}

operator fun BigInt.timesAssign(other: UInt) {
    mulAssign(this, other)
}

operator fun UInt.times(other: BigInt): BigInt {
    return mul(this, other)
}

operator fun BigInt.times(other: ULong): BigInt {
    return mul(this, other)
}

operator fun BigInt.timesAssign(other: ULong) {
    mulAssign(this, other)
}

operator fun ULong.times(other: BigInt): BigInt {
    return mul(this, other)
}

operator fun BigInt.times(other: Int): BigInt {
    return mul(this, other)
}

operator fun BigInt.timesAssign(other: Int) {
    mulAssign(this, other)
}

operator fun Int.times(other: BigInt): BigInt {
    return mul(this, other)
}

operator fun BigInt.times(other: Long): BigInt {
    return mul(this, other)
}

operator fun BigInt.timesAssign(other: Long) {
    mulAssign(this, other)
}

operator fun Long.times(other: BigInt): BigInt {
    return mul(this, other)
}

fun checkedMul(self: BigInt, v: BigInt): BigInt? {
    return mul(self, v)
}

fun Iterable<BigInt>.productBigInt(): BigInt {
    var product = BigInt.one()
    for (value in this) {
        product.timesAssign(value)
    }
    return product
}

fun Sequence<BigInt>.productBigInt(): BigInt {
    var product = BigInt.one()
    for (value in this) {
        product.timesAssign(value)
    }
    return product
}
