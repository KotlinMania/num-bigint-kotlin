// port-lint: source biguint/shift.rs
package io.github.kotlinmania.numbigint

private fun biguintShl(n: BigUint, shift: ULong): BigUint {
    if (n.isZero()) {
        return n
    }
    val bits = BIG_DIGIT_BITS.toULong()
    val digits = shift / bits
    check(digits <= Int.MAX_VALUE.toULong()) { "capacity overflow" }
    val smallShift = (shift % bits).toInt()
    return biguintShl2(n, digits.toInt(), smallShift)
}

private fun biguintShl2(n: BigUint, digits: Int, shift: Int): BigUint {
    val data = when (digits) {
        0 -> n.data.toMutableList()
        else -> {
            val len = digits.saturatingAdd(n.data.size + 1)
            val data = ArrayList<BigDigit>(len)
            repeat(digits) {
                data.add(0u)
            }
            data.addAll(n.data)
            data
        }
    }

    if (shift > 0) {
        var carry = 0u
        val carryShift = BIG_DIGIT_BITS - shift
        var i = digits
        while (i < data.size) {
            val newCarry = data[i] shr carryShift
            data[i] = (data[i] shl shift) or carry
            carry = newCarry
            i += 1
        }
        if (carry != 0u) {
            data.add(carry)
        }
    }

    return biguintFromVec(data)
}

private fun biguintShr(n: BigUint, shift: ULong): BigUint {
    if (n.isZero()) {
        return n
    }
    val bits = BIG_DIGIT_BITS.toULong()
    val digits = shift / bits
    val smallShift = (shift % bits).toInt()
    return biguintShr2(n, if (digits > Int.MAX_VALUE.toULong()) Int.MAX_VALUE else digits.toInt(), smallShift)
}

private fun biguintShr2(n: BigUint, digits: Int, shift: Int): BigUint {
    if (digits >= n.data.size) {
        val n = n.clone()
        n.setZero()
        return n
    }
    val data = n.data.subList(digits, n.data.size).toMutableList()

    if (shift > 0) {
        var borrow = 0u
        val borrowShift = BIG_DIGIT_BITS - shift
        var i = data.lastIndex
        while (i >= 0) {
            val newBorrow = data[i] shl borrowShift
            data[i] = (data[i] shr shift) or borrow
            borrow = newBorrow
            i -= 1
        }
    }

    return biguintFromVec(data)
}

fun BigUint.shiftLeft(rhs: ULong): BigUint {
    return biguintShl(this, rhs)
}

fun BigUint.shiftLeftAssign(rhs: ULong) {
    val n = clone()
    data.clear()
    data.addAll(n.shiftLeft(rhs).data)
}

fun BigUint.shiftRight(rhs: ULong): BigUint {
    return biguintShr(this, rhs)
}

fun BigUint.shiftRightAssign(rhs: ULong) {
    val n = clone()
    data.clear()
    data.addAll(n.shiftRight(rhs).data)
}

private fun Int.saturatingAdd(rhs: Int): Int {
    val sum = toLong() + rhs.toLong()
    return if (sum > Int.MAX_VALUE) Int.MAX_VALUE else sum.toInt()
}
