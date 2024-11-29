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

package io.github.chrisimx.esclkt/*
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

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.w3c.dom.Element
import org.w3c.dom.Node.ELEMENT_NODE
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private fun Element.findUniqueElementWithName(name: String, required: Boolean = false): Element? {
    val elements = this.getElementsByTagName(name)
    if (required && elements.length != 1) throw IllegalArgumentException("Required element not found: $name")

    return elements.item(0) as Element?
}

private fun Element.findRequiredUniqueElementWithName(name: String): Element {
    return this.findUniqueElementWithName(name, true)!!
}

data class InputSourceCaps(
    val minWidth: UInt,
    val maxWidth: UInt,
    val minHeight: UInt,
    val maxHeight: UInt,
    val maxScanRegions: UInt,
    val maxOpticalXResolution: UInt,
    val maxOpticalYResolution: UInt,
    val riskyLeftMargin: UInt,
    val riskyRightMargin: UInt,
    val riskyTopMargin: UInt,
    val riskyBottomMargin: UInt,
    val maxPhysicalWidth: UInt?,
    val maxPhysicalHeight: UInt?,
    val settingProfiles: List<SettingProfile>,
    val supportedIntents: List<Any>,
) {
    companion object {
        fun fromXMLElement(inputSourceCapsElem: Element): InputSourceCaps {
            val settingsProfiles = inputSourceCapsElem
                .findRequiredUniqueElementWithName("scan:SettingProfiles")
                .getElementsByTagName("scan:SettingProfile").let {
                    val settingProfiles = mutableListOf<SettingProfile>()
                    for (i in 0..<it.length) {
                        settingProfiles.add(SettingProfile.fromXMLElement(it.item(i) as Element))
                    }
                    settingProfiles
                }
            val scanIntents = inputSourceCapsElem.findRequiredUniqueElementWithName("scan:SupportedIntents")
                .getElementsByTagName("scan:Intent").let {
                    val intents = mutableListOf<Any>()
                    for (i in 0..<it.length) {
                        try {
                            intents.add(ScanIntent.valueOf(it.item(i).textContent))
                        } catch (e: IllegalArgumentException) {
                            intents.add(it.item(i).textContent)
                        }
                    }
                    intents
                }

            val resultISC: InputSourceCaps
            try {
                resultISC = InputSourceCaps(
                    inputSourceCapsElem.findRequiredUniqueElementWithName("scan:MinWidth").textContent.toUInt(),
                    inputSourceCapsElem.findRequiredUniqueElementWithName("scan:MaxWidth").textContent.toUInt(),
                    inputSourceCapsElem.findRequiredUniqueElementWithName("scan:MinHeight").textContent.toUInt(),
                    inputSourceCapsElem.findRequiredUniqueElementWithName("scan:MaxHeight").textContent.toUInt(),
                    inputSourceCapsElem.findRequiredUniqueElementWithName("scan:MaxScanRegions").textContent.toUInt(),
                    inputSourceCapsElem.findRequiredUniqueElementWithName("scan:MaxOpticalXResolution").textContent.toUInt(),
                    inputSourceCapsElem.findRequiredUniqueElementWithName("scan:MaxOpticalYResolution").textContent.toUInt(),
                    inputSourceCapsElem.findRequiredUniqueElementWithName("scan:RiskyLeftMargin").textContent.toUInt(),
                    inputSourceCapsElem.findRequiredUniqueElementWithName("scan:RiskyRightMargin").textContent.toUInt(),
                    inputSourceCapsElem.findRequiredUniqueElementWithName("scan:RiskyTopMargin").textContent.toUInt(),
                    inputSourceCapsElem.findRequiredUniqueElementWithName("scan:RiskyBottomMargin").textContent.toUInt(),
                    inputSourceCapsElem.findUniqueElementWithName("scan:MaxPhysicalWidth")?.textContent?.toUInt(),
                    inputSourceCapsElem.findUniqueElementWithName("scan:MaxPhysicalHeight")?.textContent?.toUInt(),
                    settingsProfiles,
                    scanIntents
                )
            } catch (exception: NumberFormatException) {
                throw IllegalArgumentException("InputSourceCaps invalid as one of the numbers is invalid: $exception")
            }
            return resultISC
        }
    }
}

data class DiscreteResolution(
    val xResolution: UInt,
    val yResolution: UInt,
) {
    companion object {
        fun fromXMLElement(discreteResolutionElem: Element): DiscreteResolution {
            val xResolution: UInt
            val yResolution: UInt
            try {
                xResolution =
                    discreteResolutionElem.findRequiredUniqueElementWithName("scan:XResolution").textContent.toUInt()
                yResolution =
                    discreteResolutionElem.findRequiredUniqueElementWithName("scan:YResolution").textContent.toUInt()
            } catch (exception: NumberFormatException) {
                throw IllegalArgumentException("DiscreteResolution is invalid because number is not formated correctly: $exception")
            }
            return DiscreteResolution(xResolution, yResolution)
        }
    }
}

data class SettingProfile(
    val colorModes: List<ColorMode>,
    val contentTypes: List<ContentType>?,
    val documentFormats: DocumentFormats,
    val supportedResolutions: List<DiscreteResolution>,
    val colorSpaces: List<String>,
    val ccdChannels: List<CcdChannel>? = null
) {
    companion object {
        fun fromXMLElement(settingsProfileElem: Element): SettingProfile {
            val colorModes = settingsProfileElem
                .findRequiredUniqueElementWithName("scan:ColorModes")
                .getElementsByTagName("scan:ColorMode").let {
                    val modes = mutableListOf<ColorMode>()
                    for (i in 0..<it.length) {
                        modes.add(ColorMode.valueOf(it.item(i).textContent))
                    }
                    modes
                }

            val contentTypes = settingsProfileElem
                .findUniqueElementWithName("scan:ContentTypes")
                ?.getElementsByTagName("scan:ContentType")?.let {
                    val contentTypes = mutableListOf<ContentType>()
                    for (i in 0..<it.length) {
                        contentTypes.add(ContentType.valueOf(it.item(i).textContent))
                    }
                    contentTypes
                }

            val documentFormats = DocumentFormats.fromXMLElement(
                settingsProfileElem.findRequiredUniqueElementWithName("scan:DocumentFormats")
            )

            val supportedResolutions = settingsProfileElem
                .findRequiredUniqueElementWithName("scan:SupportedResolutions")
                .findRequiredUniqueElementWithName("scan:DiscreteResolutions")
                .getElementsByTagName("scan:DiscreteResolution").let {
                    val resolutions = mutableListOf<DiscreteResolution>()
                    for (i in 0..<it.length) {
                        resolutions.add(DiscreteResolution.fromXMLElement(it.item(i) as Element))
                    }
                    resolutions
                }

            val colorSpaces = settingsProfileElem
                .findRequiredUniqueElementWithName("scan:ColorSpaces").getElementsByTagName("scan:ColorSpace").let {
                    val spaces = mutableListOf<String>()
                    for (i in 0..<it.length) {
                        spaces.add(it.item(i).textContent)
                    }
                    spaces
                }

            val ccdChannels = settingsProfileElem
                .findUniqueElementWithName("scan:CCDChannels")?.getElementsByTagName("scan:CCDChannel")?.let {
                    val channels = mutableListOf<CcdChannel>()
                    for (i in 0 until it.length) {
                        channels.add(CcdChannel.valueOf(it.item(i).textContent))
                    }
                    channels
                }

            return SettingProfile(
                colorModes = colorModes,
                contentTypes = contentTypes,
                documentFormats = documentFormats,
                supportedResolutions = supportedResolutions,
                colorSpaces = colorSpaces,
                ccdChannels = ccdChannels
            )
        }

    }
}

data class DocumentFormats(
    val documentFormat: List<String>,
    val documentFormatExt: List<String>
) {
    companion object {
        fun fromXMLElement(documentFormatsElem: Element): DocumentFormats {
            val documentFormatElems = documentFormatsElem.getElementsByTagName("pwg:DocumentFormat")
            val documentFormatExtElems = documentFormatsElem.getElementsByTagName("scan:DocumentFormatExt")
            if (documentFormatElems.length < 2 || documentFormatExtElems.length < 2) throw IllegalArgumentException("Mandatory document formats not found")
            val documentFormats: MutableList<String> = mutableListOf()
            val documentFormatsExt: MutableList<String> = mutableListOf()
            for (i in 0..<documentFormatElems.length) {
                documentFormats.add(documentFormatElems.item(i).textContent)
            }
            for (j in 0..<documentFormatExtElems.length) {
                documentFormatsExt.add(documentFormatExtElems.item(j).textContent)
            }
            // Scanners MUST at least support PDF and JPEG
            if (!documentFormats.contains("application/pdf") || !documentFormats.contains("image/jpeg")) {
                throw IllegalArgumentException("Mandatory document formats not found")
            }
            if (!documentFormatsExt.contains("application/pdf") || !documentFormatsExt.contains("image/jpeg")) {
                throw IllegalArgumentException("Mandatory document formats not found")
            }
            return DocumentFormats(documentFormats, documentFormatsExt)
        }
    }
}

@Serializable
@XmlSerialName("scan:Intent")
enum class ScanIntent {
    /// Scanning optimized for text.
    Document,

    /// A composite document with mixed text/graphic/photo content.
    TextAndGraphic,

    /// Scanning optimized for photo
    Photo,

    /// Scanning optimized for performance (fast output)
    Preview,

    /// Scanning optimized for 3 dimensional objects - objects with depth
    Object,

    /// Scanning optimized for a business card
    BusinessCard,
}

@Serializable
@XmlSerialName("pwg:ContentType")
enum class ContentType {
    Photo,
    Text,
    TextAndPhoto,
    LineArt,
    Magazine,
    Halftone,
    Auto,
}

enum class CcdChannel {
    /// Use the Red CCD
    Red,

    /// Use the Green CCD
    Green,

    /// Use the Blue CCD
    Blue,

    /// Weighted combination of the three color channels optimized for photos
    NTSC,

    /// A dedicated Gray CCD array in the hardware (optimized for documents)
    GrayCcd,

    /// An emulated Gray CCD mode where each CCD line are given even weight (1/3 R, 1/3 G, 1/3 B)
    /// (optimized for documents).
    GrayCcdEmulated,
}

data class Platen(
    val inputSourceCaps: InputSourceCaps
) {
    companion object {
        fun fromXMLElement(platenElement: Element) =
            Platen(InputSourceCaps.fromXMLElement(platenElement.findRequiredUniqueElementWithName("scan:PlatenInputCaps")))
    }
}


data class Adf(
    val simplexCaps: InputSourceCaps,
    val duplexCaps: InputSourceCaps? = null,
    val feederCapacity: UInt,
    val adfOptions: List<AdfOption>,
) {
    companion object {
        fun fromXMLElement(adfElement: Element): Adf {
            // Extract simplexCaps - required
            val simplexCaps = InputSourceCaps.fromXMLElement(
                adfElement.findRequiredUniqueElementWithName("scan:AdfSimplexInputCaps")
            )

            // Extract duplexCaps - optional
            val duplexCaps = adfElement.findUniqueElementWithName("scan:AdfDuplexInputCaps")?.let {
                InputSourceCaps.fromXMLElement(it)
            }

            // Extract feederCapacity
            val feederCapacity =
                adfElement.findRequiredUniqueElementWithName("scan:FeederCapacity").textContent.toUInt()

            // Extract adfOptions
            val adfOptions = adfElement.findRequiredUniqueElementWithName("scan:AdfOptions")
                .getElementsByTagName("scan:AdfOption").let { optionsList ->
                    val options = mutableListOf<AdfOption>()
                    for (i in 0..<optionsList.length) {
                        options.add(AdfOption.valueOf(optionsList.item(i).textContent))
                    }
                    options
                }

            return Adf(
                simplexCaps = simplexCaps,
                duplexCaps = duplexCaps,
                feederCapacity = feederCapacity,
                adfOptions = adfOptions
            )
        }
    }
}

enum class AdfOption {
    DetectPaperLoaded,
    SelectSinglePage,
    Duplex
}

data class SharpenSupport(
    val min: Int,
    val max: Int,
    val normal: Int,
    val step: Int,
) {
    companion object {
        fun fromXMLElement(sharpenSupportElem: Element): SharpenSupport {
            val sharpenSupport: SharpenSupport
            try {
                sharpenSupport = SharpenSupport(
                    sharpenSupportElem.findRequiredUniqueElementWithName("scan:Min").textContent.toInt(),
                    sharpenSupportElem.findRequiredUniqueElementWithName("scan:Max").textContent.toInt(),
                    sharpenSupportElem.findRequiredUniqueElementWithName("scan:Normal").textContent.toInt(),
                    sharpenSupportElem.findRequiredUniqueElementWithName("scan:Step").textContent.toInt(),
                )
            } catch (NumberFormatException: NumberFormatException) {
                throw IllegalArgumentException("Number in scan:SharpenSupport was not valid")
            }
            return sharpenSupport
        }
    }
}

data class CompressionFactorSupport(
    val min: Int,
    val max: Int,
    val normal: Int,
    val step: Int,
) {
    companion object {
        fun fromXMLElement(compressionFactorSupportElem: Element): CompressionFactorSupport {
            val compressionFactorSupport: CompressionFactorSupport
            try {
                compressionFactorSupport = CompressionFactorSupport(
                    compressionFactorSupportElem.findRequiredUniqueElementWithName("scan:Min").textContent.toInt(),
                    compressionFactorSupportElem.findRequiredUniqueElementWithName("scan:Max").textContent.toInt(),
                    compressionFactorSupportElem.findRequiredUniqueElementWithName("scan:Normal").textContent.toInt(),
                    compressionFactorSupportElem.findRequiredUniqueElementWithName("scan:Step").textContent.toInt(),
                )
            } catch (NumberFormatException: NumberFormatException) {
                throw IllegalArgumentException("Number in scan:SharpenSupport was not valid")
            }
            return compressionFactorSupport
        }
    }
}

enum class ColorMode {
    BlackAndWhite1,

    /// 8-bit grayscale
    Grayscale8,

    /// 16-bit grayscale
    Grayscale16,

    /// 8-bit per channel RGB
    RGB24,

    /// 16-bit per channel RGB
    RGB48,
}

data class Certfication(
    val name: String,
    val version: String
) {
    companion object {
        fun fromXMLElement(certElem: Element): Certfication {
            val name = certElem.findRequiredUniqueElementWithName("scan:Name").textContent
            val certVersion = certElem.findRequiredUniqueElementWithName("scan:Version").textContent
            if (name.isEmpty() || certVersion.isEmpty()) throw IllegalArgumentException("Malformed Certfication: name or version's length is 0")
            if (!certVersion.matches(Regex("[0-9]+.[0-9]+"))) throw IllegalArgumentException("Malformed Certification version '$certVersion'")
            return Certfication(name, certVersion)
        }

        fun certListFromXMLElement(certificationsElem: Element): List<Certfication>? {
            val certifications: MutableList<Certfication>?
            if (certificationsElem.getElementsByTagName("scan:Certification").length > 0) {
                val certificationElems = certificationsElem.getElementsByTagName("scan:Certification")
                certifications = mutableListOf()
                for (i in 0..<certificationElems.length) {
                    certifications.add(fromXMLElement(certificationElems.item(i) as Element))
                }
                return certifications
            } else return null
        }
    }
}

data class StoredJobRequestSupport(
    val maxStoredJobRequests: UInt,
    val timoutInSeconds: UInt,
) {
    companion object {
        fun fromXMLElement(storedJobRequestSupportElem: Element): StoredJobRequestSupport {
            val sjrs: StoredJobRequestSupport
            try {
                sjrs = StoredJobRequestSupport(
                    storedJobRequestSupportElem.findRequiredUniqueElementWithName("scan:MaxStoredjobRequests").textContent.toUInt(),
                    storedJobRequestSupportElem.findRequiredUniqueElementWithName("scan:TimeoutInSeconds").textContent.toUInt()
                )
            } catch (exception: NumberFormatException) {
                throw IllegalArgumentException("StoredJobRequestSupport: UInt was not valid: ${exception.message}")
            }
            return sjrs
        }
    }
}


data class ScannerCapabilities @OptIn(ExperimentalUuidApi::class) constructor(
    val interfaceVersion: String,
    val makeAndModel: String,
    val manufacturer: String? = null,
    /** Conforming to RFC 4122, has to match mDNS "UUID" txt record **/
    val deviceUuid: Uuid,
    val serialNumber: String,
    val adminURI: String,
    val iconURI: String,
    val certifications: List<Certfication>?,
    val platen: Platen?,
    val adf: Adf?,
    val supportedMediaTypes: List<String>?,
    val sharpenSupport: SharpenSupport?,
    val compressionFactorSupport: CompressionFactorSupport?,
    val storedJobRequestSupport: StoredJobRequestSupport?,
) {
    companion object {
        @OptIn(ExperimentalUuidApi::class)
        fun fromXML(xml: InputStream): ScannerCapabilities {
            // Parsing body
            val documentBuilderFactory = DocumentBuilderFactory.newInstance()
            val docBuilder = documentBuilderFactory.newDocumentBuilder()
            val parsedDoc = docBuilder.parse(xml)
            parsedDoc.documentElement.normalize()
            val xmlRoot = parsedDoc.documentElement

            if (xmlRoot.tagName != "scan:ScannerCapabilities") throw IllegalArgumentException("Malformed ScannerCapabilities: root tag not 'scan:ScannerCapabilities'")

            val version = xmlRoot.findRequiredUniqueElementWithName("pwg:Version").textContent
            if (!version.matches(Regex("[0-9]+.[0-9]+"))) throw IllegalArgumentException("Malformed ScannerCapabilities version '$version'")

            val makeAndModel = xmlRoot.findRequiredUniqueElementWithName("pwg:MakeAndModel").textContent
            if (makeAndModel.length > 127) throw IllegalArgumentException("Malformed ScannerCapabilities MakeAndModel '$makeAndModel' is not too long")

            val manufacturer = xmlRoot.findUniqueElementWithName("scan:Manufacturer")?.textContent

            val serialNumber = xmlRoot.findRequiredUniqueElementWithName("pwg:SerialNumber").textContent
            val printerUUID = Uuid.parse(xmlRoot.findRequiredUniqueElementWithName("scan:UUID").textContent)
            val adminURI = xmlRoot.findRequiredUniqueElementWithName("scan:AdminURI").textContent
            val iconURI = xmlRoot.findRequiredUniqueElementWithName("scan:IconURI").textContent

            val certifications = xmlRoot.findUniqueElementWithName("scan:Certifications")?.let {
                Certfication.certListFromXMLElement(it)
            }

            val platenElem = xmlRoot.findRequiredUniqueElementWithName("scan:Platen")
            val platen = Platen.fromXMLElement(platenElem)

            val adfElem = xmlRoot.findUniqueElementWithName("scan:Adf")
            val adf = adfElem?.let { Adf.fromXMLElement(it) }

            val supportedMediaTypes =
                xmlRoot.findUniqueElementWithName("scan:SupportedMediaTypes")?.getElementsByTagName("scan:MediaType")
                    ?.let {
                        val mediaTypesList: MutableList<String> = mutableListOf()
                        for (i in 0..<it.length) {
                            mediaTypesList.add(it.item(i).textContent)
                        }
                        mediaTypesList
                    }
            val compressionFactorSupport = xmlRoot.findUniqueElementWithName("scan:CompressionFactorSupport")?.let {
                CompressionFactorSupport.fromXMLElement(it)
            }
            val sharpenSupport = xmlRoot.findUniqueElementWithName("scan:SharpenSupport")?.let {
                SharpenSupport.fromXMLElement(it)
            }
            val storedJobRequestSupport = xmlRoot.findUniqueElementWithName("scan:StoredJobRequestSupport")?.let {
                StoredJobRequestSupport.fromXMLElement(it)
            }

            // Check for addtional illegal elements
            val reservedNames = listOf(
                "pwg:Version",
                "pwg:MakeAndModel",
                "pwg:SerialNumber",
                "scan:Manufacturer",
                "scan:UUID",
                "scan:AdminURI",
                "scan:IconURI",
                "scan:Certifications",
                "scan:Platen",
                "scan:Adf",
                "scan:Camera",
                "scan:SupportedMediaTypes",
                "scan:CompressionFactorSupport",
                "scan:SharpenSupport",
                "scan:StoredJobRequestSupport",
                "scan:MaxJobNameLength",
                "scan:BlankPageDetection",
                "scan:BlankPageDetectionAndRemoval"
            )
            for (i in 0..<xmlRoot.childNodes.length) {
                val currentNode = xmlRoot.childNodes.item(i)
                if (currentNode.nodeType != ELEMENT_NODE) continue
                val currentElement = currentNode as Element

                if (!reservedNames.contains(currentElement.tagName)) throw NotImplementedError("The toplevel element with ${currentElement.tagName} is not recognized. Please file an issue with the ScannerCapabilites which cause this!")
            }

            return ScannerCapabilities(
                interfaceVersion = version,
                makeAndModel = makeAndModel,
                manufacturer = manufacturer,
                serialNumber = serialNumber,
                deviceUuid = printerUUID,
                adminURI = adminURI,
                iconURI = iconURI,
                certifications = certifications,
                platen = platen,
                adf = adf,
                supportedMediaTypes = supportedMediaTypes,
                compressionFactorSupport = compressionFactorSupport,
                sharpenSupport = sharpenSupport,
                storedJobRequestSupport = storedJobRequestSupport
            )
        }
    }
}