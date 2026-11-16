// port-lint: source bigint/bits.rs
package io.github.kotlinmania.numbigint.bigint

import io.github.kotlinmania.numbigint.BIG_DIGIT_BITS
import io.github.kotlinmania.numbigint.BIG_DIGIT_MAX
import io.github.kotlinmania.numbigint.BigDigit
import io.github.kotlinmania.numbigint.BigInt
import io.github.kotlinmania.numbigint.Sign
import io.github.kotlinmania.numbigint.bitAnd
import io.github.kotlinmania.numbigint.bitAndAssign
import io.github.kotlinmania.numbigint.bitOr
import io.github.kotlinmania.numbigint.bitOrAssign
import io.github.kotlinmania.numbigint.bitXorAssign

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
internal fun negateCarry(a: BigDigit, acc: ULongArray): BigDigit {
    acc[0] += a.inv().toULong()
    val mask = BIG_DIGIT_MAX.toULong()
    val lo = acc[0] and mask
    acc[0] = acc[0] shr BIG_DIGIT_BITS
    return lo.toUInt()
}

// + 1 & -ff = ...0 01 & ...f 01 = ...0 01 = + 1
// +ff & - 1 = ...0 ff & ...f ff = ...0 ff = +ff
// answer is positive, has length of `a`
fun bitandPosNeg(a: MutableList<BigDigit>, b: List<BigDigit>) {
    val carryB = ulongArrayOf(1uL)
    val limit = minOf(a.size, b.size)
    for (i in 0 until limit) {
        val twosB = negateCarry(b[i], carryB)
        a[i] = a[i] and twosB
    }
    check(b.size > a.size || carryB[0] == 0uL)
}

// - 1 & +ff = ...f ff & ...0 ff = ...0 ff = +ff
// -ff & + 1 = ...f 01 & ...0 01 = ...0 01 = + 1
// answer is positive, has length of `b`
fun bitandNegPos(a: MutableList<BigDigit>, b: List<BigDigit>) {
    val carryA = ulongArrayOf(1uL)
    val limit = minOf(a.size, b.size)
    for (i in 0 until limit) {
        val twosA = negateCarry(a[i], carryA)
        a[i] = twosA and b[i]
    }
    check(a.size > b.size || carryA[0] == 0uL)
    when {
        a.size > b.size -> {
            while (a.size > b.size) {
                a.removeAt(a.lastIndex)
            }
        }

        a.size < b.size -> {
            a.addAll(b.subList(a.size, b.size))
        }
    }
}

// - 1 & -ff = ...f ff & ...f 01 = ...f 01 = - ff
// -ff & - 1 = ...f 01 & ...f ff = ...f 01 = - ff
// -ff & -fe = ...f 01 & ...f 02 = ...f 00 = -100
// answer is negative, has length of longest with a possible carry
fun bitandNegNeg(a: MutableList<BigDigit>, b: List<BigDigit>) {
    val carryA = ulongArrayOf(1uL)
    val carryB = ulongArrayOf(1uL)
    val carryAnd = ulongArrayOf(1uL)
    val limit = minOf(a.size, b.size)
    for (i in 0 until limit) {
        val twosA = negateCarry(a[i], carryA)
        val twosB = negateCarry(b[i], carryB)
        a[i] = negateCarry(twosA and twosB, carryAnd)
    }
    check(a.size > b.size || carryA[0] == 0uL)
    check(b.size > a.size || carryB[0] == 0uL)
    when {
        a.size > b.size -> {
            for (i in b.size until a.size) {
                val twosA = negateCarry(a[i], carryA)
                a[i] = negateCarry(twosA, carryAnd)
            }
            check(carryA[0] == 0uL)
        }

        a.size < b.size -> {
            for (i in a.size until b.size) {
                val twosB = negateCarry(b[i], carryB)
                a.add(negateCarry(twosB, carryAnd))
            }
            check(carryB[0] == 0uL)
        }
    }
    if (carryAnd[0] != 0uL) {
        a.add(1u)
    }
}

infix fun BigInt.bitAnd(other: BigInt): BigInt =
    when {
        sign() == Sign.NoSign || other.sign() == Sign.NoSign -> {
            BigInt.ZERO
        }

        sign() == Sign.Plus && other.sign() == Sign.Plus -> {
            BigInt.from(data bitAnd other.data)
        }

        sign() == Sign.Plus && other.sign() == Sign.Minus -> {
            val copy = clone()
            copy.bitAndAssign(other)
            copy
        }

        sign() == Sign.Minus && other.sign() == Sign.Plus -> {
            val copy = other.clone()
            copy.bitAndAssign(this)
            copy
        }

        else -> {
            // forward to val-ref, choosing the larger to clone
            if (len() >= other.len()) {
                val copy = clone()
                copy.bitAndAssign(other)
                copy
            } else {
                val copy = other.clone()
                copy.bitAndAssign(this)
                copy
            }
        }
    }

fun BigInt.bitAndAssign(other: BigInt) {
    if (sign() == Sign.NoSign) {
        return
    }
    when {
        other.sign() == Sign.NoSign -> {
            setZero()
        }

        sign() == Sign.Plus && other.sign() == Sign.Plus -> {
            data.bitAndAssign(other.data)
            if (data.isZero()) {
                signValue = Sign.NoSign
            }
        }

        sign() == Sign.Plus && other.sign() == Sign.Minus -> {
            bitandPosNeg(digitsMut(), other.digits())
            normalize()
        }

        sign() == Sign.Minus && other.sign() == Sign.Plus -> {
            bitandNegPos(digitsMut(), other.digits())
            signValue = Sign.Plus
            normalize()
        }

        else -> {
            bitandNegNeg(digitsMut(), other.digits())
            normalize()
        }
    }
}

// + 1 | -ff = ...0 01 | ...f 01 = ...f 01 = -ff
// +ff | - 1 = ...0 ff | ...f ff = ...f ff = - 1
// answer is negative, has length of `b`
fun bitorPosNeg(a: MutableList<BigDigit>, b: List<BigDigit>) {
    val carryB = ulongArrayOf(1uL)
    val carryOr = ulongArrayOf(1uL)
    val limit = minOf(a.size, b.size)
    for (i in 0 until limit) {
        val twosB = negateCarry(b[i], carryB)
        a[i] = negateCarry(a[i] or twosB, carryOr)
    }
    check(b.size > a.size || carryB[0] == 0uL)
    when {
        a.size > b.size -> {
            while (a.size > b.size) {
                a.removeAt(a.lastIndex)
            }
        }

        a.size < b.size -> {
            for (i in a.size until b.size) {
                val twosB = negateCarry(b[i], carryB)
                a.add(negateCarry(twosB, carryOr))
            }
            check(carryB[0] == 0uL)
        }
    }
    // for carryOr to be non-zero, we would need twosB == 0
    check(carryOr[0] == 0uL)
}

// - 1 | +ff = ...f ff | ...0 ff = ...f ff = - 1
// -ff | + 1 = ...f 01 | ...0 01 = ...f 01 = -ff
// answer is negative, has length of `a`
fun bitorNegPos(a: MutableList<BigDigit>, b: List<BigDigit>) {
    val carryA = ulongArrayOf(1uL)
    val carryOr = ulongArrayOf(1uL)
    val limit = minOf(a.size, b.size)
    for (i in 0 until limit) {
        val twosA = negateCarry(a[i], carryA)
        a[i] = negateCarry(twosA or b[i], carryOr)
    }
    check(a.size > b.size || carryA[0] == 0uL)
    if (a.size > b.size) {
        for (i in b.size until a.size) {
            val twosA = negateCarry(a[i], carryA)
            a[i] = negateCarry(twosA, carryOr)
        }
        check(carryA[0] == 0uL)
    }
    // for carryOr to be non-zero, we would need twosA == 0
    check(carryOr[0] == 0uL)
}

// - 1 | -ff = ...f ff | ...f 01 = ...f ff = -1
// -ff | - 1 = ...f 01 | ...f ff = ...f ff = -1
// answer is negative, has length of shortest
fun bitorNegNeg(a: MutableList<BigDigit>, b: List<BigDigit>) {
    val carryA = ulongArrayOf(1uL)
    val carryB = ulongArrayOf(1uL)
    val carryOr = ulongArrayOf(1uL)
    val limit = minOf(a.size, b.size)
    for (i in 0 until limit) {
        val twosA = negateCarry(a[i], carryA)
        val twosB = negateCarry(b[i], carryB)
        a[i] = negateCarry(twosA or twosB, carryOr)
    }
    check(a.size > b.size || carryA[0] == 0uL)
    check(b.size > a.size || carryB[0] == 0uL)
    while (a.size > b.size) {
        a.removeAt(a.lastIndex)
    }
    // for carryOr to be non-zero, we would need twosA == 0 or twosB == 0
    check(carryOr[0] == 0uL)
}

infix fun BigInt.bitOr(other: BigInt): BigInt =
    when {
        sign() == Sign.NoSign -> {
            other.clone()
        }

        other.sign() == Sign.NoSign -> {
            clone()
        }

        sign() == Sign.Plus && other.sign() == Sign.Plus -> {
            BigInt.from(data bitOr other.data)
        }

        sign() == Sign.Plus && other.sign() == Sign.Minus -> {
            val copy = other.clone()
            copy.bitOrAssign(this)
            copy
        }

        sign() == Sign.Minus && other.sign() == Sign.Plus -> {
            val copy = clone()
            copy.bitOrAssign(other)
            copy
        }

        else -> {
            // forward to val-ref, choosing the smaller to clone
            if (len() <= other.len()) {
                val copy = clone()
                copy.bitOrAssign(other)
                copy
            } else {
                val copy = other.clone()
                copy.bitOrAssign(this)
                copy
            }
        }
    }

fun BigInt.bitOrAssign(other: BigInt) {
    if (other.sign() == Sign.NoSign) {
        return
    }
    when {
        sign() == Sign.NoSign -> {
            cloneFrom(other)
        }

        sign() == Sign.Plus && other.sign() == Sign.Plus -> {
            data.bitOrAssign(other.data)
        }

        sign() == Sign.Plus && other.sign() == Sign.Minus -> {
            bitorPosNeg(digitsMut(), other.digits())
            signValue = Sign.Minus
            normalize()
        }

        sign() == Sign.Minus && other.sign() == Sign.Plus -> {
            bitorNegPos(digitsMut(), other.digits())
            normalize()
        }

        else -> {
            bitorNegNeg(digitsMut(), other.digits())
            normalize()
        }
    }
}

// + 1 ^ -ff = ...0 01 ^ ...f 01 = ...f 00 = -100
// +ff ^ - 1 = ...0 ff ^ ...f ff = ...f 00 = -100
// answer is negative, has length of longest with a possible carry
fun bitxorPosNeg(a: MutableList<BigDigit>, b: List<BigDigit>) {
    val carryB = ulongArrayOf(1uL)
    val carryXor = ulongArrayOf(1uL)
    val limit = minOf(a.size, b.size)
    for (i in 0 until limit) {
        val twosB = negateCarry(b[i], carryB)
        a[i] = negateCarry(a[i] xor twosB, carryXor)
    }
    check(b.size > a.size || carryB[0] == 0uL)
    when {
        a.size > b.size -> {
            for (i in b.size until a.size) {
                val twosB = 0u.inv()
                a[i] = negateCarry(a[i] xor twosB, carryXor)
            }
        }

        a.size < b.size -> {
            for (i in a.size until b.size) {
                val twosB = negateCarry(b[i], carryB)
                a.add(negateCarry(twosB, carryXor))
            }
            check(carryB[0] == 0uL)
        }
    }
    if (carryXor[0] != 0uL) {
        a.add(1u)
    }
}

// - 1 ^ +ff = ...f ff ^ ...0 ff = ...f 00 = -100
// -ff ^ + 1 = ...f 01 ^ ...0 01 = ...f 00 = -100
// answer is negative, has length of longest with a possible carry
fun bitxorNegPos(a: MutableList<BigDigit>, b: List<BigDigit>) {
    val carryA = ulongArrayOf(1uL)
    val carryXor = ulongArrayOf(1uL)
    val limit = minOf(a.size, b.size)
    for (i in 0 until limit) {
        val twosA = negateCarry(a[i], carryA)
        a[i] = negateCarry(twosA xor b[i], carryXor)
    }
    check(a.size > b.size || carryA[0] == 0uL)
    when {
        a.size > b.size -> {
            for (i in b.size until a.size) {
                val twosA = negateCarry(a[i], carryA)
                a[i] = negateCarry(twosA, carryXor)
            }
            check(carryA[0] == 0uL)
        }

        a.size < b.size -> {
            for (i in a.size until b.size) {
                val twosA = 0u.inv()
                a.add(negateCarry(twosA xor b[i], carryXor))
            }
        }
    }
    if (carryXor[0] != 0uL) {
        a.add(1u)
    }
}

// - 1 ^ -ff = ...f ff ^ ...f 01 = ...0 fe = +fe
// -ff & - 1 = ...f 01 ^ ...f ff = ...0 fe = +fe
// answer is positive, has length of longest
fun bitxorNegNeg(a: MutableList<BigDigit>, b: List<BigDigit>) {
    val carryA = ulongArrayOf(1uL)
    val carryB = ulongArrayOf(1uL)
    val limit = minOf(a.size, b.size)
    for (i in 0 until limit) {
        val twosA = negateCarry(a[i], carryA)
        val twosB = negateCarry(b[i], carryB)
        a[i] = twosA xor twosB
    }
    check(a.size > b.size || carryA[0] == 0uL)
    check(b.size > a.size || carryB[0] == 0uL)
    when {
        a.size > b.size -> {
            for (i in b.size until a.size) {
                val twosA = negateCarry(a[i], carryA)
                val twosB = 0u.inv()
                a[i] = twosA xor twosB
            }
            check(carryA[0] == 0uL)
        }

        a.size < b.size -> {
            for (i in a.size until b.size) {
                val twosA = 0u.inv()
                val twosB = negateCarry(b[i], carryB)
                a.add(twosA xor twosB)
            }
            check(carryB[0] == 0uL)
        }
    }
}

infix fun BigInt.bitXor(other: BigInt): BigInt =
    if (len() >= other.len()) {
        val copy = clone()
        copy.bitXorAssign(other)
        copy
    } else {
        val copy = other.clone()
        copy.bitXorAssign(this)
        copy
    }

fun BigInt.bitXorAssign(other: BigInt) {
    if (other.sign() == Sign.NoSign) {
        return
    }
    when {
        sign() == Sign.NoSign -> {
            cloneFrom(other)
        }

        sign() == Sign.Plus && other.sign() == Sign.Plus -> {
            data.bitXorAssign(other.data)
            if (data.isZero()) {
                signValue = Sign.NoSign
            }
        }

        sign() == Sign.Plus && other.sign() == Sign.Minus -> {
            bitxorPosNeg(digitsMut(), other.digits())
            signValue = Sign.Minus
            normalize()
        }

        sign() == Sign.Minus && other.sign() == Sign.Plus -> {
            bitxorNegPos(digitsMut(), other.digits())
            normalize()
        }

        else -> {
            bitxorNegNeg(digitsMut(), other.digits())
            signValue = Sign.Plus
            normalize()
        }
    }
}

internal fun setNegativeBit(x: BigInt, bit: ULong, value: Boolean) {
    check(x.sign() == Sign.Minus)
    val data = x.data

    val bitsPerDigit = BIG_DIGIT_BITS.toULong()
    if (bit >= bitsPerDigit * data.len().toULong()) {
        if (!value) {
            data.setBit(bit, true)
        }
    } else {
        // If the Uint number is
        //   ... 0  x 1 0 ... 0
        // then the two's complement is
        //   ... 1 !x 1 0 ... 0
        //            |-- bit at position `trailingZeros`
        // where !x is obtained from x by flipping each bit
        val trailingZeros = data.trailingZeros()!!
        if (bit > trailingZeros) {
            data.setBit(bit, !value)
        } else if (bit == trailingZeros && !value) {
            // Clearing the bit at position `trailingZeros` is dealt with by doing
            // similarly to what `bitandNegPos` does, except we start at digit
            // `bitIndex`. All digits below `bitIndex` are guaranteed to be zero,
            // so initially we have `carryIn` = `carryOut` = 1. Furthermore, we
            // stop traversing the digits when there are no more carries.
            val bitIndexLong = bit / bitsPerDigit
            check(bitIndexLong <= Int.MAX_VALUE.toULong())
            val bitIndex = bitIndexLong.toInt()
            val bitMask = 1u shl (bit % bitsPerDigit).toInt()
            val digits = data.digitsMut()
            val carryIn = ulongArrayOf(1uL)
            val carryOut = ulongArrayOf(1uL)

            val twosIn = negateCarry(digits[bitIndex], carryIn)
            val twosOut = twosIn and bitMask.inv()
            digits[bitIndex] = negateCarry(twosOut, carryOut)

            for (idx in (bitIndex + 1) until digits.size) {
                if (carryIn[0] == 0uL && carryOut[0] == 0uL) {
                    // Exit the loop since no more digits can change
                    break
                }
                val twos = negateCarry(digits[idx], carryIn)
                digits[idx] = negateCarry(twos, carryOut)
            }

            if (carryOut[0] != 0uL) {
                // All digits have been traversed and there is a carry
                check(carryIn[0] == 0uL)
                digits.add(1u)
            }
        } else if (bit < trailingZeros && value) {
            // Flip each bit from position `bit` to `trailingZeros`, both inclusive
            //       ... 1 !x 1 0 ... 0 ... 0
            //                        |-- bit at position `bit`
            //                |-- bit at position `trailingZeros`
            // bitMask:       1 1 ... 1 0 .. 0
            // This is done by xoring with the bitMask
            val indexLoLong = bit / bitsPerDigit
            val indexHiLong = trailingZeros / bitsPerDigit
            check(indexLoLong <= Int.MAX_VALUE.toULong() && indexHiLong <= Int.MAX_VALUE.toULong())
            val indexLo = indexLoLong.toInt()
            val indexHi = indexHiLong.toInt()
            val bitMaskLo = BIG_DIGIT_MAX shl (bit % bitsPerDigit).toInt()
            val bitMaskHi =
                BIG_DIGIT_MAX shr (bitsPerDigit - 1uL - (trailingZeros % bitsPerDigit)).toInt()
            val digits = data.digitsMut()

            if (indexLo == indexHi) {
                digits[indexLo] = digits[indexLo] xor (bitMaskLo and bitMaskHi)
            } else {
                digits[indexLo] = bitMaskLo
                for (idx in (indexLo + 1) until indexHi) {
                    digits[idx] = BIG_DIGIT_MAX
                }
                digits[indexHi] = digits[indexHi] xor bitMaskHi
            }
        }
        // In the remaining two cases (bit == trailingZeros && value, or bit < trailingZeros && !value),
        // the bit is already set or cleared respectively.
    }
}
