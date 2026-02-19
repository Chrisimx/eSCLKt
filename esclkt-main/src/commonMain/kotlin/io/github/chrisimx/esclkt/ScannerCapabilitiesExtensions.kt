package io.github.chrisimx.esclkt

fun ScannerCapabilities.getInputSourceCaps(inputSource: InputSource, duplex: Boolean = false): InputSourceCaps = when (inputSource) {
    InputSource.Platen -> this.platen!!.inputSourceCaps
    InputSource.Feeder -> if (duplex) this.adf!!.duplexCaps!! else this.adf!!.simplexCaps
    InputSource.Camera -> TODO()
}

fun ScannerCapabilities.getInputSourceOptions(): List<InputSource> {
    val tmpInputSourceOptions = mutableListOf<InputSource>()
    if (this.platen != null) {
        tmpInputSourceOptions.add(InputSource.Platen)
    }
    if (this.adf != null) {
        tmpInputSourceOptions.add(InputSource.Feeder)
    }
    return tmpInputSourceOptions
}