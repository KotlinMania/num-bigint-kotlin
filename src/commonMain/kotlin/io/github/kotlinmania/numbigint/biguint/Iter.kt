// port-lint: source biguint/iter.rs
package io.github.kotlinmania.numbigint

/**
 * An iterator of `UInt` digits representation of a `BigUint` or `BigInt`,
 * ordered least significant digit first.
 */
class U32Digits internal constructor(
    private val data: List<BigDigit>,
) : Iterator<UInt> {
    private var front: Int = 0
    private var back: Int = data.size

    fun len(): Int = back - front

    override fun hasNext(): Boolean = front < back

    fun nextOrNull(): UInt? {
        if (!hasNext()) {
            return null
        }
        return data[front++]
    }

    override fun next(): UInt {
        return nextOrNull() ?: throw NoSuchElementException()
    }

    fun nth(n: Int): UInt? {
        require(n >= 0)
        val index = front + n
        if (index >= back) {
            front = back
            return null
        }
        front = index + 1
        return data[index]
    }

    fun lastOrNull(): UInt? {
        if (!hasNext()) {
            return null
        }
        val last = data[back - 1]
        front = back
        return last
    }

    fun count(): Int {
        val len = len()
        front = back
        return len
    }

    fun nextBack(): UInt? {
        if (!hasNext()) {
            return null
        }
        back -= 1
        return data[back]
    }
}

/**
 * An iterator of `ULong` digits representation of a `BigUint` or `BigInt`,
 * ordered least significant digit first.
 */
class U64Digits internal constructor(
    private val data: List<BigDigit>,
) : Iterator<ULong> {
    private var frontChunk: Int = 0
    private var backChunk: Int = (data.size + 1) / 2

    fun len(): Int = backChunk - frontChunk

    override fun hasNext(): Boolean = frontChunk < backChunk

    fun nextOrNull(): ULong? {
        if (!hasNext()) {
            return null
        }
        val offset = frontChunk * 2
        frontChunk += 1
        return u32ChunkToU64(data.subList(offset, minOf(offset + 2, data.size)))
    }

    override fun next(): ULong {
        return nextOrNull() ?: throw NoSuchElementException()
    }

    fun lastOrNull(): ULong? {
        if (!hasNext()) {
            return null
        }
        val offset = (backChunk - 1) * 2
        frontChunk = backChunk
        return u32ChunkToU64(data.subList(offset, minOf(offset + 2, data.size)))
    }

    fun count(): Int {
        val len = len()
        frontChunk = backChunk
        return len
    }

    fun nextBack(): ULong? {
        if (!hasNext()) {
            return null
        }
        backChunk -= 1
        val offset = backChunk * 2
        return u32ChunkToU64(data.subList(offset, minOf(offset + 2, data.size)))
    }
}
