// port-lint: source bigint/addition.rs
package io.github.kotlinmania.numbigint.bigint

import io.github.kotlinmania.numbigint.BigInt
import io.github.kotlinmania.numbigint.BigUint
import io.github.kotlinmania.numbigint.Sign
import io.github.kotlinmania.numbigint.minus
import io.github.kotlinmania.numbigint.plus

// We want to forward to BigUint.plus, but it is not clear how that will go until
// we compare both sign and magnitude. So this body centralizes every Kotlin
// receiver combination, deferring that decision to BigUint's own forwarding.
private fun addBigInt(a: BigInt, b: BigInt): BigInt =
    when {
        b.sign() == Sign.NoSign -> a.clone()
        a.sign() == Sign.NoSign -> b.clone()
        a.sign() == b.sign() -> BigInt.fromBiguint(a.sign(), a.data + b.data)
        a.data < b.data -> BigInt.fromBiguint(b.sign(), b.data - a.data)
        a.data > b.data -> BigInt.fromBiguint(a.sign(), a.data - b.data)
        else -> BigInt.ZERO
    }

fun add(self: BigInt, other: BigInt): BigInt = addBigInt(self, other)

fun addAssign(self: BigInt, other: BigInt) {
    self.cloneFrom(add(self, other))
}

fun add(self: BigInt, other: UInt): BigInt =
    when (self.sign()) {
        Sign.NoSign -> BigInt.from(other)
        Sign.Plus -> BigInt.from(self.data + other)
        Sign.Minus -> {
            val unsigned = BigUint.fromUInt(other)
            when {
                self.data == unsigned -> BigInt.ZERO
                self.data < unsigned -> BigInt.from(unsigned - self.data)
                else -> -BigInt.from(self.data - unsigned)
            }
        }
    }

fun addAssign(self: BigInt, other: UInt) {
    self.cloneFrom(add(self, other))
}

fun add(self: UInt, other: BigInt): BigInt = add(other, self)

fun add(self: BigInt, other: ULong): BigInt =
    when (self.sign()) {
        Sign.NoSign -> BigInt.from(other)
        Sign.Plus -> BigInt.from(self.data + other)
        Sign.Minus -> {
            val unsigned = BigUint.fromULong(other)
            when {
                self.data == unsigned -> BigInt.ZERO
                self.data < unsigned -> BigInt.from(unsigned - self.data)
                else -> -BigInt.from(self.data - unsigned)
            }
        }
    }

fun addAssign(self: BigInt, other: ULong) {
    self.cloneFrom(add(self, other))
}

fun add(self: ULong, other: BigInt): BigInt = add(other, self)

fun add(self: BigInt, other: Int): BigInt = add(self, BigInt.from(other))

fun addAssign(self: BigInt, other: Int) {
    self.cloneFrom(add(self, other))
}

fun add(self: Int, other: BigInt): BigInt = add(BigInt.from(self), other)

fun add(self: BigInt, other: Long): BigInt = add(self, BigInt.from(other))

fun addAssign(self: BigInt, other: Long) {
    self.cloneFrom(add(self, other))
}

fun add(self: Long, other: BigInt): BigInt = add(BigInt.from(self), other)

operator fun BigInt.plus(other: BigInt): BigInt = add(this, other)

operator fun BigInt.plusAssign(other: BigInt) {
    addAssign(this, other)
}

operator fun BigInt.plus(other: UInt): BigInt = add(this, other)

operator fun BigInt.plusAssign(other: UInt) {
    addAssign(this, other)
}

operator fun UInt.plus(other: BigInt): BigInt = add(this, other)

operator fun BigInt.plus(other: ULong): BigInt = add(this, other)

operator fun BigInt.plusAssign(other: ULong) {
    addAssign(this, other)
}

operator fun ULong.plus(other: BigInt): BigInt = add(this, other)

operator fun BigInt.plus(other: Int): BigInt = add(this, other)

operator fun BigInt.plusAssign(other: Int) {
    addAssign(this, other)
}

operator fun Int.plus(other: BigInt): BigInt = add(this, other)

operator fun BigInt.plus(other: Long): BigInt = add(this, other)

operator fun BigInt.plusAssign(other: Long) {
    addAssign(this, other)
}

operator fun Long.plus(other: BigInt): BigInt = add(this, other)

fun checkedAdd(self: BigInt, v: BigInt): BigInt? = add(self, v)

fun Iterable<BigInt>.sumBigInt(): BigInt {
    var sum = BigInt.ZERO
    for (value in this) {
        sum.plusAssign(value)
    }
    return sum
}

fun Sequence<BigInt>.sumBigInt(): BigInt {
    var sum = BigInt.ZERO
    for (value in this) {
        sum.plusAssign(value)
    }
    return sum
}
