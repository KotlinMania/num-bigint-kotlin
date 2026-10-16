package io.github.kotlinmania.numbigint

class BigIntParseResult internal constructor(
    val value: BigInt?,
    val error: ParseBigIntError?,
) {
    init {
        require((value == null) != (error == null)) {
            "BigIntParseResult must carry exactly one of value or error"
        }
    }

    fun isSuccess(): Boolean = value != null

    fun isFailure(): Boolean = error != null

    fun getOrNull(): BigInt? = value

    fun getOrThrow(): BigInt =
        when {
            value != null -> value
            error != null -> throw IllegalArgumentException(error.message)
            else -> error("BigIntParseResult invariant violated")
        }
}
