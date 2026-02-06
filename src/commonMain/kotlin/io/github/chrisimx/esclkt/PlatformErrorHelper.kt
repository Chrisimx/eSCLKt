package io.github.chrisimx.esclkt

sealed class Result<T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Error<T>(val error: Any) : Result<T>()
}
expect suspend fun <T> catchAny(block: suspend () -> T): Result<T>