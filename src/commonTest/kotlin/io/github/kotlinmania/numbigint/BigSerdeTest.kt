// port-lint: tests ci/big_serde/src/lib.rs
package io.github.kotlinmania.numbigint

import io.github.kotlinmania.numbigint.bigint.productBigInt
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.ser.Serialize
import io.github.kotlinmania.serdetest.Token
import io.github.kotlinmania.serdetest.assertDeTokens
import io.github.kotlinmania.serdetest.assertSerTokens
import io.github.kotlinmania.serdetest.assertTokens
import kotlin.test.Test
import kotlin.test.assertFails

/**
 * Test serialization and deserialization of [BigUint] and [BigInt].
 *
 * The serialized formats should not change, even if we change our
 * internal representation, because we want to preserve forward and
 * backward compatibility of serialized data!
 */
class BigSerdeTest {
    @Test
    fun biguintZero() {
        val tokens = listOf(Token.Seq(len = 0), Token.SeqEnd)
        assertTokens(BigUint.zero(), BigUint, tokens)
    }

    @Test
    fun bigintZero() {
        val tokens =
            listOf(
                Token.Tuple(len = 2),
                Token.I8(0),
                Token.Seq(len = 0),
                Token.SeqEnd,
                Token.TupleEnd,
            )
        assertTokens(BigInt.zero(), BigInt, tokens)
    }

    @Test
    fun biguintOne() {
        val tokens = listOf(Token.Seq(len = 1), Token.U32(1u), Token.SeqEnd)
        assertTokens(BigUint.one(), BigUint, tokens)
    }

    @Test
    fun bigintOne() {
        val tokens =
            listOf(
                Token.Tuple(len = 2),
                Token.I8(1),
                Token.Seq(len = 1),
                Token.U32(1u),
                Token.SeqEnd,
                Token.TupleEnd,
            )
        assertTokens(BigInt.one(), BigInt, tokens)
    }

    @Test
    fun bigintNegone() {
        val tokens =
            listOf(
                Token.Tuple(len = 2),
                Token.I8(-1),
                Token.Seq(len = 1),
                Token.U32(1u),
                Token.SeqEnd,
                Token.TupleEnd,
            )
        assertTokens(-BigInt.one(), BigInt, tokens)
    }

    @Test
    fun biguintFactorial100() {
        val n: BigUint = (1u..100u).map { BigUint.fromUInt(it) }.productBigUint()

        val tokens = ArrayList<Token>()
        tokens.add(Token.Seq(len = FACTORIAL_100.size))
        tokens.addAll(FACTORIAL_100.map { Token.U32(it) })
        tokens.add(Token.SeqEnd)

        assertTokens(n, BigUint, tokens)
    }

    @Test
    fun bigintFactorial100() {
        val n: BigInt = (1..100).map { BigInt.from(it) }.productBigInt()

        val tokens = ArrayList<Token>()
        tokens.add(Token.Tuple(len = 2))
        tokens.add(Token.I8(1))
        tokens.add(Token.Seq(len = FACTORIAL_100.size))
        tokens.addAll(FACTORIAL_100.map { Token.U32(it) })
        tokens.add(Token.SeqEnd)
        tokens.add(Token.TupleEnd)

        assertTokens(n, BigInt, tokens)
    }

    @Test
    fun bigDigits() {
        // Try a few different lengths for u32/u64 digit coverage
        for (len in 1u..9u) {
            val digits = (1u..len).toList()
            val n = BigUint.new(digits)

            val tokens = ArrayList<Token>()
            tokens.add(Token.Seq(len = len.toInt()))
            tokens.addAll(digits.map { Token.U32(it) })
            tokens.add(Token.SeqEnd)

            assertTokens(n, BigUint, tokens)

            val signed = BigInt.from(n)
            tokens.add(0, Token.Tuple(len = 2))
            tokens.add(1, Token.I8(1))
            tokens.add(Token.TupleEnd)
            assertTokens(signed, BigInt, tokens)

            tokens[1] = Token.I8(-1)
            assertTokens(-signed, BigInt, tokens)
        }
    }

    @Test
    fun badSizeHintInt() {
        badSizeHint(
            BigInt.one(),
            BigInt,
            listOf(Token.Tuple(len = 2), Token.I8(1)),
            listOf(Token.TupleEnd),
        )
    }

    @Test
    fun badSizeHintUint() {
        badSizeHint(
            BigUint.one(),
            BigUint,
            emptyList(),
            emptyList(),
        )
    }

    private fun <T : Serialize> badSizeHint(
        one: T,
        deserialize: Deserialize<T>,
        prefix: List<Token>,
        suffix: List<Token>,
    ) {
        val tokens = ArrayList<Token>()
        tokens.addAll(prefix)
        tokens.addAll(listOf(Token.Seq(len = 1), Token.U32(1u), Token.SeqEnd))
        tokens.addAll(suffix)

        assertTokens(one, deserialize, tokens)

        tokens[prefix.size] = Token.Seq(len = Int.MAX_VALUE)

        assertFails { assertSerTokens(one, tokens) }
        assertDeTokens(one, deserialize, tokens)
    }

    companion object {
        // Generated independently from python `hex(factorial(100))`
        private val FACTORIAL_100: List<UInt> =
            listOf(
                0x00000000u,
                0x00000000u,
                0x00000000u,
                0x2735c61au,
                0xee8b02eau,
                0xb3b72ed2u,
                0x9420c6ecu,
                0x45570ccau,
                0xdf103917u,
                0x943a321cu,
                0xeb21b5b2u,
                0x66ef9a70u,
                0xa40d16e9u,
                0x28d54bbdu,
                0xdc240695u,
                0x964ec395u,
                0x1b30u,
            )
    }
}
