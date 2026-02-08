package io.github.chrisimx.esclkt

actual fun <T> platformErrorMapping(e: Any): ESCLHttpCallResult<T> {
    return when (e) {
        else -> ESCLHttpCallResult.Error.UnknownError(e)
    }
}