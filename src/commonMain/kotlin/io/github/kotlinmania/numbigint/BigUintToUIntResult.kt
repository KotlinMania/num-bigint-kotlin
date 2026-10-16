package io.github.kotlinmania.numbigint

class BigUintToUIntResult internal constructor(
    val value: UInt?,
    val error: TryFromBigIntError?,
) {
    init {
        require((value == null) != (error == null)) {
            "BigUintToUIntResult must carry exactly one of value or error"
        }
    }

    fun isSuccess(): Boolean = value != null

    fun isFailure(): Boolean = error != null

    fun getOrNull(): UInt? = value

    fun getOrThrow(): UInt =
        when {
            value != null -> value
            error != null -> throw IllegalArgumentException(error.message)
            else -> error("BigUintToUIntResult invariant violated")
        }
}
