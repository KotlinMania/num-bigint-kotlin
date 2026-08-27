// port-lint: source bigint/bits.rs
package io.github.kotlinmania.numbigint.bigint

import io.github.kotlinmania.numbigint.BIG_DIGIT_BITS
import io.github.kotlinmania.numbigint.BigDigit
import io.github.kotlinmania.numbigint.BigInt
import io.github.kotlinmania.numbigint.BigUint
import io.github.kotlinmania.numbigint.DoubleBigDigit
import io.github.kotlinmania.numbigint.Sign

private const val SIGN_BIT: BigDigit = 0x80000000u

internal class Carry(
    var value: DoubleBigDigit,
)

// Negation in two's complement.
// `acc` must be initialized as 1 for least-significant digit.
//
// When negating, a carry (`acc == 1`) means that all the digits
// considered to this point were zero. This means that if all the
// digits of a negative `BigInt` have been considered, carry must be
// zero as we cannot have negative zero.
//
//    01 -> ...f    ff
//    ff -> ...f    01
// 01 00 -> ...f ff 00
// 01 01 -> ...f fe ff
// 01 ff -> ...f fe 01
// ff 00 -> ...f 01 00
// ff 01 -> ...f 00 ff
// ff ff -> ...f 00 01
internal fun negateCarry(a: BigDigit, acc: Carry): BigDigit {
    acc.value += a.inv().toULong()
    val lo = acc.value.toUInt()
    acc.value = acc.value shr BIG_DIGIT_BITS
    return lo
}

private fun twosComplement(value: BigInt, width: Int): MutableList<BigDigit> {
    val digits = MutableList(width) { 0u }
    for (i in value.data.data.indices
        .take(width)) {
        digits[i] = value.data.data[i]
    }
    if (!value.isNegative()) {
        return digits
    }

    for (i in digits.indices) {
        digits[i] = digits[i].inv()
    }
    addOne(digits)
    return digits
}

private fun addOne(digits: MutableList<BigDigit>) {
    var carry = 1uL
    var i = 0
    while (carry != 0uL && i < digits.size) {
        val sum = digits[i].toULong() + carry
        digits[i] = sum.toUInt()
        carry = sum shr BIG_DIGIT_BITS
        i += 1
    }
}

private fun fromTwosComplement(digits0: List<BigDigit>): BigInt {
    val digits = digits0.toMutableList()
    if (digits.isEmpty() || (digits.last() and SIGN_BIT) == 0u) {
        return BigInt.from(BigUint.new(digits))
    }

    for (i in digits.indices) {
        digits[i] = digits[i].inv()
    }
    addOne(digits)
    return BigInt.fromBiguint(Sign.Minus, BigUint.new(digits))
}

private fun bitwise(a: BigInt, b: BigInt, op: (BigDigit, BigDigit) -> BigDigit): BigInt {
    val width = maxOf(a.data.data.size, b.data.data.size) + 1
    val lhs = twosComplement(a, width)
    val rhs = twosComplement(b, width)
    val result = MutableList(width) { i -> op(lhs[i], rhs[i]) }
    return fromTwosComplement(result)
}

// + 1 & -ff = ...0 01 & ...f 01 = ...0 01 = + 1
// +ff & - 1 = ...0 ff & ...f ff = ...0 ff = +ff
// answer is positive, has length of `a`
fun bitandPosNeg(a: MutableList<BigDigit>, b: List<BigDigit>) {
    val result =
        bitwise(
            BigInt.from(BigUint.new(a)),
            BigInt.fromBiguint(Sign.Minus, BigUint.fromSlice(b)),
        ) { x, y -> x and y }
    a.clear()
    a.addAll(result.data.data)
}

// - 1 & +ff = ...f ff & ...0 ff = ...0 ff = +ff
// -ff & + 1 = ...f 01 & ...0 01 = ...0 01 = + 1
// answer is positive, has length of `b`
fun bitandNegPos(a: MutableList<BigDigit>, b: List<BigDigit>) {
    val result =
        bitwise(
            BigInt.fromBiguint(Sign.Minus, BigUint.new(a)),
            BigInt.from(BigUint.fromSlice(b)),
        ) { x, y -> x and y }
    a.clear()
    a.addAll(result.data.data)
}

// - 1 & -ff = ...f ff & ...f 01 = ...f 01 = - ff
// -ff & - 1 = ...f 01 & ...f ff = ...f 01 = - ff
// -ff & -fe = ...f 01 & ...f 02 = ...f 00 = -100
// answer is negative, has length of longest with a possible carry
fun bitandNegNeg(a: MutableList<BigDigit>, b: List<BigDigit>) {
    val result =
        bitwise(
            BigInt.fromBiguint(Sign.Minus, BigUint.new(a)),
            BigInt.fromBiguint(Sign.Minus, BigUint.fromSlice(b)),
        ) { x, y -> x and y }
    a.clear()
    a.addAll(result.data.data)
}

fun bitand(self: BigInt, other: BigInt): BigInt = bitwise(self, other) { a, b -> a and b }

fun bitandAssign(self: BigInt, other: BigInt) {
    self.cloneFrom(bitand(self, other))
}

// + 1 | -ff = ...0 01 | ...f 01 = ...f 01 = -ff
// +ff | - 1 = ...0 ff | ...f ff = ...f ff = - 1
// answer is negative, has length of `b`
fun bitorPosNeg(a: MutableList<BigDigit>, b: List<BigDigit>) {
    val result =
        bitwise(
            BigInt.from(BigUint.new(a)),
            BigInt.fromBiguint(Sign.Minus, BigUint.fromSlice(b)),
        ) { x, y -> x or y }
    a.clear()
    a.addAll(result.data.data)
}

// - 1 | +ff = ...f ff | ...0 ff = ...f ff = - 1
// -ff | + 1 = ...f 01 | ...0 01 = ...f 01 = -ff
// answer is negative, has length of `a`
fun bitorNegPos(a: MutableList<BigDigit>, b: List<BigDigit>) {
    val result =
        bitwise(
            BigInt.fromBiguint(Sign.Minus, BigUint.new(a)),
            BigInt.from(BigUint.fromSlice(b)),
        ) { x, y -> x or y }
    a.clear()
    a.addAll(result.data.data)
}

// - 1 | -ff = ...f ff | ...f 01 = ...f ff = -1
// -ff | - 1 = ...f 01 | ...f ff = ...f ff = -1
// answer is negative, has length of shortest
fun bitorNegNeg(a: MutableList<BigDigit>, b: List<BigDigit>) {
    val result =
        bitwise(
            BigInt.fromBiguint(Sign.Minus, BigUint.new(a)),
            BigInt.fromBiguint(Sign.Minus, BigUint.fromSlice(b)),
        ) { x, y -> x or y }
    a.clear()
    a.addAll(result.data.data)
}

fun bitor(self: BigInt, other: BigInt): BigInt = bitwise(self, other) { a, b -> a or b }

fun bitorAssign(self: BigInt, other: BigInt) {
    self.cloneFrom(bitor(self, other))
}

// + 1 ^ -ff = ...0 01 ^ ...f 01 = ...f 00 = -100
// +ff ^ - 1 = ...0 ff ^ ...f ff = ...f 00 = -100
// answer is negative, has length of longest with a possible carry
fun bitxorPosNeg(a: MutableList<BigDigit>, b: List<BigDigit>) {
    val result =
        bitwise(
            BigInt.from(BigUint.new(a)),
            BigInt.fromBiguint(Sign.Minus, BigUint.fromSlice(b)),
        ) { x, y -> x xor y }
    a.clear()
    a.addAll(result.data.data)
}

// - 1 ^ +ff = ...f ff ^ ...0 ff = ...f 00 = -100
// -ff ^ + 1 = ...f 01 ^ ...0 01 = ...f 00 = -100
// answer is negative, has length of longest with a possible carry
fun bitxorNegPos(a: MutableList<BigDigit>, b: List<BigDigit>) {
    val result =
        bitwise(
            BigInt.fromBiguint(Sign.Minus, BigUint.new(a)),
            BigInt.from(BigUint.fromSlice(b)),
        ) { x, y -> x xor y }
    a.clear()
    a.addAll(result.data.data)
}

// - 1 ^ -ff = ...f ff ^ ...f 01 = ...0 fe = +fe
// -ff & - 1 = ...f 01 ^ ...f ff = ...0 fe = +fe
// answer is positive, has length of longest
fun bitxorNegNeg(a: MutableList<BigDigit>, b: List<BigDigit>) {
    val result =
        bitwise(
            BigInt.fromBiguint(Sign.Minus, BigUint.new(a)),
            BigInt.fromBiguint(Sign.Minus, BigUint.fromSlice(b)),
        ) { x, y -> x xor y }
    a.clear()
    a.addAll(result.data.data)
}

fun bitxor(self: BigInt, other: BigInt): BigInt = bitwise(self, other) { a, b -> a xor b }

fun bitxorAssign(self: BigInt, other: BigInt) {
    self.cloneFrom(bitxor(self, other))
}

infix fun BigInt.bitAnd(other: BigInt): BigInt = bitand(this, other)

fun BigInt.bitAndAssign(other: BigInt) {
    bitandAssign(this, other)
}

infix fun BigInt.bitOr(other: BigInt): BigInt = bitor(this, other)

fun BigInt.bitOrAssign(other: BigInt) {
    bitorAssign(this, other)
}

infix fun BigInt.bitXor(other: BigInt): BigInt = bitxor(this, other)

fun BigInt.bitXorAssign(other: BigInt) {
    bitxorAssign(this, other)
}

fun setNegativeBit(x: BigInt, bit: ULong, value: Boolean) {
    check(x.isNegative())
    x.setBit(bit, value)
}
