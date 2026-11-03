package io.github.kotlinmania.numbigint

class BigIntToLongResult internal constructor(
    val value: Long?,
    val error: TryFromBigIntError?,
) {
    init {
        require((value == null) != (error == null)) {
            "BigIntToLongResult must carry exactly one of value or error"
        }
    }

    fun isSuccess(): Boolean = value != null

    fun isFailure(): Boolean = error != null

    fun getOrNull(): Long? = value

    fun getOrThrow(): Long =
        when {
            value != null -> value
            error != null -> throw IllegalArgumentException(error.message)
            else -> error("BigIntToLongResult invariant violated")
        }
}
