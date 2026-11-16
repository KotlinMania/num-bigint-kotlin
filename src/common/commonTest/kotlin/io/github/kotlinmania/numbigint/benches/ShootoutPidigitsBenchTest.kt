// port-lint: tests benches/shootout-pidigits.rs
// The Computer Language Benchmarks Game
// http://benchmarksgame.alioth.debian.org/
//
// contributed by the Rust Project Developers

// Copyright (c) 2013-2014 The Rust Project Developers
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
//
// - Redistributions of source code must retain the above copyright
//   notice, this list of conditions and the following disclaimer.
//
// - Redistributions in binary form must reproduce the above copyright
//   notice, this list of conditions and the following disclaimer in
//   the documentation and/or other materials provided with the
//   distribution.
//
// - Neither the name of "The Computer Language Benchmarks Game" nor
//   the name of "The Computer Language Shootout Benchmarks" nor the
//   names of its contributors may be used to endorse or promote
//   products derived from this software without specific prior
//   written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.

package io.github.kotlinmania.numbigint

import io.github.kotlinmania.numbigint.bigint.minus
import io.github.kotlinmania.numbigint.bigint.plus
import io.github.kotlinmania.numbigint.bigint.times
import kotlin.test.Test
import kotlin.test.assertTrue

private class Context(
    var numer: BigInt,
    var accum: BigInt,
    var denom: BigInt,
) {
    companion object {
        fun new(): Context =
            Context(
                numer = BigInt.one(),
                accum = BigInt.zero(),
                denom = BigInt.one(),
            )

        fun fromI32(i: Int): BigInt = BigInt.from(i)
    }

    fun extractDigit(): Int {
        if (numer > accum) {
            return -1
        }
        val (q, r) = (numer * fromI32(3) + accum).divRem(denom)
        if (r + numer >= denom) {
            return -1
        }
        return checkNotNull(q.toIntOrNull())
    }

    fun nextTerm(k: Int) {
        val y2 = fromI32(k * 2 + 1)
        accum = (accum + (numer shl 1)) * y2
        numer = numer * fromI32(k)
        denom = denom * y2
    }

    fun eliminateDigit(d: Int) {
        val dBig = fromI32(d)
        val ten = fromI32(10)
        accum = (accum - denom * dBig) * ten
        numer = numer * ten
    }
}

private fun pidigits(
    n: Int,
    out: Appendable,
) {
    var k = 0
    val context = Context.new()

    for (i in 1..n) {
        var d: Int
        while (true) {
            k += 1
            context.nextTerm(k)
            d = context.extractDigit()
            if (d != -1) {
                break
            }
        }

        out.append(d.toString())
        if (i % 10 == 0) {
            out.append("\t:$i\n")
        }

        context.eliminateDigit(d)
    }

    val m = n % 10
    if (m != 0) {
        repeat(10 - m) {
            out.append(" ")
        }
        out.append("\t:$n\n")
    }
}

private const val DEFAULT_DIGITS: Int = 512

class ShootoutPidigitsBenchTest {
    @Test
    fun main() {
        val out = StringBuilder()
        pidigits(DEFAULT_DIGITS, out)
        assertTrue(out.toString().startsWith("3141592653\t:10\n5897932384\t:20\n"))
    }
}
