// port-lint: source bigint/division.rs
package io.github.kotlinmania.numbigint.bigint

import io.github.kotlinmania.numbigint.BigInt
import io.github.kotlinmania.numbigint.div
import io.github.kotlinmania.numbigint.rem

fun div(self: BigInt, other: BigInt): BigInt {
    val (q, _) = self.divRem(other)
    return q
}

fun divAssign(self: BigInt, other: BigInt) {
    self.cloneFrom(div(self, other))
}

fun div(self: BigInt, other: UInt): BigInt {
    return BigInt.fromBiguint(self.sign(), self.data / other)
}

fun divAssign(self: BigInt, other: UInt) {
    self.cloneFrom(div(self, other))
}

fun div(self: UInt, other: BigInt): BigInt {
    return BigInt.fromBiguint(other.sign(), self / other.data)
}

fun div(self: BigInt, other: ULong): BigInt {
    return BigInt.fromBiguint(self.sign(), self.data / other)
}

fun divAssign(self: BigInt, other: ULong) {
    self.cloneFrom(div(self, other))
}

fun div(self: ULong, other: BigInt): BigInt {
    return BigInt.fromBiguint(other.sign(), self / other.data)
}

fun div(self: BigInt, other: Int): BigInt {
    return div(self, BigInt.from(other))
}

fun divAssign(self: BigInt, other: Int) {
    self.cloneFrom(div(self, other))
}

fun div(self: Int, other: BigInt): BigInt {
    return div(BigInt.from(self), other)
}

fun div(self: BigInt, other: Long): BigInt {
    return div(self, BigInt.from(other))
}

fun divAssign(self: BigInt, other: Long) {
    self.cloneFrom(div(self, other))
}

fun div(self: Long, other: BigInt): BigInt {
    return div(BigInt.from(self), other)
}

fun rem(self: BigInt, other: BigInt): BigInt {
    val (_, r) = self.divRem(other)
    return r
}

fun remAssign(self: BigInt, other: BigInt) {
    self.cloneFrom(rem(self, other))
}

fun rem(self: BigInt, other: UInt): BigInt {
    return BigInt.fromBiguint(self.sign(), self.data % other)
}

fun remAssign(self: BigInt, other: UInt) {
    self.cloneFrom(rem(self, other))
}

fun rem(self: UInt, other: BigInt): BigInt {
    return BigInt.from(self % other.data)
}

fun rem(self: BigInt, other: ULong): BigInt {
    return BigInt.fromBiguint(self.sign(), self.data % other)
}

fun remAssign(self: BigInt, other: ULong) {
    self.cloneFrom(rem(self, other))
}

fun rem(self: ULong, other: BigInt): BigInt {
    return BigInt.from(self % other.data)
}

fun rem(self: BigInt, other: Int): BigInt {
    return rem(self, BigInt.from(other))
}

fun remAssign(self: BigInt, other: Int) {
    self.cloneFrom(rem(self, other))
}

fun rem(self: Int, other: BigInt): BigInt {
    return rem(BigInt.from(self), other)
}

fun rem(self: BigInt, other: Long): BigInt {
    return rem(self, BigInt.from(other))
}

fun remAssign(self: BigInt, other: Long) {
    self.cloneFrom(rem(self, other))
}

fun rem(self: Long, other: BigInt): BigInt {
    return rem(BigInt.from(self), other)
}

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

fun checkedDivRemEuclid(self: BigInt, v: BigInt): Pair<BigInt, BigInt>? {
    return divRemEuclid(self, v)
}

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
