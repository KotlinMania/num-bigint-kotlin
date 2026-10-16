// port-lint: source bigint/serde.rs

package io.github.kotlinmania.numbigint.bigint

import io.github.kotlinmania.numbigint.BigInt
import io.github.kotlinmania.numbigint.BigUint
import io.github.kotlinmania.numbigint.Sign
import io.github.kotlinmania.serde.SerdeError
import io.github.kotlinmania.serde.SerdeException
import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serde.serdeCatching
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.de.Expected
import io.github.kotlinmania.serdecore.de.I8Deserialize
import io.github.kotlinmania.serdecore.de.Unexpected
import io.github.kotlinmania.serdecore.de.pairDeserialize
import io.github.kotlinmania.serdecore.ser.Serializer
import io.github.kotlinmania.serdecore.ser.serialize

fun <Ok> serialize(
    self: Sign,
    serializer: Serializer<Ok>,
): SerdeResult<Ok> =
    // Note: do not change the serialization format, or it may break
    // forward and backward compatibility of serialized data!
    when (self) {
        Sign.Minus -> (-1).toByte().serialize(serializer)
        Sign.NoSign -> 0.toByte().serialize(serializer)
        Sign.Plus -> 1.toByte().serialize(serializer)
    }

fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<Sign> =
    serdeCatching {
        val sign = I8Deserialize.deserialize(deserializer).getOrThrow().toInt()
        when (sign) {
            -1 -> {
                Sign.Minus
            }

            0 -> {
                Sign.NoSign
            }

            1 -> {
                Sign.Plus
            }

            else -> {
                throw SerdeException(
                    SerdeError.invalidValue(
                        Unexpected.Signed(sign.toLong()),
                        Expected { "a sign of -1, 0, or 1" },
                    ),
                )
            }
        }
    }

fun <Ok> serialize(
    self: BigInt,
    serializer: Serializer<Ok>,
): SerdeResult<Ok> =
    // Note: do not change the serialization format, or it may break
    // forward and backward compatibility of serialized data!
    Pair(self.sign(), self.data).serialize(serializer)

fun <D : Deserializer> deserialize(
    deserializer: D,
    companion: BigInt.Companion = BigInt,
): SerdeResult<BigInt> =
    serdeCatching {
        val (sign, data) =
            pairDeserialize(Sign, BigUint)
                .deserialize(deserializer)
                .getOrThrow()
        companion.fromBiguint(sign, data)
    }
