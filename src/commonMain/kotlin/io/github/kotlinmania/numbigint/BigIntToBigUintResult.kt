package io.github.kotlinmania.numbigint

class BigIntToBigUintResult internal constructor(
    val value: BigUint?,
    val error: TryFromBigIntError?,
) {
    init {
        require((value == null) != (error == null)) {
            "BigIntToBigUintResult must carry exactly one of value or error"
        }
    }

    fun isSuccess(): Boolean = value != null

    fun isFailure(): Boolean = error != null

    fun getOrNull(): BigUint? = value

    fun getOrThrow(): BigUint =
        when {
            value != null -> value
            error != null -> throw IllegalArgumentException(error.message)
            else -> error("BigIntToBigUintResult invariant violated")
        }
}
