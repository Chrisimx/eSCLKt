package io.github.chrisimx.esclkt

import kotlin.coroutines.cancellation.CancellationException

actual suspend fun <T> catchAny(block: suspend () -> T): Result<T> =
    try {
        Result.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Error(e)
    }
