package io.github.chrisimx.esclkt

actual suspend fun <T> catchAny(block: suspend () -> T): Result<T> {
    return try {
        Result.Success(block())
    } catch (e: Exception) {
        Result.Error(e)
    } catch (e: dynamic) {
        Result.Error(e)
    }
}