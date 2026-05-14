// port-lint: source biguint/power.rs
package io.github.kotlinmania.numbigint

fun BigUint.pow(exp: BigUint): BigUint {
    return powBigUint(this, exp)
}

internal fun powBigUint(self: BigUint, exp: BigUint): BigUint {
    return if (self.isOne() || exp.isZero()) {
        BigUint.one()
    } else if (self.isZero()) {
        BigUint.ZERO
    } else {
        val primitiveExp = exp.toULongOrNull()
            ?: throw ArithmeticException("memory overflow")
        powBigUint(self, primitiveExp)
    }
}

internal fun powBigUint(self: BigUint, exp: UInt): BigUint {
    return powBigUint(self, exp.toULong())
}

internal fun powBigUint(self: BigUint, exp0: ULong): BigUint {
    var exp = exp0
    if (exp == 0uL) {
        return BigUint.one()
    }
    var base = self.clone()

    while (exp and 1uL == 0uL) {
        base = base * base
        exp = exp shr 1
    }

    if (exp == 1uL) {
        return base
    }

    var acc = base.clone()
    while (exp > 1uL) {
        exp = exp shr 1
        base = base * base
        if (exp and 1uL == 1uL) {
            acc.timesAssign(base)
        }
    }
    return acc
}

internal fun modpowBigUint(x: BigUint, exponent: BigUint, modulus: BigUint): BigUint {
    require(!modulus.isZero()) { "attempt to calculate with zero modulus!" }

    // Otherwise do basically the same as numeric power, but with a modulus.
    return plainModpow(x, exponent.data, modulus)
}

private fun plainModpow(base0: BigUint, expData: List<BigDigit>, modulus: BigUint): BigUint {
    require(!modulus.isZero()) { "attempt to calculate with zero modulus!" }

    val start = expData.indexOfFirst { it != 0u }
    if (start < 0) {
        return BigUint.one()
    }

    var base = base0 % modulus
    repeat(start) {
        repeat(BIG_DIGIT_BITS) {
            base = base * base % modulus
        }
    }

    var r = expData[start]
    var b = 0
    while ((r and 1u) == 0u) {
        base = base * base % modulus
        r = r shr 1
        b += 1
    }

    if (start + 1 == expData.size && r == 1u) {
        return base
    }

    var acc = base.clone()
    r = r shr 1
    b += 1

    fun unit(expIsOdd: Boolean) {
        base = base * base % modulus
        if (expIsOdd) {
            acc.timesAssign(base)
            acc.remAssign(modulus)
        }
    }

    if (start + 1 < expData.size) {
        // consume expData[start]
        var bit = b
        while (bit < BIG_DIGIT_BITS) {
            unit((r and 1u) == 1u)
            r = r shr 1
            bit += 1
        }

        // consume all other digits before the last
        var idx = start + 1
        while (idx < expData.lastIndex) {
            var digit = expData[idx]
            repeat(BIG_DIGIT_BITS) {
                unit((digit and 1u) == 1u)
                digit = digit shr 1
            }
            idx += 1
        }
        r = expData.last()
    }

    check(r != 0u)
    while (r != 0u) {
        unit((r and 1u) == 1u)
        r = r shr 1
    }
    return acc
}
