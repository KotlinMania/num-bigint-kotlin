// port-lint: source biguint/serde.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.numbigint.biguint

import io.github.kotlinmania.numbigint.BigDigit
import io.github.kotlinmania.numbigint.BigUint
import io.github.kotlinmania.numbigint.biguintFromVec
import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serde.serdeCatching
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.de.SeqAccess
import io.github.kotlinmania.serdecore.de.U32Deserialize
import io.github.kotlinmania.serdecore.de.Visitor
import io.github.kotlinmania.serdecore.ser.Serialize
import io.github.kotlinmania.serdecore.ser.Serializer
import io.github.kotlinmania.serdecore.ser.serialize
import kotlin.math.min
import kotlin.native.HiddenFromObjC

private const val MAX_PREALLOC_BYTES: Int = 1024 * 1024

internal typealias Value = BigUint

fun cautious(hint: Int?): Int = min(hint ?: 0, MAX_PREALLOC_BYTES / UInt.SIZE_BYTES)

@HiddenFromObjC
fun <Ok> serialize(
    self: BigUint,
    serializer: Serializer<Ok>,
): SerdeResult<Ok> {
    // Note: do not change the serialization format, or it may break forward
    // and backward compatibility of serialized data! If we ever change the
    // internal representation, we should still serialize in base-`UInt`.
    val data: List<UInt> = self.data
    return serializer.collectSeq(
        data.map { digit ->
            object : Serialize {
                override fun <ElementOk> serialize(serializer: Serializer<ElementOk>): SerdeResult<ElementOk> =
                    digit.serialize(serializer)
            }
        },
    )
}

@HiddenFromObjC
fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<BigUint> =
    deserializer.deserializeSeq(U32Visitor)

@HiddenFromObjC
object U32Visitor : Visitor<BigUint> {
    override fun expecting(): String = "a sequence of unsigned 32-bit numbers"

    override fun <A : SeqAccess> visitSeq(access: A): SerdeResult<BigUint> {
        val len = cautious(access.sizeHint())
        val data = ArrayList<BigDigit>(len)
        return serdeCatching {
            while (true) {
                val value = access.nextElement(U32Deserialize).getOrThrow() ?: break
                data.add(value)
            }
            biguintFromVec(data)
        }
    }
}
