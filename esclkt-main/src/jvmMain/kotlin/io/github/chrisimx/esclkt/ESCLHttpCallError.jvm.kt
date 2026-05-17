package io.github.chrisimx.esclkt

import io.ktor.util.network.UnresolvedAddressException
import java.security.cert.CertPathBuilderException
import java.security.cert.CertPathValidatorException
import javax.net.ssl.SSLHandshakeException

actual fun <T> platformErrorMapping(e: Any): ESCLHttpCallResult<T>? = when (e) {
        is UnresolvedAddressException -> ESCLHttpCallResult.Error.NetworkError(e)
        is CertPathBuilderException, is CertPathValidatorException, is SSLHandshakeException
            -> ESCLHttpCallResult.Error.UntrustedCertificate(e.message)
        else -> null
    }