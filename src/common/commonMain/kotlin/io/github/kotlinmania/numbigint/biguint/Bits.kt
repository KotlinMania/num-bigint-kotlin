// port-lint: source biguint/bits.rs
package io.github.kotlinmania.numbigint

// do not use a generic commutative forwarding helper for bitAnd so that we can
// clone the smaller value rather than the larger, avoiding over-allocation
infix fun BigUint.bitAnd(other: BigUint): BigUint {
    // forward to val-ref, choosing the smaller to clone
    val result = if (data.size <= other.data.size) clone() else other.clone()
    val operand = if (data.size <= other.data.size) other else this
    result.bitAndAssign(operand)
    return result
}

fun BigUint.bitAndAssign(other: BigUint) {
    var i = 0
    while (i < data.size && i < other.data.size) {
        data[i] = data[i] and other.data[i]
        i += 1
    }
    while (data.size > other.data.size) {
        data.removeAt(data.lastIndex)
    }
    normalize()
}

infix fun BigUint.bitOr(other: BigUint): BigUint {
    val result = clone()
    result.bitOrAssign(other)
    return result
}

fun BigUint.bitOrAssign(other: BigUint) {
    var i = 0
    while (i < data.size && i < other.data.size) {
        data[i] = data[i] or other.data[i]
        i += 1
    }
    if (other.data.size > data.size) {
        data.addAll(other.data.subList(data.size, other.data.size))
    }
}

infix fun BigUint.bitXor(other: BigUint): BigUint {
    val result = clone()
    result.bitXorAssign(other)
    return result
}

fun BigUint.bitXorAssign(other: BigUint) {
    var i = 0
    while (i < data.size && i < other.data.size) {
        data[i] = data[i] xor other.data[i]
        i += 1
    }
    if (other.data.size > data.size) {
        data.addAll(other.data.subList(data.size, other.data.size))
    }
    normalize()
}
