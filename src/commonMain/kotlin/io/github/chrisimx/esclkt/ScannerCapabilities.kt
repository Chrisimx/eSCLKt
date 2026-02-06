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
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import nl.adaptivity.xmlutil.EventType.*
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.XmlException
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlNamespaceDeclSpecs
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue
import nl.adaptivity.xmlutil.startTag
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@XmlSerialName("SupportedEdge", NS_SCAN, "scan")
enum class Edge {
    TopEdge,
    BottomEdge,
    LeftEdge,
    RightEdge,
}

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
    @Serializable(with = ScanIntentDataListSerializer::class)
    val supportedIntents: List<EnumOrRaw<ScanIntent>>,
    @XmlSerialName("EdgeAutoDetection", NS_SCAN, "scan")
    @XmlChildrenName("SupportedEdge", NS_SCAN, "scan")
    val edgeAutoDetection: List<Edge>,
)


@Serializable
@XmlSerialName("DiscreteResolution", NS_SCAN, "scan")
data class DiscreteResolution(
    @XmlSerialName("XResolution", NS_SCAN, "scan")
    val xResolution: UInt,
    @XmlSerialName("YResolution", NS_SCAN, "scan")
    val yResolution: UInt,
)

@Serializable
@XmlSerialName("SupportedResolutions", NS_SCAN, "scan")
data class SupportedResolutions(
    @XmlSerialName("DiscreteResolutions", NS_SCAN, "scan")
    @XmlChildrenName("DiscreteResolution", NS_SCAN, "scan")
    val discreteResolutions: List<DiscreteResolution>
)

@Serializable
@XmlSerialName("SettingProfile", NS_SCAN, "scan")
data class SettingProfile(
    @XmlSerialName("ColorModes", NS_SCAN, "scan")
    @XmlChildrenName("ColorMode", NS_SCAN, "scan")
    @Serializable(with = ColorModeListSerializer::class)
    val colorModes: List<EnumOrRaw<ColorMode>>,
    @XmlSerialName("ContentTypes", NS_SCAN, "scan")
    @XmlChildrenName("ContentType", NS_PWG, "scan")
    @Serializable(with = ContentTypeDataListSerializer::class)
    val contentTypes: List<EnumOrRaw<ContentType>>?,
    @XmlSerialName("DocumentFormats", NS_SCAN, "scan")
    val documentFormats: DocumentFormats,
    val supportedResolutions: SupportedResolutions,
    @XmlSerialName("ColorSpaces", NS_SCAN, "scan")
    @XmlChildrenName("ColorSpace", NS_SCAN, "scan")
    val colorSpaces: List<String>? = null,
    @XmlSerialName("CcdChannels", NS_SCAN, "scan")
    @XmlChildrenName("CcdChannel", NS_SCAN, "scan")
    @Serializable(with = CcdChannelListSerializer::class)
    val ccdChannels: List<EnumOrRaw<CcdChannel>>? = null,
    @XmlSerialName("BinaryRenderings", NS_SCAN, "scan")
    @XmlChildrenName("BinaryRendering", NS_SCAN, "scan")
    val binaryRenderings: List<String>? = null,
)

@Serializable(with = DocumentFormatsSerializer::class)
@XmlSerialName("DocumentFormats", NS_SCAN, "scan")
data class DocumentFormats(
    val documentFormats: List<String>,
    val documentFormatExt: List<String>
)

object DocumentFormatsSerializer : KSerializer<DocumentFormats> {

    private val dfSerializer = ListSerializer(String.serializer())
    private val dfeSerializer = ListSerializer(String.serializer())

    override val descriptor = buildClassSerialDescriptor("DocumentFormats") {
        element("DocumentFormat", dfSerializer.descriptor)
        element("DocumentFormatExt", dfeSerializer.descriptor)
    }

    override fun serialize(encoder: Encoder, value: DocumentFormats) {
        (encoder as? XML.XmlOutput)?.target?.run {
            startTag(NS_SCAN, "DocumentFormats", "scan")
            // encode all pwg:DocumentFormat elements
            value.documentFormats.forEach { format ->
                startTag(NS_PWG, "DocumentFormat", "pwg") {
                    text(format)
                }
            }

            // encode all scan:DocumentFormatExt elements
            value.documentFormatExt.forEach { format ->
                startTag(NS_SCAN, "DocumentFormatExt", "scan") {
                    text(format)
                }
            }
            endTag(NS_SCAN, "DocumentFormats", "scan")
        }
    }

    enum class DocumentFormatElementType {
        EXT,
        LEGACY
    }

    override fun deserialize(decoder: Decoder): DocumentFormats {
        val df: MutableList<String> = mutableListOf()
        val dfe: MutableList<String> = mutableListOf()
        var depth = 0
        var currentDocumentFormatElementType: DocumentFormatElementType? = null

        (decoder as? XML.XmlInput)?.input?.run {
            if (prefix != "scan") {
                throw XmlException("unexpected prefix in DocumentFormats element: $prefix ($extLocationInfo)")
            }
            if (localName != "DocumentFormats") {
                throw XmlException("unexpected localName. Expected a DocumentFormats element but got $localName ($extLocationInfo)")
            }

            while (hasNext()) {
                val newEvent = next()
                when (newEvent) {
                    START_ELEMENT -> {
                        depth++

                        if (depth > 1) {
                            throw XmlException("unexpected depth in DocumentFormats, tag started $depth ($localName) ($extLocationInfo)")
                        }

                        currentDocumentFormatElementType = when (localName) {
                            "DocumentFormat" -> DocumentFormatElementType.LEGACY
                            "DocumentFormatExt" -> DocumentFormatElementType.EXT
                            "DocumentFormats" -> {
                                throw XmlException("unexpected localName in DocumentFormats tag. Duplicate DocumentFormats tag $localName ($extLocationInfo)")
                            }
                            else -> {
                                throw XmlException("unexpected localName in DocumentFormats tag started $localName ($extLocationInfo)")
                            }
                        }
                    }
                    END_ELEMENT -> {
                        when (localName) {
                            "DocumentFormats" -> break
                            "DocumentFormatExt", "DocumentFormat" -> currentDocumentFormatElementType = null
                            else -> throw XmlException("unexpected localName in DocumentFormats tag ended $localName ($extLocationInfo)")
                        }

                        depth--

                        if (depth < 0) {
                            throw XmlException("unexpected depth in DocumentFormats, tag ended $depth ($localName) ($extLocationInfo)")
                        }
                    }
                    TEXT -> {
                        when (currentDocumentFormatElementType) {
                            DocumentFormatElementType.EXT -> dfe.add(text)
                            DocumentFormatElementType.LEGACY -> df.add(text)
                            null -> throw XmlException("text $text in DocumentFormats element without opened tag ($extLocationInfo")
                        }
                    }
                    END_DOCUMENT -> {
                        throw XmlException("document ended unexpectedly with unclosed DocumentFormats tag ($extLocationInfo)")
                    }
                    else -> {}
                }
            }
        }

        return DocumentFormats(df, dfe)
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

object ScanIntentDataSerializer : KSerializer<EnumOrRaw<ScanIntent>> by enumOrRawSerializer<ScanIntent>()
object ScanIntentDataListSerializer : KSerializer<List<EnumOrRaw<ScanIntent>>> by ListSerializer(enumOrRawSerializer<ScanIntent>())

sealed class EnumOrRaw<E : Enum<E>> {

    data class Known<E : Enum<E>>(val value: E) : EnumOrRaw<E>()

    data class Unknown<E : Enum<E>>(val raw: String) : EnumOrRaw<E>()

    fun asString(): String = when (this) {
        is Known -> value.name
        is Unknown -> raw
    }
}

class EnumOrRawSerializer<E : Enum<E>>(
    private val enumValues: Array<E>
) : KSerializer<EnumOrRaw<E>> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("EnumOrRaw", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: EnumOrRaw<E>) {
        encoder.encodeString(
            when (value) {
                is EnumOrRaw.Known -> value.value.name
                is EnumOrRaw.Unknown -> value.raw
            }
        )
    }

    override fun deserialize(decoder: Decoder): EnumOrRaw<E> {
        val str = decoder.decodeString()
        val cleanedReadString = str.removePrefix("scan:") // Needed because of brother-ds‑940dw-caps.xml
        val match = enumValues.firstOrNull { it.name == cleanedReadString}
        return if (match != null) {
            EnumOrRaw.Known(match)
        } else {
            EnumOrRaw.Unknown(str)
        }
    }
}

inline fun <reified E : Enum<E>> enumOrRawSerializer():
        KSerializer<EnumOrRaw<E>> =
    EnumOrRawSerializer(enumValues<E>())



@Serializable
@XmlSerialName("Intent", NS_SCAN, "scan")
private class ScanIntentSerialDescriptorDelegate(
    @XmlValue
    val intent: String,
)

@XmlSerialName("ContentType", NS_SCAN, "scan")
enum class ContentType {
    Photo,
    Text,
    TextAndPhoto,
    LineArt,
    Magazine,
    Halftone,
    Auto,
    Thru
}

object ContentTypeDataSerializer : KSerializer<EnumOrRaw<ContentType>> by enumOrRawSerializer<ContentType>()
object ContentTypeDataListSerializer : KSerializer<List<EnumOrRaw<ContentType>>> by ListSerializer(enumOrRawSerializer<ContentType>())

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

object CcdChannelSerializer : KSerializer<EnumOrRaw<CcdChannel>> by enumOrRawSerializer<CcdChannel>()
object CcdChannelListSerializer : KSerializer<List<EnumOrRaw<CcdChannel>>> by ListSerializer(enumOrRawSerializer<CcdChannel>())

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
    @Serializable(with = AdfOptionListSerializer::class)
    val adfOptions: List<EnumOrRaw<AdfOption>>,
)

@XmlSerialName("AdfOption", NS_SCAN, "scan")
enum class AdfOption {
    DetectPaperLoaded,
    SelectSinglePage,
    Duplex,
    MultipickDetection,
}

object AdfOptionListSerializer : KSerializer<List<EnumOrRaw<AdfOption>>> by ListSerializer(enumOrRawSerializer<AdfOption>())

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
    @XmlSerialName("Min", NS_SCAN, "scan")
    val min: Int,
    @XmlSerialName("Max", NS_SCAN, "scan")
    val max: Int,
    @XmlSerialName("Normal", NS_SCAN, "scan")
    val normal: Int,
    @XmlSerialName("Step", NS_SCAN, "scan")
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
}

object ColorModeListSerializer : KSerializer<List<EnumOrRaw<ColorMode>>> by ListSerializer(enumOrRawSerializer<ColorMode>())

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
        @XmlSerialName("Version", NS_PWG, "pwg")
        val interfaceVersion: String,
        @XmlSerialName("MakeAndModel", NS_PWG, "pwg")
        val makeAndModel: String,
        @XmlSerialName("Manufacturer", NS_SCAN, "scan")
        val manufacturer: String? = null,
        @XmlSerialName("UUID", NS_SCAN, "scan")
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
        val certifications: List<Certification>? = null,
        @XmlElement
        val platen: Platen? = null,
        @XmlElement
        val adf: Adf? = null,
        @XmlSerialName("SupportedMediaTypes", NS_SCAN, "scan")
        @XmlChildrenName("MediaType", NS_SCAN, "scan")
        val supportedMediaTypes: List<String>? = null,
        @XmlElement
        val sharpenSupport: SharpenSupport? = null,
        @XmlElement
        val compressionFactorSupport: CompressionFactorSupport? = null,
        @XmlElement
        val storedJobRequestSupport: StoredJobRequestSupport? = null,)