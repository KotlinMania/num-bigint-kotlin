// port-lint: source bigint/arbitrary.rs
package io.github.kotlinmania.numbigint.bigint

import io.github.kotlinmania.numbigint.BigInt
import io.github.kotlinmania.numbigint.Sign
import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.numbigint.biguint.arbitrary as biguintArbitrary
import io.github.kotlinmania.numbigint.biguint.arbitraryTakeRest as biguintArbitraryTakeRest
import io.github.kotlinmania.numbigint.biguint.shrink as biguintShrink
import io.github.kotlinmania.numbigint.biguint.sizeHint as biguintSizeHint

internal fun arbitrary(g: Rng): BigInt {
    val positive = (g.nextU32() and 1u) != 0u
    val sign = if (positive) Sign.Plus else Sign.Minus
    return BigInt.fromBiguint(sign, biguintArbitrary(g))
}

internal fun shrink(self: BigInt): Sequence<BigInt> {
    val sign = self.sign()
    val unsignedShrink = biguintShrink(self.data)
    return unsignedShrink.map { x -> BigInt.fromBiguint(sign, x) }
}

internal fun arbitrary(u: ByteArray): Result<BigInt> {
    val positive = u.isNotEmpty() && (u[0].toInt() and 1) != 0
    val sign = if (positive) Sign.Plus else Sign.Minus
    return biguintArbitrary(if (u.isNotEmpty()) u.copyOfRange(1, u.size) else ByteArray(0)).map {
        BigInt.fromBiguint(sign, it)
    }
}

internal fun arbitraryTakeRest(u: ByteArray): Result<BigInt> {
    val positive = u.isNotEmpty() && (u[0].toInt() and 1) != 0
    val sign = if (positive) Sign.Plus else Sign.Minus
    return biguintArbitraryTakeRest(if (u.isNotEmpty()) u.copyOfRange(1, u.size) else ByteArray(0)).map {
        BigInt.fromBiguint(sign, it)
    }
}

internal fun sizeHint(depth: Int): Pair<Int, Int?> =
    Pair(1 + biguintSizeHint(depth).first, biguintSizeHint(depth).second?.plus(1))
