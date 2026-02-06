package io.github.chrisimx.esclkt

import io.ktor.client.engine.js.JsError
import kotlin.io.println

actual fun <T> platformErrorMapping(e: Any): ESCLHttpCallResult<T> {
    val error = gatherJsErrorIfPossible(e)
    return ESCLHttpCallResult.Error.UnknownError(error)
}

fun gatherJsErrorIfPossible(e: dynamic): Any {
    return try {
        when {
            e == null -> "null"
            isJsError(e) -> JsError(errorObject = e)
            isJsError(js("e.error_1")) -> JsError(errorObject = js("e.error_1"))
            else -> js("String(e)") as String
        }
    } catch (_: dynamic) {
        "[unknown JS error]"
    }
}

data class JsError(val message: String?, val cause: String?, val name: String?, val stack: String?) {
    constructor(message: dynamic, cause: dynamic, name: dynamic, stack: dynamic) :
            this(
                undefinedToNull(message),
                undefinedToNull(cause),
                undefinedToNull(name),
                undefinedToNull(stack),
            )

    constructor(errorObject: dynamic) : this(
        errorObject.message,
        errorObject.cause,
        errorObject.name,
        errorObject.stack
    )
}

fun isJsError(e: dynamic): Boolean {
    return e.stack != undefined || e.message != undefined || e.cause != undefined || e.name != undefined
}

fun <T> undefinedToNull(value: dynamic): T? {
    return if (value == undefined) null else value as T?
}
fun allProps(o: dynamic): Array<String> =
    js("Object.getOwnPropertyNames(o)") as Array<String>