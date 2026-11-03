// port-lint: tests tests/bigint_scalar.rs
package io.github.kotlinmania.numbigint

import io.github.kotlinmania.numbigint.bigint.plus
import io.github.kotlinmania.numbigint.bigint.times
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class BigIntScalarTest {
    @Test
    fun testScalarAdd() {
        fun check(x0: BigInt, y0: BigInt, z0: BigInt) {
            val (x, y, z) = Triple(x0.clone(), y0.clone(), z0.clone())
            assertSignedScalarOp(x, y, z, ScalarOp.Add)
            assertSignedScalarAssignOp(x, y, z, ScalarAssignOp.AddAssign)
        }

        for (elm in SUM_TRIPLES) {
            val (aVec, bVec, cVec) = elm
            val a = BigInt.fromSlice(Sign.Plus, aVec)
            val b = BigInt.fromSlice(Sign.Plus, bVec)
            val c = BigInt.fromSlice(Sign.Plus, cVec)
            val (na, nb, nc) = Triple(-a, -b, -c)

            check(a, b, c)
            check(b, a, c)
            check(c, na, b)
            check(c, nb, a)
            check(a, nc, nb)
            check(b, nc, na)
            check(na, nb, nc)
            check(a, na, BigInt.zero())
        }
    }

    @Test
    fun testScalarSub() {
        fun check(x0: BigInt, y0: BigInt, z0: BigInt) {
            val (x, y, z) = Triple(x0.clone(), y0.clone(), z0.clone())
            assertSignedScalarOp(x, y, z, ScalarOp.Sub)
            assertSignedScalarAssignOp(x, y, z, ScalarAssignOp.SubAssign)
        }

        for (elm in SUM_TRIPLES) {
            val (aVec, bVec, cVec) = elm
            val a = BigInt.fromSlice(Sign.Plus, aVec)
            val b = BigInt.fromSlice(Sign.Plus, bVec)
            val c = BigInt.fromSlice(Sign.Plus, cVec)
            val (na, nb, nc) = Triple(-a, -b, -c)

            check(c, a, b)
            check(c, b, a)
            check(nb, a, nc)
            check(na, b, nc)
            check(b, na, c)
            check(a, nb, c)
            check(nc, na, nb)
            check(a, a, BigInt.zero())
        }
    }

    @Test
    fun testScalarMul() {
        fun check(x0: BigInt, y0: BigInt, z0: BigInt) {
            val (x, y, z) = Triple(x0.clone(), y0.clone(), z0.clone())
            assertSignedScalarOp(x, y, z, ScalarOp.Mul)
            assertSignedScalarAssignOp(x, y, z, ScalarAssignOp.MulAssign)
        }

        for (elm in MUL_TRIPLES) {
            val (aVec, bVec, cVec) = elm
            val a = BigInt.fromSlice(Sign.Plus, aVec)
            val b = BigInt.fromSlice(Sign.Plus, bVec)
            val c = BigInt.fromSlice(Sign.Plus, cVec)
            val (na, nb, nc) = Triple(-a, -b, -c)

            check(a, b, c)
            check(b, a, c)
            check(na, nb, c)

            check(na, b, nc)
            check(nb, a, nc)
        }
    }

    @Test
    fun testScalarDivRem() {
        fun checkSub(a0: BigInt, b0: UInt, ansQ0: BigInt, ansR0: BigInt) {
            val (q, r) = Pair(a0 / b0, a0 % b0)
            if (!r.isZero()) {
                assertEquals(a0.sign(), r.sign())
            }
            assertTrue(r.abs() <= BigInt.from(b0))
            assertTrue(a0 == b0 * q + r)
            assertTrue(q == ansQ0)
            assertTrue(r == ansR0)

            val b = BigInt.from(b0)
            val (a, ansQ, ansR) = Triple(a0.clone(), ansQ0.clone(), ansR0.clone())
            assertSignedScalarOp(a, b, ansQ, ScalarOp.Div)
            assertSignedScalarOp(a, b, ansR, ScalarOp.Rem)
            assertSignedScalarAssignOp(a, b, ansQ, ScalarAssignOp.DivAssign)
            assertSignedScalarAssignOp(a, b, ansR, ScalarAssignOp.RemAssign)

            val nb = -b
            assertSignedScalarOp(a, nb, -ansQ.clone(), ScalarOp.Div)
            assertSignedScalarOp(a, nb, ansR, ScalarOp.Rem)
            assertSignedScalarAssignOp(a, nb, -ansQ.clone(), ScalarAssignOp.DivAssign)
            assertSignedScalarAssignOp(a, nb, ansR, ScalarAssignOp.RemAssign)
        }

        fun check(a: BigInt, b: UInt, q: BigInt, r: BigInt) {
            checkSub(a, b, q, r)
            checkSub(-a, b, -q, -r)
        }

        for (elm in MUL_TRIPLES) {
            val (aVec, bVec, cVec) = elm
            val a = BigInt.fromSlice(Sign.Plus, aVec)
            val b = BigInt.fromSlice(Sign.Plus, bVec)
            val c = BigInt.fromSlice(Sign.Plus, cVec)

            if (aVec.size == 1 && aVec[0] != 0u) {
                val aDigit = aVec[0]
                check(c, aDigit, b, BigInt.zero())
            }

            if (bVec.size == 1 && bVec[0] != 0u) {
                val bDigit = bVec[0]
                check(c, bDigit, a, BigInt.zero())
            }
        }

        for (elm in DIV_REM_QUADRUPLES) {
            val aVec = elm[0]
            val bVec = elm[1]
            val cVec = elm[2]
            val dVec = elm[3]
            val a = BigInt.fromSlice(Sign.Plus, aVec)
            val c = BigInt.fromSlice(Sign.Plus, cVec)
            val d = BigInt.fromSlice(Sign.Plus, dVec)

            if (bVec.size == 1 && bVec[0] != 0u) {
                val bDigit = bVec[0]
                check(a, bDigit, c, d)
            }
        }
    }

    @Test
    fun testScalarDivRemZero() {
        assertFailsWith<Throwable> { BigInt.zero() / 0u }
        assertFailsWith<Throwable> { BigInt.zero() % 0u }
        assertFailsWith<Throwable> { BigInt.one() / 0u }
        assertFailsWith<Throwable> { BigInt.one() % 0u }
    }
}
