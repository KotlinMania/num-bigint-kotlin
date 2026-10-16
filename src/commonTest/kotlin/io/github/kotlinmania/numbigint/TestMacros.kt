// port-lint: tests tests/macros/mod.rs
package io.github.kotlinmania.numbigint

import kotlin.test.assertEquals

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
