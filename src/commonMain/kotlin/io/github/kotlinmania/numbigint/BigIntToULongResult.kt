package io.github.kotlinmania.numbigint

class BigIntToULongResult internal constructor(
    val value: ULong?,
    val error: TryFromBigIntError?,
) {
    init {
        require((value == null) != (error == null)) {
            "BigIntToULongResult must carry exactly one of value or error"
        }
    }

    fun isSuccess(): Boolean = value != null

    fun isFailure(): Boolean = error != null

    fun getOrNull(): ULong? = value

    fun getOrThrow(): ULong =
        when {
            value != null -> value
            error != null -> throw IllegalArgumentException(error.message)
            else -> error("BigIntToULongResult invariant violated")
        }
}
