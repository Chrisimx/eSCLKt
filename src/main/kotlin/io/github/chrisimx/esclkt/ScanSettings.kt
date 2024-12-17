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
import nl.adaptivity.xmlutil.serialization.XmlNamespaceDeclSpec
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("pwg:ScanRegion")
data class ScanRegion(
    @XmlElement
    @XmlSerialName("pwg:Height")
    val height: ThreeHundredthsOfInch,
    @XmlElement
    @XmlSerialName("pwg:Width")
    val width: ThreeHundredthsOfInch,
    @XmlElement
    @XmlSerialName("pwg:XOffset")
    val xOffset: ThreeHundredthsOfInch,
    @XmlElement
    @XmlSerialName("pwg:YOffset")
    val yOffset: ThreeHundredthsOfInch,

    ) {
    @XmlElement
    @XmlSerialName("pwg:ContentRegionUnits")
    private val contentRegionsUnits: String = "escl:ThreeHundredthsOfInches"
}

@Serializable
@XmlSerialName("pwg:ScanRegions")
data class ScanRegions(
    @XmlElement
    val regions: List<ScanRegion>,
    @XmlSerialName("pwg:MustHonor")
    val mustHonor: Boolean = true,
)

@Serializable
@XmlSerialName("pwg:InputSource")
enum class InputSource {
    /** Glass flat bed **/
    Platen,

    /** ADF - Automatic Document Feede **/
    Feeder,

    /** Non-classical camera based scanning **/
    Camera,
}

@Serializable
enum class BinaryRendering {
    Halftone,
    Threshold
}

@Serializable
enum class FeedDirection {
    LongEdgeFeed,
    ShortEdgeFeed,
}

@OptIn(ExperimentalXmlUtilApi::class)
@Serializable
@XmlSerialName(value = "scan:ScanSettings")
@XmlNamespaceDeclSpec("pwg=http://www.pwg.org/schemas/2010/12/sm;scan=http://schemas.hp.com/imaging/escl/2011/05/03")
data class ScanSettings(
    @XmlElement
    @XmlSerialName("pwg:Version")
    val version: String,
    val intent: ScanIntentData? = null,
    @XmlElement
    val scanRegions: ScanRegions? = null,
    @XmlElement
    @XmlSerialName("scan:DocumentFormatExt")
    val documentFormatExt: String? = null,
    @XmlElement
    val contentType: ContentType? = null,
    @XmlElement
    val inputSource: InputSource? = null,
    /** Specified in DPI **/
    @XmlElement
    @XmlSerialName("scan:XResolution")
    val xResolution: UInt? = null,
    /** Specified in DPI **/
    @XmlElement
    @XmlSerialName("scan:YResolution")
    val yResolution: UInt? = null,
    @XmlElement
    @XmlSerialName("scan:ColorMode")
    val colorMode: ColorMode? = null,
    @XmlElement
    @XmlSerialName("scan:ColorSpace")
    val colorSpace: String? = null,
    @XmlElement
    @XmlSerialName("scan:MediaType")
    val mediaType: String? = null,
    @XmlElement
    @XmlSerialName("scan:CcdChannel")
    val ccdChannel: CcdChannel? = null,
    @XmlElement
    @XmlSerialName("scan:BinaryRendering")
    val binaryRendering: BinaryRendering? = null,
    @XmlElement
    @XmlSerialName("scan:Duplex")
    val duplex: Boolean? = null,
    @XmlElement
    @XmlSerialName("scan:NumberOfPages")
    val numberOfPages: UInt? = null,
    @XmlElement
    @XmlSerialName("scan:Brightness")
    val brightness: UInt? = null,
    @XmlElement
    @XmlSerialName("scan:CompressionFactor")
    val compressionFactor: UInt? = null,
    @XmlElement
    @XmlSerialName("scan:Contrast")
    val contrast: UInt? = null,
    @XmlElement
    @XmlSerialName("scan:Gamma")
    val gamma: UInt? = null,
    @XmlElement
    @XmlSerialName("scan:Highlight")
    val highlight: UInt? = null,
    @XmlElement
    @XmlSerialName("scan:NoiseRemoval")
    val noiseRemoval: UInt? = null,
    @XmlElement
    @XmlSerialName("scan:Shadow")
    val shadow: UInt? = null,
    @XmlElement
    @XmlSerialName("scan:Sharpen")
    val sharpen: UInt? = null,
    @XmlElement
    @XmlSerialName("scan:Threshold")
    val threshold: UInt? = null,
    /** As per spec:  "opaque information relayed by the client." **/
    @XmlElement
    @XmlSerialName("scan:ContextID")
    val contextID: String? = null,
    // val scanDestinations: HTTPDestination?, omitted as no known scanner supports this
    @XmlElement
    @XmlSerialName("scan:BlankPageDetection")
    val blankPageDetection: Boolean? = null,
    @XmlElement
    @XmlSerialName("scan:FeedDirection")
    val feedDirection: FeedDirection? = null,
    @XmlElement
    @XmlSerialName("scan:BlankPageDetectionAndRemoval")
    val blankPageDetectionAndRemoval: Boolean? = null,
)