// port-lint: source bigint/serde.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.numbigint.bigint

import io.github.kotlinmania.numbigint.BigInt
import io.github.kotlinmania.numbigint.Sign
import io.github.kotlinmania.numbigint.biguint.U32Visitor
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.PairSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
object SignSerializer : KSerializer<Sign> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("io.github.kotlinmania.numbigint.Sign", PrimitiveKind.BYTE)

    override fun serialize(encoder: Encoder, value: Sign) {
        serialize(value, encoder)
    }

    override fun deserialize(decoder: Decoder): Sign {
        return deserializeSign(decoder)
    }
}

@HiddenFromObjC
fun serialize(value: Sign, serializer: Encoder) {
    val sign = when (value) {
        Sign.Minus -> -1
        Sign.NoSign -> 0
        Sign.Plus -> 1
    }
    serializer.encodeByte(sign.toByte())
}

@HiddenFromObjC
fun deserializeSign(deserializer: Decoder): Sign {
    return when (val sign = deserializer.decodeByte().toInt()) {
        -1 -> Sign.Minus
        0 -> Sign.NoSign
        1 -> Sign.Plus
        else -> throw SerializationException("invalid value $sign, expected a sign of -1, 0, or 1")
    }
}

@HiddenFromObjC
object BigIntSerializer : KSerializer<BigInt> {
    override val descriptor: SerialDescriptor
        get() = dataSerializer.descriptor

    override fun serialize(encoder: Encoder, value: BigInt) {
        serialize(value, encoder)
    }

    override fun deserialize(decoder: Decoder): BigInt {
        return deserializeBigint(decoder)
    }
}

private val dataSerializer = PairSerializer(SignSerializer, U32Visitor)

@HiddenFromObjC
fun serialize(value: BigInt, serializer: Encoder) {
    dataSerializer.serialize(serializer, Pair(value.sign(), value.data))
}

@HiddenFromObjC
fun deserializeBigint(deserializer: Decoder): BigInt {
    val (sign, data) = dataSerializer.deserialize(deserializer)
    return BigInt.fromBiguint(sign, data)
}
