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

import io.github.chrisimx.esclkt.XmlHelpers.addTextElement
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.StringWriter
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

data class ScanRegion(
    val height: ThreeHundredthsOfInch,
    val width: ThreeHundredthsOfInch,
    val xOffset: ThreeHundredthsOfInch,
    val yOffset: ThreeHundredthsOfInch,
) {
    private val contentRegionsUnits: String = "escl:ThreeHundredthsOfInches"

    fun toElement(doc: Document): Element {
        val root = doc.createElement("pwg:ScanRegion")
        root.addTextElement("pwg:Height", height.value.toString())
        root.addTextElement("pwg:ContentRegionUnits", contentRegionsUnits)
        root.addTextElement("pwg:Width", width.value.toString())
        root.addTextElement("pwg:XOffset", xOffset.value.toString())
        root.addTextElement("pwg:YOffset", yOffset.value.toString())
        return root
    }
}

data class ScanRegions(
    val regions: List<ScanRegion>,
    val mustHonor: Boolean = true,
) {
    fun toElement(doc: Document): Element {
        val root = doc.createElement("pwg:ScanRegions")
        root.setAttribute("pwg:MustHonor", mustHonor.toString())
        for (region in regions) {
            root.appendChild(region.toElement(doc))
        }
        return root
    }
}

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

data class ScanSettings(
    val version: String,
    val intent: ScanIntentData? = null,
    val scanRegions: ScanRegions? = null,
    val documentFormat: String? = null,
    val documentFormatExt: String? = null,
    val contentType: ContentType? = null,
    val inputSource: InputSource? = null,
    /** Specified in DPI **/
    val xResolution: UInt? = null,
    /** Specified in DPI **/
    val yResolution: UInt? = null,
    val colorMode: ColorMode? = null,
    val colorSpace: String? = null,
    val mediaType: String? = null,
    val ccdChannel: CcdChannel? = null,
    val binaryRendering: BinaryRendering? = null,
    val duplex: Boolean? = null,
    val numberOfPages: UInt? = null,
    val brightness: UInt? = null,
    val compressionFactor: UInt? = null,
    val contrast: UInt? = null,
    val gamma: UInt? = null,
    val highlight: UInt? = null,
    val noiseRemoval: UInt? = null,
    val shadow: UInt? = null,
    val sharpen: UInt? = null,
    val threshold: UInt? = null,
    /** As per spec:  "opaque information relayed by the client." **/
    val contextID: String? = null,
    // val scanDestinations: HTTPDestination?, omitted as no known scanner supports this
    val blankPageDetection: Boolean? = null,
    val feedDirection: FeedDirection? = null,
    val blankPageDetectionAndRemoval: Boolean? = null,
) {
    fun toXMLString(): String {
        val doc = XmlHelpers.newDocument()
        doc.xmlVersion = "1.0"
        doc.xmlStandalone = true

        val root: Element = doc.createElement("scan:ScanSettings")
        root.setAttribute("xmlns:scan", "http://schemas.hp.com/imaging/escl/2011/05/03")
        root.setAttribute("xmlns:pwg", "http://www.pwg.org/schemas/2010/12/sm")
        doc.appendChild(root)

        root.addTextElement("pwg:Version", version)
        root.addTextElement("scan:Intent", intent?.toScanIntentString())
        scanRegions?.toElement(doc)?.let { root.appendChild(it) }
        root.addTextElement("pwg:DocumentFormat", documentFormat)
        root.addTextElement("scan:DocumentFormatExt", documentFormatExt)
        root.addTextElement("pwg:ContentType", contentType?.toString())
        root.addTextElement("pwg:InputSource", inputSource?.toString())
        root.addTextElement("scan:XResolution", xResolution?.toString())
        root.addTextElement("scan:YResolution", yResolution?.toString())
        root.addTextElement("scan:ColorMode", colorMode?.toString())
        root.addTextElement("scan:ColorSpace", colorSpace)
        root.addTextElement("scan:MediaType", mediaType)
        root.addTextElement("scan:CcdChannel", ccdChannel?.toString())
        root.addTextElement("scan:BinaryRendering", binaryRendering?.toString())
        root.addTextElement("scan:Duplex", duplex?.toString())
        root.addTextElement("scan:NumberOfPages", numberOfPages?.toString())
        root.addTextElement("scan:Brightness", brightness?.toString())
        root.addTextElement("scan:CompressionFactor", compressionFactor?.toString())
        root.addTextElement("scan:Contrast", contrast?.toString())
        root.addTextElement("scan:Gamma", gamma?.toString())
        root.addTextElement("scan:Highlight", highlight?.toString())
        root.addTextElement("scan:NoiseRemoval", noiseRemoval?.toString())
        root.addTextElement("scan:Shadow", shadow?.toString())
        root.addTextElement("scan:Sharpen", sharpen?.toString())
        root.addTextElement("scan:Threshold", threshold?.toString())
        root.addTextElement("scan:ContextID", contextID)
        root.addTextElement("scan:BlankPageDetection", blankPageDetection?.toString())
        root.addTextElement("scan:FeedDirection", feedDirection?.toString())
        root.addTextElement("scan:BlankPageDetectionAndRemoval", blankPageDetectionAndRemoval?.toString())

        val transformer = TransformerFactory.newInstance().newTransformer()
        val writer = StringWriter()
        transformer.transform(DOMSource(doc), StreamResult(writer))
        return writer.toString()
    }
}