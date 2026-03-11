package io.github.chrisimx.esclmockserver

import com.goncalossilva.resources.Resource

data class EsclMockServerArgs(
    val bindingAddress: String = "127.0.0.1",
    val resourcePath: String = "/eSCL",
    val scannerCaps: String = Resource("default_scanner_caps.xml").readText(),
    val servedImage: ByteArray = Resource("example_image.jpg").readBytes(),
    val count: UInt = 1u,
    val port: UShort = 8080.toUShort()
) {

    class Builder {
        var bindingAddress: String = "127.0.0.1"
        var resourcePath: String = "/eSCL"
        var scannerCaps: String = Resource("default_scanner_caps.xml").readText()
        var servedImage: ByteArray = Resource("example_image.jpg").readBytes()
        var count: UInt = 1u
        var port: UShort = 8080.toUShort()

        fun build(): EsclMockServerArgs {
            require(count >= 1u) { "count must be >= 1" }
            require(port >= 1.toUShort()) { "port must be >= 1" }
            return EsclMockServerArgs(
                bindingAddress,
                resourcePath,
                scannerCaps,
                servedImage,
                count,
                port
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as EsclMockServerArgs

        if (bindingAddress != other.bindingAddress) return false
        if (resourcePath != other.resourcePath) return false
        if (scannerCaps != other.scannerCaps) return false
        if (!servedImage.contentEquals(other.servedImage)) return false
        if (count != other.count) return false
        if (port != other.port) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bindingAddress.hashCode()
        result = 31 * result + resourcePath.hashCode()
        result = 31 * result + scannerCaps.hashCode()
        result = 31 * result + servedImage.contentHashCode()
        result = 31 * result + count.hashCode()
        result = 31 * result + port.hashCode()
        return result
    }
}

fun esclMockServerArgs(block: EsclMockServerArgs.Builder.() -> Unit): EsclMockServerArgs =
    EsclMockServerArgs.Builder().apply(block).build()