// port-lint: source bigint/subtraction.rs
package io.github.kotlinmania.numbigint.bigint

import io.github.kotlinmania.numbigint.BigInt
import io.github.kotlinmania.numbigint.BigUint
import io.github.kotlinmania.numbigint.Sign
import io.github.kotlinmania.numbigint.minus
import io.github.kotlinmania.numbigint.plus
import io.github.kotlinmania.numbigint.unaryMinus

// We want to forward to BigUint.minus, but it is not clear how that will go until
// we compare both sign and magnitude. So this body centralizes every Kotlin
// receiver combination, deferring that decision to BigUint's own forwarding.
private fun subBigInt(a: BigInt, b: BigInt): BigInt =
    when {
        b.sign() == Sign.NoSign -> a.clone()
        a.sign() == Sign.NoSign -> -b.clone()
        a.sign() != b.sign() -> BigInt.fromBiguint(a.sign(), a.data + b.data)
        a.data < b.data -> BigInt.fromBiguint(-a.sign(), b.data - a.data)
        a.data > b.data -> BigInt.fromBiguint(a.sign(), a.data - b.data)
        else -> BigInt.ZERO
    }

fun sub(self: BigInt, other: BigInt): BigInt = subBigInt(self, other)

fun subAssign(self: BigInt, other: BigInt) {
    self.cloneFrom(sub(self, other))
}

fun sub(self: BigInt, other: UInt): BigInt =
    when (self.sign()) {
        Sign.NoSign -> -BigInt.from(other)
        Sign.Minus -> -BigInt.from(self.data + other)
        Sign.Plus -> {
            val unsigned = BigUint.fromUInt(other)
            when {
                self.data == unsigned -> BigInt.ZERO
                self.data > unsigned -> BigInt.from(self.data - unsigned)
                else -> -BigInt.from(unsigned - self.data)
            }
        }
    }

fun subAssign(self: BigInt, other: UInt) {
    self.cloneFrom(sub(self, other))
}

fun sub(self: UInt, other: BigInt): BigInt = -sub(other, self)

fun sub(self: BigInt, other: ULong): BigInt =
    when (self.sign()) {
        Sign.NoSign -> -BigInt.from(other)
        Sign.Minus -> -BigInt.from(self.data + other)
        Sign.Plus -> {
            val unsigned = BigUint.fromULong(other)
            when {
                self.data == unsigned -> BigInt.ZERO
                self.data > unsigned -> BigInt.from(self.data - unsigned)
                else -> -BigInt.from(unsigned - self.data)
            }
        }
    }

fun subAssign(self: BigInt, other: ULong) {
    self.cloneFrom(sub(self, other))
}

fun sub(self: ULong, other: BigInt): BigInt = -sub(other, self)

fun sub(self: BigInt, other: Int): BigInt = sub(self, BigInt.from(other))

fun subAssign(self: BigInt, other: Int) {
    self.cloneFrom(sub(self, other))
}

fun sub(self: Int, other: BigInt): BigInt = sub(BigInt.from(self), other)

fun sub(self: BigInt, other: Long): BigInt = sub(self, BigInt.from(other))

fun subAssign(self: BigInt, other: Long) {
    self.cloneFrom(sub(self, other))
}

fun sub(self: Long, other: BigInt): BigInt = sub(BigInt.from(self), other)

operator fun BigInt.minus(other: BigInt): BigInt = sub(this, other)

operator fun BigInt.minusAssign(other: BigInt) {
    subAssign(this, other)
}

operator fun BigInt.minus(other: UInt): BigInt = sub(this, other)

operator fun BigInt.minusAssign(other: UInt) {
    subAssign(this, other)
}

operator fun UInt.minus(other: BigInt): BigInt = sub(this, other)

operator fun BigInt.minus(other: ULong): BigInt = sub(this, other)

operator fun BigInt.minusAssign(other: ULong) {
    subAssign(this, other)
}

operator fun ULong.minus(other: BigInt): BigInt = sub(this, other)

operator fun BigInt.minus(other: Int): BigInt = sub(this, other)

operator fun BigInt.minusAssign(other: Int) {
    subAssign(this, other)
}

operator fun Int.minus(other: BigInt): BigInt = sub(this, other)

operator fun BigInt.minus(other: Long): BigInt = sub(this, other)

operator fun BigInt.minusAssign(other: Long) {
    subAssign(this, other)
}

operator fun Long.minus(other: BigInt): BigInt = sub(this, other)

fun checkedSub(self: BigInt, v: BigInt): BigInt? = sub(self, v)
