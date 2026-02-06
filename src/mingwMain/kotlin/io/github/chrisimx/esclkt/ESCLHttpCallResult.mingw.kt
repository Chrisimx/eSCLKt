package io.github.chrisimx.esclkt

actual fun <T> platformErrorMapping(e: Any): io.github.chrisimx.esclkt.ESCLHttpCallResult<T> {
    return when (e) {
        else -> ESCLHttpCallResult.Error.UnknownError(e)
    }
}