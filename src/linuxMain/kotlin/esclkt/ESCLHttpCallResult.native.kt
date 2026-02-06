package io.github.chrisimx.esclkt

actual fun <T> platformErrorMapping(e: Any): ESCLHttpCallResult<T> {
    return when (e) {
        is IllegalStateException -> {
            when {
                e.message?.contains("TLS verification failed") ?: false -> ESCLHttpCallResult.Error.UntrustedCertificate(e.message)
                else -> ESCLHttpCallResult.Error.UnknownError(e)
            }
        }
        else -> ESCLHttpCallResult.Error.UnknownError(e)
    }
}