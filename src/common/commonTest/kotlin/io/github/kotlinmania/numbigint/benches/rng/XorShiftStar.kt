package io.github.kotlinmania.numbigint.benches.rng

import io.github.kotlinmania.rand.Rng

internal class Bencher {
    inline fun <T> iter(block: () -> T): T = block()
}

internal fun getRng(): Rng =
    XorShiftStar(
        a = 0x0123_4567_89AB_CDEFuL,
    )

/** Simple [Rng] for benchmarking without additional dependencies */
internal class XorShiftStar(
    var a: ULong,
) : Rng {
    override fun nextU32(): UInt = nextU64().toUInt()

    override fun nextU64(): ULong {
        // https://en.wikipedia.org/wiki/Xorshift#xorshift*
        a = a xor (a shr 12)
        a = a xor (a shl 25)
        a = a xor (a shr 27)
        return a * 0x2545_F491_4F6C_DD1DuL
    }

    override fun fillBytes(dest: ByteArray) {
        var offset = 0
        while (offset < dest.size) {
            val v = nextU64()
            val len = minOf(8, dest.size - offset)
            for (i in 0 until len) {
                dest[offset + i] = (v shr (i * 8)).toByte()
            }
            offset += len
        }
    }

    fun tryFillBytes(dest: ByteArray): Result<Unit> = Result.success(fillBytes(dest))
}
