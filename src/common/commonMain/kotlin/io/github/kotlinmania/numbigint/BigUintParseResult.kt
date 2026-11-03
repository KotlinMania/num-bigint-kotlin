package io.github.kotlinmania.numbigint

class BigUintParseResult internal constructor(
    val value: BigUint?,
    val error: ParseBigIntError?,
) {
    init {
        require((value == null) != (error == null)) {
            "BigUintParseResult must carry exactly one of value or error"
        }
    }

    fun isSuccess(): Boolean = value != null

    fun isFailure(): Boolean = error != null

    fun getOrNull(): BigUint? = value

    fun getOrThrow(): BigUint =
        when {
            value != null -> value
            error != null -> throw IllegalArgumentException(error.message)
            else -> error("BigUintParseResult invariant violated")
        }
}
