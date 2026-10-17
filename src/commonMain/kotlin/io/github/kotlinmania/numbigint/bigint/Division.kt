// port-lint: source bigint/division.rs
package io.github.kotlinmania.numbigint.bigint

import io.github.kotlinmania.numbigint.BigInt
import io.github.kotlinmania.numbigint.Sign
import io.github.kotlinmania.numbigint.checkedUabs
import io.github.kotlinmania.numbigint.div
import io.github.kotlinmania.numbigint.divAssign
import io.github.kotlinmania.numbigint.rem
import io.github.kotlinmania.numbigint.remAssign
import io.github.kotlinmania.numbigint.toIntOrNull
import io.github.kotlinmania.numbigint.toUIntOrNull
import io.github.kotlinmania.numbigint.unaryMinus

fun div(self: BigInt, other: BigInt): BigInt {
    val (q, _) = self.divRem(other)
    return q
}

fun divAssign(self: BigInt, other: BigInt) {
    self.cloneFrom(div(self, other))
}

fun div(self: BigInt, other: UInt): BigInt = BigInt.fromBiguint(self.sign(), self.data / other)

fun divAssign(self: BigInt, other: UInt) {
    self.data /= other
    if (self.data.isZero()) {
        self.signValue = Sign.NoSign
    }
}

fun div(self: UInt, other: BigInt): BigInt = BigInt.fromBiguint(other.sign(), self / other.data)

fun div(self: BigInt, other: ULong): BigInt = BigInt.fromBiguint(self.sign(), self.data / other)

fun divAssign(self: BigInt, other: ULong) {
    self.data /= other
    if (self.data.isZero()) {
        self.signValue = Sign.NoSign
    }
}

fun div(self: ULong, other: BigInt): BigInt = BigInt.fromBiguint(other.sign(), self / other.data)

fun div(self: BigInt, other: Int): BigInt {
    val uabs = other.checkedUabs()
    return if (uabs.isPositive) {
        div(self, uabs.value)
    } else {
        div(-self, uabs.value)
    }
}

fun divAssign(self: BigInt, other: Int) {
    val uabs = other.checkedUabs()
    if (uabs.isPositive) {
        divAssign(self, uabs.value)
    } else {
        self.signValue = -self.signValue
        divAssign(self, uabs.value)
    }
}

fun div(self: Int, other: BigInt): BigInt {
    val uabs = self.checkedUabs()
    return if (uabs.isPositive) {
        div(uabs.value, other)
    } else {
        div(uabs.value, -other)
    }
}

fun div(self: BigInt, other: Long): BigInt {
    val uabs = other.checkedUabs()
    return if (uabs.isPositive) {
        div(self, uabs.value)
    } else {
        div(-self, uabs.value)
    }
}

fun divAssign(self: BigInt, other: Long) {
    val uabs = other.checkedUabs()
    if (uabs.isPositive) {
        divAssign(self, uabs.value)
    } else {
        self.signValue = -self.signValue
        divAssign(self, uabs.value)
    }
}

fun div(self: Long, other: BigInt): BigInt {
    val uabs = self.checkedUabs()
    return if (uabs.isPositive) {
        div(uabs.value, other)
    } else {
        div(uabs.value, -other)
    }
}

fun div(self: BigInt, other: UByte): BigInt = div(self, other.toUInt())

fun divAssign(self: BigInt, other: UByte) {
    divAssign(self, other.toUInt())
}

fun div(self: UByte, other: BigInt): BigInt = div(self.toUInt(), other)

fun div(self: BigInt, other: Byte): BigInt = div(self, other.toInt())

fun divAssign(self: BigInt, other: Byte) {
    divAssign(self, other.toInt())
}

fun div(self: Byte, other: BigInt): BigInt = div(self.toInt(), other)

fun rem(self: BigInt, other: BigInt): BigInt {
    val u32 = other.toUIntOrNull()
    if (u32 != null) {
        return rem(self, u32)
    }
    val i32 = other.toIntOrNull()
    if (i32 != null) {
        return rem(self, i32)
    }
    val (_, r) = self.divRem(other)
    return r
}

fun remAssign(self: BigInt, other: BigInt) {
    self.cloneFrom(rem(self, other))
}

fun rem(self: BigInt, other: UInt): BigInt = BigInt.fromBiguint(self.sign(), self.data % other)

fun remAssign(self: BigInt, other: UInt) {
    self.data %= other
    if (self.data.isZero()) {
        self.signValue = Sign.NoSign
    }
}

fun rem(self: UInt, other: BigInt): BigInt = BigInt.from(self % other.data)

fun rem(self: BigInt, other: ULong): BigInt = BigInt.fromBiguint(self.sign(), self.data % other)

fun remAssign(self: BigInt, other: ULong) {
    self.data %= other
    if (self.data.isZero()) {
        self.signValue = Sign.NoSign
    }
}

fun rem(self: ULong, other: BigInt): BigInt = BigInt.from(self % other.data)

fun rem(self: BigInt, other: Int): BigInt = rem(self, other.checkedUabs().value)

fun remAssign(self: BigInt, other: Int) {
    remAssign(self, other.checkedUabs().value)
}

fun rem(self: Int, other: BigInt): BigInt {
    val uabs = self.checkedUabs()
    return if (uabs.isPositive) {
        rem(uabs.value, other)
    } else {
        -rem(uabs.value, other)
    }
}

fun rem(self: BigInt, other: Long): BigInt = rem(self, other.checkedUabs().value)

fun remAssign(self: BigInt, other: Long) {
    remAssign(self, other.checkedUabs().value)
}

fun rem(self: Long, other: BigInt): BigInt {
    val uabs = self.checkedUabs()
    return if (uabs.isPositive) {
        rem(uabs.value, other)
    } else {
        -rem(uabs.value, other)
    }
}

fun rem(self: BigInt, other: UByte): BigInt = rem(self, other.toUInt())

fun remAssign(self: BigInt, other: UByte) {
    remAssign(self, other.toUInt())
}

fun rem(self: UByte, other: BigInt): BigInt = rem(self.toUInt(), other)

fun rem(self: BigInt, other: Byte): BigInt = rem(self, other.toInt())

fun remAssign(self: BigInt, other: Byte) {
    remAssign(self, other.toInt())
}

fun rem(self: Byte, other: BigInt): BigInt = rem(self.toInt(), other)

fun checkedDiv(self: BigInt, v: BigInt): BigInt? {
    if (v.isZero()) {
        return null
    }
    return div(self, v)
}

fun checkedDivEuclid(self: BigInt, v: BigInt): BigInt? {
    if (v.isZero()) {
        return null
    }
    return divEuclid(self, v)
}

fun checkedRemEuclid(self: BigInt, v: BigInt): BigInt? {
    if (v.isZero()) {
        return null
    }
    return remEuclid(self, v)
}

fun checkedDivRemEuclid(self: BigInt, v: BigInt): Pair<BigInt, BigInt>? = divRemEuclid(self, v)

fun divEuclid(self: BigInt, v: BigInt): BigInt {
    val (q, r) = self.divRem(v)
    return if (r.isNegative()) {
        if (v.isPositive()) {
            sub(q, 1u)
        } else {
            add(q, 1u)
        }
    } else {
        q
    }
}

fun remEuclid(self: BigInt, v: BigInt): BigInt {
    val r = rem(self, v)
    return if (r.isNegative()) {
        if (v.isPositive()) {
            add(r, v)
        } else {
            sub(r, v)
        }
    } else {
        r
    }
}

fun divRemEuclid(self: BigInt, v: BigInt): Pair<BigInt, BigInt> {
    val (q, r) = self.divRem(v)
    return if (r.isNegative()) {
        if (v.isPositive()) {
            Pair(sub(q, 1u), add(r, v))
        } else {
            Pair(add(q, 1u), sub(r, v))
        }
    } else {
        Pair(q, r)
    }
}
