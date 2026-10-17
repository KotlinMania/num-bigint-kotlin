// port-lint: source bigint/multiplication.rs
package io.github.kotlinmania.numbigint.bigint

import io.github.kotlinmania.numbigint.BigInt
import io.github.kotlinmania.numbigint.Sign
import io.github.kotlinmania.numbigint.checkedUabs
import io.github.kotlinmania.numbigint.times
import io.github.kotlinmania.numbigint.timesAssign
import io.github.kotlinmania.numbigint.unaryMinus

fun mul(self: Sign, other: Sign): Sign =
    when {
        self == Sign.NoSign || other == Sign.NoSign -> Sign.NoSign
        self == other -> Sign.Plus
        else -> Sign.Minus
    }

fun mul(self: BigInt, other: BigInt): BigInt = BigInt.fromBiguint(mul(self.sign(), other.sign()), self.data * other.data)

fun mulAssign(self: BigInt, other: BigInt) {
    self.data *= other.data
    if (self.data.isZero()) {
        self.signValue = Sign.NoSign
    } else {
        self.signValue = mul(self.sign(), other.sign())
    }
}

fun mul(self: BigInt, other: UInt): BigInt = BigInt.fromBiguint(self.sign(), self.data * other)

fun mulAssign(self: BigInt, other: UInt) {
    self.data *= other
    if (self.data.isZero()) {
        self.signValue = Sign.NoSign
    }
}

fun mul(self: UInt, other: BigInt): BigInt = mul(other, self)

fun mul(self: BigInt, other: ULong): BigInt = BigInt.fromBiguint(self.sign(), self.data * other)

fun mulAssign(self: BigInt, other: ULong) {
    self.data *= other
    if (self.data.isZero()) {
        self.signValue = Sign.NoSign
    }
}

fun mul(self: ULong, other: BigInt): BigInt = mul(other, self)

fun mul(self: BigInt, other: Int): BigInt {
    val uabs = other.checkedUabs()
    return if (uabs.isPositive) {
        mul(self, uabs.value)
    } else {
        mul(-self, uabs.value)
    }
}

fun mulAssign(self: BigInt, other: Int) {
    val uabs = other.checkedUabs()
    if (uabs.isPositive) {
        mulAssign(self, uabs.value)
    } else {
        self.signValue = -self.signValue
        self.data *= uabs.value
    }
}

fun mul(self: Int, other: BigInt): BigInt = mul(other, self)

fun mul(self: BigInt, other: Long): BigInt {
    val uabs = other.checkedUabs()
    return if (uabs.isPositive) {
        mul(self, uabs.value)
    } else {
        mul(-self, uabs.value)
    }
}

fun mulAssign(self: BigInt, other: Long) {
    val uabs = other.checkedUabs()
    if (uabs.isPositive) {
        mulAssign(self, uabs.value)
    } else {
        self.signValue = -self.signValue
        self.data *= uabs.value
    }
}

fun mul(self: Long, other: BigInt): BigInt = mul(other, self)

fun mulAssign(self: BigInt, other: UByte) {
    mulAssign(self, other.toUInt())
}

operator fun BigInt.times(other: BigInt): BigInt = mul(this, other)

operator fun BigInt.timesAssign(other: BigInt) {
    mulAssign(this, other)
}

operator fun BigInt.times(other: UInt): BigInt = mul(this, other)

operator fun BigInt.timesAssign(other: UInt) {
    mulAssign(this, other)
}

operator fun UInt.times(other: BigInt): BigInt = mul(this, other)

operator fun BigInt.times(other: ULong): BigInt = mul(this, other)

operator fun BigInt.timesAssign(other: ULong) {
    mulAssign(this, other)
}

operator fun ULong.times(other: BigInt): BigInt = mul(this, other)

operator fun BigInt.times(other: Int): BigInt = mul(this, other)

operator fun BigInt.timesAssign(other: Int) {
    mulAssign(this, other)
}

operator fun Int.times(other: BigInt): BigInt = mul(this, other)

operator fun BigInt.times(other: Long): BigInt = mul(this, other)

operator fun BigInt.timesAssign(other: Long) {
    mulAssign(this, other)
}

operator fun Long.times(other: BigInt): BigInt = mul(this, other)

operator fun BigInt.times(other: UByte): BigInt = mul(this, other.toUInt())

operator fun BigInt.timesAssign(other: UByte) {
    mulAssign(this, other.toUInt())
}

operator fun UByte.times(other: BigInt): BigInt = mul(toUInt(), other)

operator fun BigInt.times(other: UShort): BigInt = mul(this, other.toUInt())

operator fun BigInt.timesAssign(other: UShort) {
    mulAssign(this, other.toUInt())
}

operator fun UShort.times(other: BigInt): BigInt = mul(toUInt(), other)

operator fun BigInt.times(other: Byte): BigInt = mul(this, other.toInt())

operator fun BigInt.timesAssign(other: Byte) {
    mulAssign(this, other.toInt())
}

operator fun Byte.times(other: BigInt): BigInt = mul(toInt(), other)

operator fun BigInt.times(other: Short): BigInt = mul(this, other.toInt())

operator fun BigInt.timesAssign(other: Short) {
    mulAssign(this, other.toInt())
}

operator fun Short.times(other: BigInt): BigInt = mul(toInt(), other)

fun checkedMul(self: BigInt, v: BigInt): BigInt? = mul(self, v)

fun Iterable<BigInt>.productBigInt(): BigInt {
    val product = BigInt.one()
    for (value in this) {
        product.timesAssign(value)
    }
    return product
}

fun Sequence<BigInt>.productBigInt(): BigInt {
    val product = BigInt.one()
    for (value in this) {
        product.timesAssign(value)
    }
    return product
}
