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

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlNamespaceDeclSpecs
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@XmlSerialName("ScanRegion", NS_PWG, "pwg")
data class ScanRegion(
    @XmlSerialName("Height", NS_PWG, "pwg")
    val height: ThreeHundredthsOfInch,
    @XmlSerialName("Width", NS_PWG, "pwg")
    val width: ThreeHundredthsOfInch,
    @XmlSerialName("XOffset", NS_PWG, "pwg")
    val xOffset: ThreeHundredthsOfInch,
    @XmlSerialName("YOffset", NS_PWG, "pwg")
    val yOffset: ThreeHundredthsOfInch,
) {
    @XmlSerialName("ContentRegionUnits", NS_PWG, "pwg")
    private val contentRegionUnits: String = "escl:ThreeHundredthsOfInches"

}


sealed class ScanRegionLength {
    data class DiscreteLength(val length: LengthUnit) : ScanRegionLength()
    data object Max : ScanRegionLength()
}

class ScanRegionBuilder(
    private val inputSourceCaps: InputSourceCaps? = null
) {
    private var _height: ScanRegionLength = ScanRegionLength.Max
    private var _width: ScanRegionLength = ScanRegionLength.Max
    var xOffset: LengthUnit = 0.threeHundredthsOfInch()
    var yOffset: LengthUnit = 0.threeHundredthsOfInch()

    var height: LengthUnit
        get() = resolve(_height, maxHeight)
        set(value) {
            _height = ScanRegionLength.DiscreteLength(value)
        }

    var width: LengthUnit
        get() = resolve(_width, maxWidth)
        set(value) {
            _width = ScanRegionLength.DiscreteLength(value)
        }

    fun width(value: ScanRegionLength) {
        _width = value
    }

    fun height(value: ScanRegionLength) {
        _height = value
    }

    fun maxWidth() {
        _width = ScanRegionLength.Max
    }

    fun maxHeight() {
        _height = ScanRegionLength.Max
    }

    fun maxSize() {
        _width = ScanRegionLength.Max
        _height = ScanRegionLength.Max
    }

    val maxHeight: LengthUnit
        get() = inputSourceCaps?.maxHeight ?: 30000.threeHundredthsOfInch()

    val maxWidth: LengthUnit
        get() = inputSourceCaps?.maxWidth ?: 30000.threeHundredthsOfInch()

    fun build(): ScanRegions {
        val region =  ScanRegion(
            height = resolve(_height, maxHeight),
            width = resolve(_width, maxWidth),
            xOffset = xOffset.toThreeHundredthsOfInch(),
            yOffset = yOffset.toThreeHundredthsOfInch()
        )
        return ScanRegions(listOf(region))
    }

    private fun resolve(
        value: ScanRegionLength,
        max: LengthUnit
    ): ThreeHundredthsOfInch =
        when (value) {
            is ScanRegionLength.DiscreteLength -> value.length.toThreeHundredthsOfInch()
            ScanRegionLength.Max -> max.toThreeHundredthsOfInch()
        }
}

fun scanRegion(
    inputSourceCaps: InputSourceCaps? = null,
    block: ScanRegionBuilder.() -> Unit
): ScanRegions {
    val builder = ScanRegionBuilder(inputSourceCaps)
    builder.block()
    return builder.build()
}

@Serializable
@XmlSerialName("ScanRegions", NS_PWG, "pwg")
data class ScanRegions(
    @XmlValue
    val regions: List<ScanRegion>,
    @XmlElement(false)
    @XmlSerialName("MustHonor", NS_PWG, "pwg")
    val mustHonor: Boolean = true,
)
enum class InputSource {
    /** Glass flat bed **/
    Platen,

    /** ADF - Automatic Document Feede **/
    Feeder,

    /** Non-classical camera based scanning **/
    Camera,
}

enum class BinaryRendering {
    Halftone,
    Threshold,
}

enum class FeedDirection {
    LongEdgeFeed,
    ShortEdgeFeed,
}

@OptIn(ExperimentalXmlUtilApi::class)
@Serializable
@XmlSerialName("ScanSettings", NS_SCAN, "scan")
@XmlNamespaceDeclSpecs("pwg=http://www.pwg.org/schemas/2010/12/sm", "scan=http://schemas.hp.com/imaging/escl/2011/05/03")
data class ScanSettings(
    @XmlSerialName("Version", NS_PWG, "pwg")
    val version: String,
    @XmlSerialName("Intent", NS_SCAN, "scan")
    val intent: ScanIntentEnumOrRaw? = null,
    @XmlSerialName("ScanRegions", NS_PWG, "pwg")
    val scanRegions: ScanRegions? = null,
    @XmlSerialName("DocumentFormat", NS_PWG, "pwg")
    val documentFormat: String? = null,
    @XmlSerialName("DocumentFormatExt", NS_SCAN, "scan")
    val documentFormatExt: String? = null,
    @XmlSerialName("ContentType", NS_PWG, "pwg")
    val contentType: ContentTypeEnumOrRaw? = null,
    @XmlSerialName("InputSource", NS_PWG, "pwg")
    val inputSource: InputSource? = null,
    /** Specified in DPI **/
    @XmlSerialName("XResolution", NS_SCAN, "scan")
    val xResolution: UInt? = null,
    /** Specified in DPI **/
    @XmlSerialName("YResolution", NS_SCAN, "scan")
    val yResolution: UInt? = null,
    @XmlSerialName("ColorMode", NS_SCAN, "scan")
    val colorMode: ColorModeEnumOrRaw? = null,
    @XmlSerialName("ColorSpace", NS_SCAN, "scan")
    val colorSpace: String? = null,
    @XmlSerialName("MediaType", NS_SCAN, "scan")
    val mediaType: String? = null,
    @XmlSerialName("CcdChannel", NS_SCAN, "scan")
    val ccdChannel: CcdChannelEnumOrRaw? = null,
    @XmlSerialName("BinaryRendering", NS_SCAN, "scan")
    val binaryRendering: BinaryRendering? = null,
    @XmlSerialName("Duplex", NS_SCAN, "scan")
    val duplex: Boolean? = null,
    @XmlSerialName("NumberOfPages", NS_SCAN, "scan")
    val numberOfPages: UInt? = null,
    @XmlSerialName("Brightness", NS_SCAN, "scan")
    val brightness: UInt? = null,
    @XmlSerialName("CompressionFactor", NS_SCAN, "scan")
    val compressionFactor: UInt? = null,
    @XmlSerialName("Contrast", NS_SCAN, "scan")
    val contrast: UInt? = null,
    @XmlSerialName("Gamma", NS_SCAN, "scan")
    val gamma: UInt? = null,
    @XmlSerialName("Highlight", NS_SCAN, "scan")
    val highlight: UInt? = null,
    @XmlSerialName("NoiseRemoval", NS_SCAN, "scan")
    val noiseRemoval: UInt? = null,
    @XmlSerialName("Shadow", NS_SCAN, "scan")
    val shadow: UInt? = null,
    @XmlSerialName("Sharpen", NS_SCAN, "scan")
    val sharpen: UInt? = null,
    @XmlSerialName("Threshold", NS_SCAN, "scan")
    val threshold: UInt? = null,
    /** Opaque information relayed by the client */
    @XmlSerialName("ContextID", NS_SCAN, "scan")
    val contextID: String? = null,
    // val scanDestinations: HTTPDestination?, omitted as no known scanner supports this
    @XmlSerialName("BlankPageDetection", NS_SCAN, "scan")
    val blankPageDetection: Boolean? = null,
    @XmlSerialName("FeedDirection", NS_SCAN, "scan")
    val feedDirection: FeedDirection? = null,
    @XmlSerialName("BlankPageDetectionAndRemoval", NS_SCAN, "scan")
    val blankPageDetectionAndRemoval: Boolean? = null,
)