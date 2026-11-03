// port-lint: tests tests/consts/mod.rs
package io.github.kotlinmania.numbigint

internal val N1: UInt = (-1).toUInt()
internal val N2: UInt = (-2).toUInt()

internal val SUM_TRIPLES: List<Triple<List<UInt>, List<UInt>, List<UInt>>> =
    listOf(
        Triple(emptyList(), emptyList(), emptyList()),
        Triple(emptyList(), listOf(1u), listOf(1u)),
        Triple(listOf(1u), listOf(1u), listOf(2u)),
        Triple(listOf(1u), listOf(1u, 1u), listOf(2u, 1u)),
        Triple(listOf(1u), listOf(N1), listOf(0u, 1u)),
        Triple(listOf(1u), listOf(N1, N1), listOf(0u, 0u, 1u)),
        Triple(listOf(N1, N1), listOf(N1, N1), listOf(N2, N1, 1u)),
        Triple(listOf(1u, 1u, 1u), listOf(N1, N1), listOf(0u, 1u, 2u)),
        Triple(listOf(2u, 2u, 1u), listOf(N1, N2), listOf(1u, 1u, 2u)),
        Triple(listOf(1u, 2u, 2u, 1u), listOf(N1, N2), listOf(0u, 1u, 3u, 1u)),
    )

internal val M: UInt = UInt.MAX_VALUE

internal val MUL_TRIPLES: List<Triple<List<UInt>, List<UInt>, List<UInt>>> =
    listOf(
        Triple(emptyList(), emptyList(), emptyList()),
        Triple(emptyList(), listOf(1u), emptyList()),
        Triple(listOf(2u), emptyList(), emptyList()),
        Triple(listOf(1u), listOf(1u), listOf(1u)),
        Triple(listOf(2u), listOf(3u), listOf(6u)),
        Triple(listOf(1u), listOf(1u, 1u, 1u), listOf(1u, 1u, 1u)),
        Triple(listOf(1u, 2u, 3u), listOf(3u), listOf(3u, 6u, 9u)),
        Triple(listOf(1u, 1u, 1u), listOf(N1), listOf(N1, N1, N1)),
        Triple(listOf(1u, 2u, 3u), listOf(N1), listOf(N1, N2, N2, 2u)),
        Triple(listOf(1u, 2u, 3u, 4u), listOf(N1), listOf(N1, N2, N2, N2, 3u)),
        Triple(listOf(N1), listOf(N1), listOf(1u, N2)),
        Triple(listOf(N1, N1), listOf(N1), listOf(1u, N1, N2)),
        Triple(listOf(N1, N1, N1), listOf(N1), listOf(1u, N1, N1, N2)),
        Triple(listOf(N1, N1, N1, N1), listOf(N1), listOf(1u, N1, N1, N1, N2)),
        Triple(listOf(M / 2u + 1u), listOf(2u), listOf(0u, 1u)),
        Triple(listOf(0u, M / 2u + 1u), listOf(2u), listOf(0u, 0u, 1u)),
        Triple(listOf(1u, 2u), listOf(1u, 2u, 3u), listOf(1u, 4u, 7u, 6u)),
        Triple(listOf(N1, N1), listOf(N1, N1, N1), listOf(1u, 0u, N1, N2, N1)),
        Triple(listOf(N1, N1, N1), listOf(N1, N1, N1, N1), listOf(1u, 0u, 0u, N1, N2, N1, N1)),
        Triple(listOf(0u, 0u, 1u), listOf(1u, 2u, 3u), listOf(0u, 0u, 1u, 2u, 3u)),
        Triple(listOf(0u, 0u, 1u), listOf(0u, 0u, 0u, 1u), listOf(0u, 0u, 0u, 0u, 0u, 1u)),
    )

internal val DIV_REM_QUADRUPLES: List<List<List<UInt>>> =
    listOf(
        listOf(listOf(1u), listOf(2u), emptyList(), listOf(1u)),
        listOf(listOf(3u), listOf(2u), listOf(1u), listOf(1u)),
        listOf(listOf(1u, 1u), listOf(2u), listOf(M / 2u + 1u), listOf(1u)),
        listOf(listOf(1u, 1u, 1u), listOf(2u), listOf(M / 2u + 1u, M / 2u + 1u), listOf(1u)),
        listOf(listOf(0u, 1u), listOf(N1), listOf(1u), listOf(1u)),
        listOf(listOf(N1, N1), listOf(N2), listOf(2u, 1u), listOf(3u)),
    )
