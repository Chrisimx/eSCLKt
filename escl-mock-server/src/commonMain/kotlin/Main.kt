package io.github.chrisimx.esclmockserver

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>): Unit = runBlocking {
    val server = EsclMockServer {}
    awaitCancellation()
}