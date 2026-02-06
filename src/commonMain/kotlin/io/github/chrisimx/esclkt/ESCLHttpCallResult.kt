package io.github.chrisimx.esclkt

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.io.IOException

sealed class ESCLHttpCallResult<out T> {
    /**
     * Represents successful network responses (2xx).
     */
    data class Success<T>(val body: T, val response: HttpResponse) : ESCLHttpCallResult<T>()

    sealed class Error : ESCLHttpCallResult<Nothing>() {
        /**
         * Represents server (50x) and client (40x) errors.
         */
        data class HttpError(val code: Int, val errorBody: String?) : Error()

        /**
         * Represent IOExceptions and connectivity issues.
         */
        data class NetworkError(val exception: Exception) : Error()

        /**
         * Represent SerializationExceptions.
         */
        data class UnknownError (val exception: Any) : Error()

        data class UntrustedCertificate(val cause: String? = null) : Error()
    }
}

suspend inline fun ResponseException.errorBody(): String? =
    try {
        response.bodyAsText()
    } catch (e: Exception) {
        null
    }

suspend inline fun <reified T> HttpClient.safeRequest(
    urlString: String,
    crossinline block: HttpRequestBuilder.() -> Unit,
): ESCLHttpCallResult<T> {
    val result = catchAny<ESCLHttpCallResult.Success<T>> {
        val response = request(urlString) { block() }
        ESCLHttpCallResult.Success(response.body(), response)
    }

    return when (result) {
        is Result.Success -> {
            ESCLHttpCallResult.Success(result.value.body, result.value.response)
        }
        is Result.Error<*> -> commonErrorMapping(result.error)
    }
}

suspend fun <T> commonErrorMapping(e: Any): ESCLHttpCallResult<T> =
    when (e) {
        is ClientRequestException -> ESCLHttpCallResult.Error.HttpError(e.response.status.value, e.errorBody())
        is ServerResponseException -> ESCLHttpCallResult.Error.HttpError(e.response.status.value, e.errorBody())
        is IOException -> ESCLHttpCallResult.Error.NetworkError(e)
        else -> platformErrorMapping(e)
    }

/** This function maps Ktor engine and system specific exceptions to system-independent error types **/
expect fun <T> platformErrorMapping(e: Any): ESCLHttpCallResult<T>