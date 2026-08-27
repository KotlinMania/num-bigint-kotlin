// port-lint: source bigint/power.rs
package io.github.kotlinmania.numbigint.bigint

import io.github.kotlinmania.numbigint.BigInt
import io.github.kotlinmania.numbigint.BigUint
import io.github.kotlinmania.numbigint.Sign
import io.github.kotlinmania.numbigint.minus
import io.github.kotlinmania.numbigint.pow
import io.github.kotlinmania.numbigint.unaryMinus

/**
 * Help function for [pow].
 *
 * Computes the effect of the exponent on the sign.
 */
fun powsign(sign: Sign, other: UInt): Sign =
    if (other == 0u) {
        Sign.Plus
    } else if (sign != Sign.Minus || other % 2u == 1u) {
        sign
    } else {
        -sign
    }

fun powsign(sign: Sign, other: ULong): Sign =
    if (other == 0uL) {
        Sign.Plus
    } else if (sign != Sign.Minus || other % 2uL == 1uL) {
        sign
    } else {
        -sign
    }

fun powsign(sign: Sign, other: BigUint): Sign =
    if (other.isZero()) {
        Sign.Plus
    } else if (sign != Sign.Minus || other.isOdd()) {
        sign
    } else {
        -sign
    }

fun pow(self: BigInt, rhs: UInt): BigInt = BigInt.fromBiguint(powsign(self.sign(), rhs), self.data.pow(rhs))

fun pow(self: BigInt, rhs: ULong): BigInt = BigInt.fromBiguint(powsign(self.sign(), rhs), self.data.pow(BigUint.fromULong(rhs)))

fun pow(self: BigInt, rhs: BigUint): BigInt = BigInt.fromBiguint(powsign(self.sign(), rhs), self.data.pow(rhs))

fun modpow(x: BigInt, exponent: BigInt, modulus: BigInt): BigInt {
    require(!exponent.isNegative()) { "negative exponentiation is not supported!" }
    require(!modulus.isZero()) { "attempt to calculate with zero modulus!" }

    val result = x.data.modpow(exponent.data, modulus.data)
    if (result.isZero()) {
        return BigInt.ZERO
    }

    // The sign of the result follows the modulus, like `modFloor`.
    val negativePower = x.isNegative() && exponent.isOdd()
    val negativeModulus = modulus.isNegative()
    val (sign, mag) =
        when {
            !negativePower && !negativeModulus -> Pair(Sign.Plus, result)
            negativePower && !negativeModulus -> Pair(Sign.Plus, modulus.data - result)
            !negativePower && negativeModulus -> Pair(Sign.Minus, modulus.data - result)
            else -> Pair(Sign.Minus, result)
        }
    return BigInt.fromBiguint(sign, mag)
}
