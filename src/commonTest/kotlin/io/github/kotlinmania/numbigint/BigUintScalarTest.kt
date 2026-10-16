// port-lint: tests tests/biguint_scalar.rs
package io.github.kotlinmania.numbigint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BigUintScalarTest {
    @Test
    fun testScalarAdd() {
        fun check(x0: BigUint, y0: BigUint, z0: BigUint) {
            val (x, y, z) = Triple(x0.clone(), y0.clone(), z0.clone())
            assertUnsignedScalarOp(x, y, z, ScalarOp.Add)
            assertUnsignedScalarAssignOp(x, y, z, ScalarAssignOp.AddAssign)
        }

        for (elm in SUM_TRIPLES) {
            val (aVec, bVec, cVec) = elm
            val a = BigUint.fromSlice(aVec)
            val b = BigUint.fromSlice(bVec)
            val c = BigUint.fromSlice(cVec)

            check(a, b, c)
            check(b, a, c)
        }
    }

    @Test
    fun testScalarSub() {
        fun check(x0: BigUint, y0: BigUint, z0: BigUint) {
            val (x, y, z) = Triple(x0.clone(), y0.clone(), z0.clone())
            assertUnsignedScalarOp(x, y, z, ScalarOp.Sub)
            assertUnsignedScalarAssignOp(x, y, z, ScalarAssignOp.SubAssign)
        }

        for (elm in SUM_TRIPLES) {
            val (aVec, bVec, cVec) = elm
            val a = BigUint.fromSlice(aVec)
            val b = BigUint.fromSlice(bVec)
            val c = BigUint.fromSlice(cVec)

            check(c, a, b)
            check(c, b, a)
        }
    }

    @Test
    fun testScalarMul() {
        fun check(x0: BigUint, y0: BigUint, z0: BigUint) {
            val (x, y, z) = Triple(x0.clone(), y0.clone(), z0.clone())
            assertUnsignedScalarOp(x, y, z, ScalarOp.Mul)
            assertUnsignedScalarAssignOp(x, y, z, ScalarAssignOp.MulAssign)
        }

        for (elm in MUL_TRIPLES) {
            val (aVec, bVec, cVec) = elm
            val a = BigUint.fromSlice(aVec)
            val b = BigUint.fromSlice(bVec)
            val c = BigUint.fromSlice(cVec)

            check(a, b, c)
            check(b, a, c)
        }
    }

    @Test
    fun testScalarRemNoncommutative() {
        assertEquals(BigUint.from(5u.toUByte()), 5u.toUByte() % BigUint.from(7u.toUByte()))
        assertEquals(BigUint.from(5u.toUByte()), BigUint.from(5u.toUByte()) % 7u.toUByte())
    }

    @Test
    fun testScalarDivRem() {
        fun check(x0: BigUint, y0: BigUint, z0: BigUint, r0: BigUint) {
            val (x, y) = Pair(x0.clone(), y0.clone())
            val (z, r) = Pair(z0.clone(), r0.clone())
            assertUnsignedScalarOp(x, y, z, ScalarOp.Div)
            assertUnsignedScalarOp(x, y, r, ScalarOp.Rem)
            assertUnsignedScalarAssignOp(x, y, z, ScalarAssignOp.DivAssign)
            assertUnsignedScalarAssignOp(x, y, r, ScalarAssignOp.RemAssign)
        }

        for (elm in MUL_TRIPLES) {
            val (aVec, bVec, cVec) = elm
            val a = BigUint.fromSlice(aVec)
            val b = BigUint.fromSlice(bVec)
            val c = BigUint.fromSlice(cVec)

            if (!a.isZero()) {
                check(c, a, b, BigUint.zero())
            }

            if (!b.isZero()) {
                check(c, b, a, BigUint.zero())
            }
        }

        for (elm in DIV_REM_QUADRUPLES) {
            val aVec = elm[0]
            val bVec = elm[1]
            val cVec = elm[2]
            val dVec = elm[3]
            val a = BigUint.fromSlice(aVec)
            val b = BigUint.fromSlice(bVec)
            val c = BigUint.fromSlice(cVec)
            val d = BigUint.fromSlice(dVec)

            if (!b.isZero()) {
                check(a, b, c, d)
                assertUnsignedScalarOp(a, b, c, ScalarOp.Div)
                assertUnsignedScalarOp(a, b, d, ScalarOp.Rem)
                assertUnsignedScalarAssignOp(a, b, c, ScalarAssignOp.DivAssign)
                assertUnsignedScalarAssignOp(a, b, d, ScalarAssignOp.RemAssign)
            }
        }
    }

    @Test
    fun testScalarDivRemZero() {
        assertFailsWith<Throwable> { BigUint.zero() / 0u }
        assertFailsWith<Throwable> { BigUint.zero() % 0u }
        assertFailsWith<Throwable> { BigUint.one() / 0u }
        assertFailsWith<Throwable> { BigUint.one() % 0u }
    }
}
