// port-lint: tests benches/factorial.rs
package io.github.kotlinmania.numbigint

import io.github.kotlinmania.numbigint.benches.rng.Bencher
import kotlin.test.Test

class FactorialBenchTest {
    @Test
    fun factorialMulBiguint() {
        val b = Bencher()
        b.iter {
            (1u until 1000u)
                .map { BigUint.from(it) }
                .fold(BigUint.one()) { acc, x -> acc * x }
        }
    }

    @Test
    fun factorialMulU32() {
        val b = Bencher()
        b.iter {
            (1u until 1000u).fold(BigUint.one()) { acc, x -> acc * x }
        }
    }

    // The division test is inspired by this blog comparison:
    // <https://tiehuis.github.io/big-integers-in-zig#division-test-single-limb>

    @Test
    fun factorialDivBiguint() {
        val b = Bencher()
        val n: BigUint = (1u until 1000u).fold(BigUint.one()) { acc, x -> acc * x }
        b.iter {
            (1u until 1000u)
                .reversed()
                .map { BigUint.from(it) }
                .fold(n.clone()) { acc, x -> acc / x }
        }
    }

    @Test
    fun factorialDivU32() {
        val b = Bencher()
        val n: BigUint = (1u until 1000u).fold(BigUint.one()) { acc, x -> acc * x }
        b.iter {
            (1u until 1000u)
                .reversed()
                .fold(n.clone()) { acc, x -> acc / x }
        }
    }
}
