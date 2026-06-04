// port-lint: source biguint/serde.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.numbigint.biguint

import io.github.kotlinmania.numbigint.BigDigit
import io.github.kotlinmania.numbigint.BigUint
import io.github.kotlinmania.numbigint.biguintFromVec
import kotlin.math.min
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.native.HiddenFromObjC

private const val MAX_PREALLOC_BYTES: Int = 1024 * 1024

internal typealias Value = BigUint

fun cautious(hint: Int?): Int {
    return min(hint ?: 0, MAX_PREALLOC_BYTES / UInt.SIZE_BYTES)
}

@HiddenFromObjC
object U32Visitor : KSerializer<BigUint> {
    override val descriptor: SerialDescriptor
        get() = dataSerializer.descriptor

    override fun serialize(encoder: Encoder, value: BigUint) {
        serialize(value, encoder)
    }

    override fun deserialize(decoder: Decoder): BigUint {
        return deserialize(decoder)
    }

    fun expecting(): String {
        return "a sequence of unsigned 32-bit numbers"
    }
}

private val dataSerializer = ListSerializer(UInt.serializer())

@HiddenFromObjC
fun serialize(value: BigUint, serializer: Encoder) {
    dataSerializer.serialize(serializer, value.data.toList())
}

@HiddenFromObjC
fun deserialize(deserializer: Decoder): BigUint {
    val digits = dataSerializer.deserialize(deserializer)
    val data = ArrayList<BigDigit>(cautious(digits.size))
    data.addAll(digits)
    return biguintFromVec(data)
}
