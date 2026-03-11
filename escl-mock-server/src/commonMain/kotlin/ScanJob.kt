package io.github.chrisimx.esclmockserver

import kotlin.uuid.Uuid

data class ScanJob(
    val uuid: Uuid = Uuid.generateV4(),
    var retrievedPages: UInt = 0u
)