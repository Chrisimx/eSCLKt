/*
 *     Copyright (C) 2024 Christian Nagel and contributors
 *
 *     This file is part of eSCLKt.
 *
 *     eSCLKt is free software: you can redistribute it and/or modify it under the terms of
 *     the GNU General Public License as published by the Free Software Foundation, either
 *     version 3 of the License, or (at your option) any later version.
 *
 *     eSCLKt is distributed in the hope that it will be useful, but WITHOUT ANY
 *     WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *     FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along with eSCLKt.
 *     If not, see <https://www.gnu.org/licenses/>.
 *
 *     SPDX-License-Identifier: GPL-3.0-or-later
 */

package io.github.chrisimx.esclkt

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlNamespaceDeclSpec
import nl.adaptivity.xmlutil.serialization.XmlNamespaceDeclSpecs
import nl.adaptivity.xmlutil.serialization.XmlPolyChildren
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.w3c.dom.Element
import org.w3c.dom.Node.ELEMENT_NODE
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class InputSourceCaps(
    @XmlSerialName("MinWidth", NS_SCAN, "scan")
    val minWidth: ThreeHundredthsOfInch,
    @XmlSerialName("MaxWidth", NS_SCAN, "scan")
    val maxWidth: ThreeHundredthsOfInch,
    @XmlSerialName("MinHeight", NS_SCAN, "scan")
    val minHeight: ThreeHundredthsOfInch,
    @XmlSerialName("MaxHeight", NS_SCAN, "scan")
    val maxHeight: ThreeHundredthsOfInch,
    @XmlSerialName("MaxScanRegions", NS_SCAN, "scan")
    val maxScanRegions: UInt?,
    @XmlSerialName("MaxOpticalXResolution", NS_SCAN, "scan")
    val maxOpticalXResolution: UInt?,
    @XmlSerialName("MaxOpticalYResolution", NS_SCAN, "scan")
    val maxOpticalYResolution: UInt?,
    @XmlSerialName("RiskyLeftMargin", NS_SCAN, "scan")
    val riskyLeftMargin: ThreeHundredthsOfInch?,
    @XmlSerialName("RiskyRightMargin", NS_SCAN, "scan")
    val riskyRightMargin: ThreeHundredthsOfInch?,
    @XmlSerialName("RiskyTopMargin", NS_SCAN, "scan")
    val riskyTopMargin: ThreeHundredthsOfInch?,
    @XmlSerialName("RiskyBottomMargin", NS_SCAN, "scan")
    val riskyBottomMargin: ThreeHundredthsOfInch?,
    @XmlSerialName("MaxPhysicalWidth", NS_SCAN, "scan")
    val maxPhysicalWidth: ThreeHundredthsOfInch?,
    @XmlSerialName("MaxPhysicalHeight", NS_SCAN, "scan")
    val maxPhysicalHeight: ThreeHundredthsOfInch?,
    @XmlSerialName("SettingProfiles", NS_SCAN, "scan")
    @XmlChildrenName("SettingProfile", NS_SCAN, "scan")
    val settingProfiles: List<SettingProfile>,
    @XmlSerialName("SupportedIntents", NS_SCAN, "scan")
    @XmlChildrenName("Intent", NS_SCAN, "scan")
    val supportedIntents: List<ScanIntentData>,
)


@Serializable
@XmlSerialName("DiscreteResolutions", NS_SCAN, "scan")
data class DiscreteResolutions(val discreteResolutions: List<DiscreteResolution>)
object DiscreteResolutionsSerializer : KSerializer<DiscreteResolutions> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("scan:DiscreteResolutions")

    override fun serialize(
        encoder: Encoder,
        value: DiscreteResolutions,
    ) {
        when (value) {
            is ScanIntentData.ScanIntentEnum -> encoder.encodeString(value.scanIntent.name)
            is ScanIntentData.StringData -> encoder.encodeString(value.string)
        }
    }

    override fun deserialize(decoder: Decoder): DiscreteResolutions {
        val decodedString = decoder.decodeString()
        decoder.
        return try {
            ScanIntentData.ScanIntentEnum(ScanIntent.valueOf(decodedString))
        } catch (exc: IllegalArgumentException) {
            ScanIntentData.StringData(decodedString)
        }
    }
}

@Serializable
@XmlSerialName("DiscreteResolution", NS_SCAN, "scan")
data class DiscreteResolution(
    @XmlSerialName("XResolution", NS_SCAN, "scan")
    val xResolution: UInt,
    @XmlSerialName("YResolution", NS_SCAN, "scan")
    val yResolution: UInt,
)

@Serializable
@XmlSerialName("SettingProfile", NS_SCAN, "scan")
data class SettingProfile(
    @XmlSerialName("ColorModes", NS_SCAN, "scan")
    @XmlChildrenName("ColorMode", NS_SCAN, "scan")
    val colorModes: List<ColorMode>,
    @XmlSerialName("ContentTypes", NS_SCAN, "scan")
    @XmlChildrenName("ContentType", NS_SCAN, "scan")
    val contentTypes: List<ContentType>?,
    @XmlSerialName("DocumentFormats", NS_SCAN, "scan")
    val documentFormats: DocumentFormats,
    val supportedResolutions: DiscreteResolutions,
    @XmlSerialName("ColorSpaces", NS_SCAN, "scan")
    @XmlChildrenName("ColorSpace", NS_SCAN, "scan")
    val colorSpaces: List<String>? = null,
    @XmlSerialName("CCDChannels", NS_SCAN, "scan")
    @XmlChildrenName("CCDChannel", NS_SCAN, "scan")
    val ccdChannels: List<CcdChannel>? = null,
)

@Serializable
data class DocumentFormats(
    val documentFormat: List<String>,
    val documentFormatExt: List<String>,
) {
    companion object {
        fun fromXMLElement(documentFormatsElem: Element): DocumentFormats {
            val documentFormatElems = documentFormatsElem.getElementsByTagName("pwg:DocumentFormat")
            val documentFormatExtElems = documentFormatsElem.getElementsByTagName("scan:DocumentFormatExt")
            if (documentFormatElems.length < 2 &&
                documentFormatExtElems.length < 2
            ) {
                throw IllegalArgumentException("Mandatory document formats not found. Case 1")
            }
            val documentFormats: MutableList<String> = mutableListOf()
            val documentFormatsExt: MutableList<String> = mutableListOf()
            for (i in 0..<documentFormatElems.length) {
                documentFormats.add(documentFormatElems.item(i).textContent)
            }
            for (j in 0..<documentFormatExtElems.length) {
                documentFormatsExt.add(documentFormatExtElems.item(j).textContent)
            }

            // Scanners MUST at least support PDF and JPEG
            val documentFormatExtInvalid =
                !documentFormatsExt.contains("application/pdf") || !documentFormatsExt.contains("image/jpeg")
            val documentFormatInvalid =
                !documentFormats.contains("application/pdf") || !documentFormats.contains("image/jpeg")
            if (documentFormatExtInvalid && documentFormatInvalid) {
                throw IllegalArgumentException("Mandatory document formats not found. Case 2")
            }
            return DocumentFormats(documentFormats, documentFormatsExt)
        }
    }
}

@XmlSerialName("Intent", NS_SCAN, "scan")
enum class ScanIntent {
    // / Scanning optimized for text.
    Document,

    // / A composite document with mixed text/graphic/photo content.
    TextAndGraphic,

    // / Scanning optimized for photo
    Photo,

    // / Scanning optimized for performance (fast output)
    Preview,

    // / Scanning optimized for 3 dimensional objects - objects with depth
    Object,

    // / Scanning optimized for a business card
    BusinessCard,
}

@Serializable(with = ScanIntentDataSerializer::class)
sealed class ScanIntentData {
    data class ScanIntentEnum(
        val scanIntent: ScanIntent,
    ) : ScanIntentData()

    data class StringData(
        val string: String,
    ) : ScanIntentData()
}

object ScanIntentDataSerializer : KSerializer<ScanIntentData> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("scan:ScanIntent")

    override fun serialize(
        encoder: Encoder,
        value: ScanIntentData,
    ) {
        when (value) {
            is ScanIntentData.ScanIntentEnum -> encoder.encodeString(value.scanIntent.name)
            is ScanIntentData.StringData -> encoder.encodeString(value.string)
        }
    }

    override fun deserialize(decoder: Decoder): ScanIntentData {
        val decodedString = decoder.decodeString()
        return try {
            ScanIntentData.ScanIntentEnum(ScanIntent.valueOf(decodedString))
        } catch (exc: IllegalArgumentException) {
            ScanIntentData.StringData(decodedString)
        }
    }
}

fun ScanIntentData.toScanIntentString(): String =
    when (this) {
        is ScanIntentData.ScanIntentEnum -> this.scanIntent.toString()
        is ScanIntentData.StringData -> this.string
    }

@XmlSerialName("ContentType", NS_SCAN, "scan")
enum class ContentType {
    Photo,
    Text,
    TextAndPhoto,
    LineArt,
    Magazine,
    Halftone,
    Auto,
}

@XmlSerialName("CcdChannel", NS_SCAN, "scan")
enum class CcdChannel {
    // / Use the Red CCD
    Red,

    // / Use the Green CCD
    Green,

    // / Use the Blue CCD
    Blue,

    // / Weighted combination of the three color channels optimized for photos
    NTSC,

    // / A dedicated Gray CCD array in the hardware (optimized for documents)
    GrayCcd,

    // / An emulated Gray CCD mode where each CCD line are given even weight (1/3 R, 1/3 G, 1/3 B)
    // / (optimized for documents).
    GrayCcdEmulated,
}

@Serializable
@XmlSerialName("Platen", NS_SCAN, "scan")
data class Platen(
    @XmlSerialName("PlatenInputCaps", NS_SCAN, "scan")
    val inputSourceCaps: InputSourceCaps,
)

@Serializable
@XmlSerialName("Adf", NS_SCAN, "scan")
data class Adf(
    @XmlSerialName("AdfSimplexInputCaps", NS_SCAN, "scan")
    val simplexCaps: InputSourceCaps,
    @XmlSerialName("AdfDuplexInputCaps", NS_SCAN, "scan")
    val duplexCaps: InputSourceCaps? = null,
    @XmlSerialName("FeederCapacity", NS_SCAN, "scan")
    val feederCapacity: UInt? = null,
    @XmlSerialName("AdfOptions", NS_SCAN, "scan")
    @XmlChildrenName("AdfOption", NS_SCAN, "scan")
    val adfOptions: List<AdfOption>,
)

@XmlSerialName("AdfOption", NS_SCAN, "scan")
enum class AdfOption {
    DetectPaperLoaded,
    SelectSinglePage,
    Duplex,
    MultipickDetection,
}

@Serializable
@XmlSerialName("SharpenSupport", NS_SCAN, "scan")
data class SharpenSupport(
    @XmlSerialName("Min", NS_SCAN, "scan")
    val min: Int,
    @XmlSerialName("Max", NS_SCAN, "scan")
    val max: Int,
    @XmlSerialName("Normal", NS_SCAN, "scan")
    val normal: Int?,
    @XmlSerialName("Step", NS_SCAN, "scan")
    val step: Int,
)

@Serializable
@XmlSerialName("CompressionFactorSupport", NS_SCAN, "scan")
data class CompressionFactorSupport(
    val min: Int,
    val max: Int,
    val normal: Int,
    val step: Int,
)

@XmlSerialName("ColorMode", NS_SCAN, "scan")
enum class ColorMode {
    BlackAndWhite1,

    // / 8-bit grayscale
    Grayscale8,

    // / 16-bit grayscale
    Grayscale16,

    // / 8-bit per channel RGB
    RGB24,

    // / 16-bit per channel RGB
    RGB48,
    AutoColorDetection,
    ;

    companion object {
        fun from(name: String): ColorMode? = entries.find { it.name.equals(name.removePrefix("scan:"), ignoreCase = true) }
    }
}

@Serializable
@XmlSerialName("Certification", NS_SCAN, "scan")
data class Certification(
    @XmlSerialName("Name", NS_SCAN, "scan")
    val name: String,
    @XmlSerialName("Version", NS_SCAN, "scan")
    val version: String,
)

@Serializable
@XmlSerialName("StoredJobRequestSupport", NS_SCAN, "scan")
data class StoredJobRequestSupport(
    @XmlSerialName("MaxStoredjobRequests", NS_SCAN, "scan")
    val maxStoredJobRequests: UInt,
    @XmlSerialName("TimeoutInSeconds", NS_SCAN, "scan")
    val timoutInSeconds: UInt,
)

@OptIn(ExperimentalXmlUtilApi::class)
@Serializable
@XmlSerialName("ScannerCapabilities", NS_SCAN, "scan")
@XmlNamespaceDeclSpecs("pwg=http://www.pwg.org/schemas/2010/12/sm", "scan=http://schemas.hp.com/imaging/escl/2011/05/03")
data class ScannerCapabilities @OptIn(ExperimentalUuidApi::class) constructor(
        @XmlSerialName("Versiom", NS_PWG, "pwg")
        val interfaceVersion: String,
        @XmlSerialName("MakeAndModel", NS_PWG, "pwg")
        val makeAndModel: String,
        @XmlSerialName("Manufacturer", NS_SCAN, "scan")
        val manufacturer: String? = null,
        /** Conforming to RFC 4122, has to match mDNS "UUID" txt record **/
        val deviceUuid: Uuid? = null,
        @XmlSerialName("SerialNumber", NS_PWG, "pwg")
        val serialNumber: String,
        @XmlSerialName("AdminURI", NS_SCAN, "scan")
        val adminURI: String? = null,
        @XmlSerialName("IconURI", NS_SCAN, "scan")
        val iconURI: String? = null,
        @XmlSerialName("Certifications", NS_SCAN, "scan")
        @XmlChildrenName("Certification", NS_SCAN, "scan")
        val certifications: List<Certification>?,
        @XmlElement
        val platen: Platen?,
        @XmlElement
        val adf: Adf?,
        @XmlSerialName("SupportedMediaTypes", NS_SCAN, "scan")
        @XmlChildrenName("MediaType", NS_SCAN, "scan")
        val supportedMediaTypes: List<String>?,
        @XmlElement
        val sharpenSupport: SharpenSupport?,
        @XmlElement
        val compressionFactorSupport: CompressionFactorSupport?,
        @XmlElement
        val storedJobRequestSupport: StoredJobRequestSupport?)