// port-lint: source biguint/arbitrary.rs
package io.github.kotlinmania.numbigint.biguint

import io.github.kotlinmania.numbigint.BigDigit
import io.github.kotlinmania.numbigint.BigUint
import io.github.kotlinmania.numbigint.biguintFromVec
import io.github.kotlinmania.rand.Rng

internal fun arbitrary(g: Rng): BigUint {
    // Use arbitrary from Vec
    return biguintFromVec(MutableList<BigDigit>((g.nextU32() % 16u).toInt()) { g.nextU32() })
}

internal fun shrink(self: BigUint): Sequence<BigUint> {
    // Use shrinker from Vec
    return self.data.indices
        .asSequence()
        .map { i -> biguintFromVec(self.data.filterIndexedTo(ArrayList(), { idx, _ -> idx != i })) }
}

internal fun arbitrary(u: ByteArray): Result<BigUint> =
    runCatching {
        biguintFromVec(
            MutableList(u.size / 4) { i ->
                (u[i * 4].toUByte().toUInt()) or
                    (u[i * 4 + 1].toUByte().toUInt() shl 8) or
                    (u[i * 4 + 2].toUByte().toUInt() shl 16) or
                    (u[i * 4 + 3].toUByte().toUInt() shl 24)
            },
        )
    }

internal fun arbitraryTakeRest(u: ByteArray): Result<BigUint> =
    arbitrary(u).map { biguintFromVec(it.data.toMutableList()) }

internal fun sizeHint(depth: Int): Pair<Int, Int?> = Pair(depth * 0, null)
