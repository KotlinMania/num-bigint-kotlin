// port-lint: tests tests/macros/mod.rs
package io.github.kotlinmania.numbigint

import io.github.kotlinmania.numbigint.bigint.minus
import io.github.kotlinmania.numbigint.bigint.minusAssign
import io.github.kotlinmania.numbigint.bigint.plus
import io.github.kotlinmania.numbigint.bigint.plusAssign
import io.github.kotlinmania.numbigint.bigint.times
import io.github.kotlinmania.numbigint.bigint.timesAssign
import kotlin.test.assertEquals

internal enum class ScalarOp {
    Add,
    Sub,
    Mul,
    Div,
    Rem,
}

internal enum class ScalarAssignOp {
    AddAssign,
    SubAssign,
    MulAssign,
    DivAssign,
    RemAssign,
}

/**
 * Assert that an op works for all val/ref combinations.
 */
internal inline fun <L, R, E> assertOp(
    left: L,
    right: R,
    expected: E,
    cloneLeft: (L) -> L,
    cloneRight: (R) -> R,
    op: (L, R) -> E,
) {
    assertEquals(expected, op(left, right))
    assertEquals(expected, op(left, cloneRight(right)))
    assertEquals(expected, op(cloneLeft(left), right))
    assertEquals(expected, op(cloneLeft(left), cloneRight(right)))
}

/**
 * Assert that an assign-op works for all val/ref combinations.
 */
internal inline fun <L, R> assertAssignOp(
    left: L,
    right: R,
    expected: L,
    cloneLeft: (L) -> L,
    cloneRight: (R) -> R,
    assignOp: (L, R) -> Unit,
) {
    val left1 = cloneLeft(left)
    assignOp(left1, right)
    assertEquals(expected, left1)

    val left2 = cloneLeft(left)
    assignOp(left2, cloneRight(right))
    assertEquals(expected, left2)
}

/**
 * Assert that an op works for scalar left or right.
 */
internal inline fun <B, S, E> assertScalarOp(
    left: B,
    right: B,
    expected: E,
    cloneBig: (B) -> B,
    toScalar: (B) -> S?,
    leftScalarOp: (S, B) -> E,
    rightScalarOp: (B, S) -> E,
) {
    toScalar(left)?.let { scalarLeft ->
        assertOp(scalarLeft, right, expected, { it }, cloneBig, leftScalarOp)
    }
    toScalar(right)?.let { scalarRight ->
        assertOp(left, scalarRight, expected, cloneBig, { it }, rightScalarOp)
    }
}

internal fun assertUnsignedScalarOp(
    left: BigUint,
    right: BigUint,
    expected: BigUint,
    op: ScalarOp,
) {
    when (op) {
        ScalarOp.Add -> {
            assertScalarOp(left, right, expected, BigUint::clone, BigUint::toUByteOrNull, UByte::plus, BigUint::plus)
            assertScalarOp(left, right, expected, BigUint::clone, BigUint::toUShortOrNull, UShort::plus, BigUint::plus)
            assertScalarOp(left, right, expected, BigUint::clone, BigUint::toUIntOrNull, UInt::plus, BigUint::plus)
            assertScalarOp(left, right, expected, BigUint::clone, BigUint::toULongOrNull, ULong::plus, BigUint::plus)
        }

        ScalarOp.Sub -> {
            assertScalarOp(left, right, expected, BigUint::clone, BigUint::toUByteOrNull, UByte::minus, BigUint::minus)
            assertScalarOp(left, right, expected, BigUint::clone, BigUint::toUShortOrNull, UShort::minus, BigUint::minus)
            assertScalarOp(left, right, expected, BigUint::clone, BigUint::toUIntOrNull, UInt::minus, BigUint::minus)
            assertScalarOp(left, right, expected, BigUint::clone, BigUint::toULongOrNull, ULong::minus, BigUint::minus)
        }

        ScalarOp.Mul -> {
            assertScalarOp(left, right, expected, BigUint::clone, BigUint::toUByteOrNull, UByte::times, BigUint::times)
            assertScalarOp(left, right, expected, BigUint::clone, BigUint::toUShortOrNull, UShort::times, BigUint::times)
            assertScalarOp(left, right, expected, BigUint::clone, BigUint::toUIntOrNull, UInt::times, BigUint::times)
            assertScalarOp(left, right, expected, BigUint::clone, BigUint::toULongOrNull, ULong::times, BigUint::times)
        }

        ScalarOp.Div -> {
            assertScalarOp(left, right, expected, BigUint::clone, BigUint::toUByteOrNull, UByte::div, BigUint::div)
            assertScalarOp(left, right, expected, BigUint::clone, BigUint::toUShortOrNull, UShort::div, BigUint::div)
            assertScalarOp(left, right, expected, BigUint::clone, BigUint::toUIntOrNull, UInt::div, BigUint::div)
            assertScalarOp(left, right, expected, BigUint::clone, BigUint::toULongOrNull, ULong::div, BigUint::div)
        }

        ScalarOp.Rem -> {
            assertScalarOp(left, right, expected, BigUint::clone, BigUint::toUByteOrNull, UByte::rem, BigUint::rem)
            assertScalarOp(left, right, expected, BigUint::clone, BigUint::toUShortOrNull, UShort::rem, BigUint::rem)
            assertScalarOp(left, right, expected, BigUint::clone, BigUint::toUIntOrNull, UInt::rem, BigUint::rem)
            assertScalarOp(left, right, expected, BigUint::clone, BigUint::toULongOrNull, ULong::rem, BigUint::rem)
        }
    }
}

internal fun assertSignedScalarOp(
    left: BigInt,
    right: BigInt,
    expected: BigInt,
    op: ScalarOp,
) {
    when (op) {
        ScalarOp.Add -> {
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toUByteOrNull, UByte::plus, BigInt::plus)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toUShortOrNull, UShort::plus, BigInt::plus)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toUIntOrNull, UInt::plus, BigInt::plus)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toULongOrNull, ULong::plus, BigInt::plus)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toByteOrNull, Byte::plus, BigInt::plus)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toShortOrNull, Short::plus, BigInt::plus)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toIntOrNull, Int::plus, BigInt::plus)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toLongOrNull, Long::plus, BigInt::plus)
        }

        ScalarOp.Sub -> {
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toUByteOrNull, UByte::minus, BigInt::minus)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toUShortOrNull, UShort::minus, BigInt::minus)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toUIntOrNull, UInt::minus, BigInt::minus)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toULongOrNull, ULong::minus, BigInt::minus)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toByteOrNull, Byte::minus, BigInt::minus)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toShortOrNull, Short::minus, BigInt::minus)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toIntOrNull, Int::minus, BigInt::minus)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toLongOrNull, Long::minus, BigInt::minus)
        }

        ScalarOp.Mul -> {
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toUByteOrNull, UByte::times, BigInt::times)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toUShortOrNull, UShort::times, BigInt::times)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toUIntOrNull, UInt::times, BigInt::times)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toULongOrNull, ULong::times, BigInt::times)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toByteOrNull, Byte::times, BigInt::times)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toShortOrNull, Short::times, BigInt::times)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toIntOrNull, Int::times, BigInt::times)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toLongOrNull, Long::times, BigInt::times)
        }

        ScalarOp.Div -> {
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toUByteOrNull, UByte::div, BigInt::div)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toUShortOrNull, UShort::div, BigInt::div)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toUIntOrNull, UInt::div, BigInt::div)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toULongOrNull, ULong::div, BigInt::div)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toByteOrNull, Byte::div, BigInt::div)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toShortOrNull, Short::div, BigInt::div)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toIntOrNull, Int::div, BigInt::div)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toLongOrNull, Long::div, BigInt::div)
        }

        ScalarOp.Rem -> {
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toUByteOrNull, UByte::rem, BigInt::rem)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toUShortOrNull, UShort::rem, BigInt::rem)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toUIntOrNull, UInt::rem, BigInt::rem)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toULongOrNull, ULong::rem, BigInt::rem)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toByteOrNull, Byte::rem, BigInt::rem)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toShortOrNull, Short::rem, BigInt::rem)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toIntOrNull, Int::rem, BigInt::rem)
            assertScalarOp(left, right, expected, BigInt::clone, BigInt::toLongOrNull, Long::rem, BigInt::rem)
        }
    }
}

/**
 * Assert that an op works for scalar right.
 */
internal inline fun <B, S> assertScalarAssignOp(
    left: B,
    right: B,
    expected: B,
    cloneBig: (B) -> B,
    toScalar: (B) -> S?,
    assignOp: (B, S) -> Unit,
) {
    toScalar(right)?.let { scalarRight ->
        val leftClone = cloneBig(left)
        assignOp(leftClone, scalarRight)
        assertEquals(expected, leftClone)
    }
}

internal fun assertUnsignedScalarAssignOp(
    left: BigUint,
    right: BigUint,
    expected: BigUint,
    op: ScalarAssignOp,
) {
    when (op) {
        ScalarAssignOp.AddAssign -> {
            assertScalarAssignOp(left, right, expected, BigUint::clone, BigUint::toUByteOrNull, BigUint::plusAssign)
            assertScalarAssignOp(left, right, expected, BigUint::clone, BigUint::toUShortOrNull, BigUint::plusAssign)
            assertScalarAssignOp(left, right, expected, BigUint::clone, BigUint::toUIntOrNull, BigUint::plusAssign)
            assertScalarAssignOp(left, right, expected, BigUint::clone, BigUint::toULongOrNull, BigUint::plusAssign)
        }

        ScalarAssignOp.SubAssign -> {
            assertScalarAssignOp(left, right, expected, BigUint::clone, BigUint::toUByteOrNull, BigUint::minusAssign)
            assertScalarAssignOp(left, right, expected, BigUint::clone, BigUint::toUShortOrNull, BigUint::minusAssign)
            assertScalarAssignOp(left, right, expected, BigUint::clone, BigUint::toUIntOrNull, BigUint::minusAssign)
            assertScalarAssignOp(left, right, expected, BigUint::clone, BigUint::toULongOrNull, BigUint::minusAssign)
        }

        ScalarAssignOp.MulAssign -> {
            assertScalarAssignOp(left, right, expected, BigUint::clone, BigUint::toUByteOrNull, BigUint::timesAssign)
            assertScalarAssignOp(left, right, expected, BigUint::clone, BigUint::toUShortOrNull, BigUint::timesAssign)
            assertScalarAssignOp(left, right, expected, BigUint::clone, BigUint::toUIntOrNull, BigUint::timesAssign)
            assertScalarAssignOp(left, right, expected, BigUint::clone, BigUint::toULongOrNull, BigUint::timesAssign)
        }

        ScalarAssignOp.DivAssign -> {
            assertScalarAssignOp(left, right, expected, BigUint::clone, BigUint::toUByteOrNull, BigUint::divAssign)
            assertScalarAssignOp(left, right, expected, BigUint::clone, BigUint::toUShortOrNull, BigUint::divAssign)
            assertScalarAssignOp(left, right, expected, BigUint::clone, BigUint::toUIntOrNull, BigUint::divAssign)
            assertScalarAssignOp(left, right, expected, BigUint::clone, BigUint::toULongOrNull, BigUint::divAssign)
        }

        ScalarAssignOp.RemAssign -> {
            assertScalarAssignOp(left, right, expected, BigUint::clone, BigUint::toUByteOrNull, BigUint::remAssign)
            assertScalarAssignOp(left, right, expected, BigUint::clone, BigUint::toUShortOrNull, BigUint::remAssign)
            assertScalarAssignOp(left, right, expected, BigUint::clone, BigUint::toUIntOrNull, BigUint::remAssign)
            assertScalarAssignOp(left, right, expected, BigUint::clone, BigUint::toULongOrNull, BigUint::remAssign)
        }
    }
}

internal fun assertSignedScalarAssignOp(
    left: BigInt,
    right: BigInt,
    expected: BigInt,
    op: ScalarAssignOp,
) {
    when (op) {
        ScalarAssignOp.AddAssign -> {
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toUByteOrNull, BigInt::plusAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toUShortOrNull, BigInt::plusAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toUIntOrNull, BigInt::plusAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toULongOrNull, BigInt::plusAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toByteOrNull, BigInt::plusAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toShortOrNull, BigInt::plusAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toIntOrNull, BigInt::plusAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toLongOrNull, BigInt::plusAssign)
        }

        ScalarAssignOp.SubAssign -> {
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toUByteOrNull, BigInt::minusAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toUShortOrNull, BigInt::minusAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toUIntOrNull, BigInt::minusAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toULongOrNull, BigInt::minusAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toByteOrNull, BigInt::minusAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toShortOrNull, BigInt::minusAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toIntOrNull, BigInt::minusAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toLongOrNull, BigInt::minusAssign)
        }

        ScalarAssignOp.MulAssign -> {
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toUByteOrNull, BigInt::timesAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toUShortOrNull, BigInt::timesAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toUIntOrNull, BigInt::timesAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toULongOrNull, BigInt::timesAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toByteOrNull, BigInt::timesAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toShortOrNull, BigInt::timesAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toIntOrNull, BigInt::timesAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toLongOrNull, BigInt::timesAssign)
        }

        ScalarAssignOp.DivAssign -> {
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toUByteOrNull, BigInt::divAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toUShortOrNull, BigInt::divAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toUIntOrNull, BigInt::divAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toULongOrNull, BigInt::divAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toByteOrNull, BigInt::divAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toShortOrNull, BigInt::divAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toIntOrNull, BigInt::divAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toLongOrNull, BigInt::divAssign)
        }

        ScalarAssignOp.RemAssign -> {
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toUByteOrNull, BigInt::remAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toUShortOrNull, BigInt::remAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toUIntOrNull, BigInt::remAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toULongOrNull, BigInt::remAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toByteOrNull, BigInt::remAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toShortOrNull, BigInt::remAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toIntOrNull, BigInt::remAssign)
            assertScalarAssignOp(left, right, expected, BigInt::clone, BigInt::toLongOrNull, BigInt::remAssign)
        }
    }
}
