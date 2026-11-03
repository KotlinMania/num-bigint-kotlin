// port-lint: source bigrand.rs
package io.github.kotlinmania.numbigint

import io.github.kotlinmania.numbigint.bigint.minus
import io.github.kotlinmania.numbigint.bigint.plus
import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import io.github.kotlinmania.rand.distr.SampleUniform
import io.github.kotlinmania.rand.distr.StandardUniform
import io.github.kotlinmania.rand.distr.Uniform
import io.github.kotlinmania.rand.distr.UniformSampler
import io.github.kotlinmania.rand.fill

/**
 * A trait for sampling random big integers.
 *
 * The `rand` feature must be enabled to use this. See crate-level documentation for details.
 */
public interface RandBigInt {
    /**
     * Generate a random [BigUint] of the given bit size.
     */
    public fun genBiguint(bitSize: ULong): BigUint

    /**
     * Generate a random [BigInt] of the given bit size.
     */
    public fun genBigint(bitSize: ULong): BigInt

    /**
     * Generate a random [BigUint] less than the given bound. Fails
     * when the bound is zero.
     */
    public fun genBiguintBelow(bound: BigUint): BigUint

    /**
     * Generate a random [BigUint] within the given range. The lower
     * bound is inclusive; the upper bound is exclusive. Fails when
     * the upper bound is not greater than the lower bound.
     */
    public fun genBiguintRange(lbound: BigUint, ubound: BigUint): BigUint

    /**
     * Generate a random [BigInt] within the given range. The lower
     * bound is inclusive; the upper bound is exclusive. Fails when
     * the upper bound is not greater than the lower bound.
     */
    public fun genBigintRange(lbound: BigInt, ubound: BigInt): BigInt
}

private fun genBits(rng: Rng, data: UIntArray, rem: ULong) {
    // `fill` is faster than many `gen::<u32>` calls
    rng.fill(data)
    if (rem > 0uL) {
        val last = data.size - 1
        data[last] = data[last] shr (32 - rem.toInt())
    }
}

/**
 * Generate a random [BigUint] of the given bit size.
 */
public fun Rng.genBiguint(bitSize: ULong): BigUint {
    val digits = bitSize / 32uL
    val rem = bitSize % 32uL
    val totalDigits = digits + if (rem > 0uL) 1uL else 0uL
    check(totalDigits <= Int.MAX_VALUE.toULong()) { "capacity overflow" }
    val len = totalDigits.toInt()
    val data = UIntArray(len)
    genBits(this, data, rem)
    return biguintFromVec(data.toMutableList())
}

/**
 * Generate a random [BigInt] of the given bit size.
 */
public fun Rng.genBigint(bitSize: ULong): BigInt {
    while (true) {
        // Generate a random BigUint...
        val biguint = this.genBiguint(bitSize)
        // ...and then randomly assign it a Sign...
        val sign =
            if (biguint.isZero()) {
                // ...except that if the BigUint is zero, we need to try
                // again with probability 0.5. This is because otherwise,
                // the probability of generating a zero BigInt would be
                // double that of any other number.
                if (StandardUniform.sampleBoolean(this)) {
                    continue
                } else {
                    Sign.NoSign
                }
            } else if (StandardUniform.sampleBoolean(this)) {
                Sign.Plus
            } else {
                Sign.Minus
            }
        return BigInt.fromBiguint(sign, biguint)
    }
}

/**
 * Generate a random [BigUint] less than the given bound. Fails
 * when the bound is zero.
 */
public fun Rng.genBiguintBelow(bound: BigUint): BigUint {
    require(!bound.isZero()) { "cannot sample below zero" }
    val bits = bound.bits()
    while (true) {
        val n = this.genBiguint(bits)
        if (n < bound) {
            return n
        }
    }
}

/**
 * Generate a random [BigUint] within the given range. The lower
 * bound is inclusive; the upper bound is exclusive. Fails when
 * the upper bound is not greater than the lower bound.
 */
public fun Rng.genBiguintRange(lbound: BigUint, ubound: BigUint): BigUint {
    require(lbound < ubound) { "cannot sample empty range" }
    return if (lbound.isZero()) {
        this.genBiguintBelow(ubound)
    } else {
        lbound + this.genBiguintBelow(ubound - lbound)
    }
}

/**
 * Generate a random [BigInt] within the given range. The lower
 * bound is inclusive; the upper bound is exclusive. Fails when
 * the upper bound is not greater than the lower bound.
 */
public fun Rng.genBigintRange(lbound: BigInt, ubound: BigInt): BigInt {
    require(lbound < ubound) { "cannot sample empty range" }
    return if (lbound.isZero()) {
        BigInt.from(this.genBiguintBelow(ubound.magnitude()))
    } else if (ubound.isZero()) {
        lbound + BigInt.from(this.genBiguintBelow(lbound.magnitude()))
    } else {
        val delta = ubound - lbound
        lbound + BigInt.from(this.genBiguintBelow(delta.magnitude()))
    }
}

/**
 * The back-end implementing rand's [UniformSampler] for [BigUint].
 */
public class UniformBigUint(
    private val base: BigUint,
    private val len: BigUint,
) : UniformSampler<BigUint> {
    override fun sample(rng: Rng): BigUint = base + rng.genBiguintBelow(len)

    public companion object : SampleUniform<BigUint> {
        override fun sampler(): UniformSampler<BigUint> = new(BigUint.ZERO, BigUint.from(1u))

        public fun new(lowB: BigUint, highB: BigUint): UniformBigUint {
            val low = lowB
            val high = highB
            require(low < high) { "cannot sample empty range" }
            return UniformBigUint(
                base = low.clone(),
                len = high - low,
            )
        }

        public fun newInclusive(lowB: BigUint, highB: BigUint): UniformBigUint {
            val low = lowB
            val high = highB
            require(low <= high) { "cannot sample empty range" }
            return new(low, high + 1u)
        }

        public fun sampleSingle(low: BigUint, high: BigUint, rng: Rng): BigUint =
            rng.genBiguintRange(low, high)
    }
}

/**
 * Creates a [Uniform] distribution for [BigUint] in `[low, high)`.
 */
public fun Uniform.Companion.new(low: BigUint, high: BigUint): Uniform<BigUint> =
    Uniform(UniformBigUint.new(low, high))

/**
 * Creates a [Uniform] distribution for [BigUint] in `[low, high]`.
 */
public fun Uniform.Companion.newInclusive(low: BigUint, high: BigUint): Uniform<BigUint> =
    Uniform(UniformBigUint.newInclusive(low, high))

/**
 * The back-end implementing rand's [UniformSampler] for [BigInt].
 */
public class UniformBigInt(
    private val base: BigInt,
    private val len: BigUint,
) : UniformSampler<BigInt> {
    override fun sample(rng: Rng): BigInt = base + BigInt.from(rng.genBiguintBelow(len))

    public companion object : SampleUniform<BigInt> {
        override fun sampler(): UniformSampler<BigInt> = new(BigInt.ZERO, BigInt.from(1))

        public fun new(lowB: BigInt, highB: BigInt): UniformBigInt {
            val low = lowB
            val high = highB
            require(low < high) { "cannot sample empty range" }
            return UniformBigInt(
                base = low.clone(),
                len = (high - low).intoParts().second,
            )
        }

        public fun newInclusive(lowB: BigInt, highB: BigInt): UniformBigInt {
            val low = lowB
            val high = highB
            require(low <= high) { "cannot sample empty range" }
            return new(low, high + 1u)
        }

        public fun sampleSingle(low: BigInt, high: BigInt, rng: Rng): BigInt =
            rng.genBigintRange(low, high)
    }
}

/**
 * Creates a [Uniform] distribution for [BigInt] in `[low, high)`.
 */
public fun Uniform.Companion.new(low: BigInt, high: BigInt): Uniform<BigInt> =
    Uniform(UniformBigInt.new(low, high))

/**
 * Creates a [Uniform] distribution for [BigInt] in `[low, high]`.
 */
public fun Uniform.Companion.newInclusive(low: BigInt, high: BigInt): Uniform<BigInt> =
    Uniform(UniformBigInt.newInclusive(low, high))

/**
 * A random distribution for [BigUint] and [BigInt] values of a particular bit size.
 *
 * The `rand` feature must be enabled to use this. See crate-level documentation for details.
 */
public class RandomBits(
    public val bits: ULong,
) : Distribution<BigUint> {
    override fun sample(rng: Rng): BigUint = rng.genBiguint(this.bits)

    public fun toBigIntDistribution(): Distribution<BigInt> = BigIntRandomBits(this.bits)

    private class BigIntRandomBits(
        private val bits: ULong,
    ) : Distribution<BigInt> {
        override fun sample(rng: Rng): BigInt = rng.genBigint(this.bits)
    }

    public companion object {
        public fun new(bits: ULong): RandomBits = RandomBits(bits)
    }
}
