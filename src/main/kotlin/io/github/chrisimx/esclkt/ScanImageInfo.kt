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

import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class ScanImageInfo
    @OptIn(ExperimentalUuidApi::class)
    constructor(
        val jobURI: String,
        val jobUuid: Uuid,
        val actualWidth: UInt,
        val actualHeight: UInt,
        val actualBytesPerLine: UInt,
        val blankPageDetected: Boolean?,
    ) {
        companion object {
            @OptIn(ExperimentalUuidApi::class)
            fun fromXML(xml: InputStream): ScanImageInfo {
                val documentBuilderFactory = DocumentBuilderFactory.newInstance()
                val docBuilder = documentBuilderFactory.newDocumentBuilder()
                val parsedDoc = docBuilder.parse(xml)
                parsedDoc.documentElement.normalize()
                val xmlRoot = parsedDoc.documentElement

                if (xmlRoot.tagName !=
                    "scan:ScanImageInfo"
                ) {
                    throw IllegalArgumentException("Malformed ScanImageInfo: root tag not 'scan:ScanImageInfo'")
                }

                val jobUri = xmlRoot.findRequiredUniqueElementWithName("pwg:JobUri").textContent
                val jobUUID = Uuid.parse(xmlRoot.findRequiredUniqueElementWithName("pwg:JobUuid").textContent)
                val actualWidth = xmlRoot.findRequiredUniqueElementWithName("scan:ActualWidth").textContent.toUInt()
                val actualHeight = xmlRoot.findRequiredUniqueElementWithName("scan:ActualHeight").textContent.toUInt()
                val actualBytesPerLine = xmlRoot.findRequiredUniqueElementWithName("scan:ActualBytesPerLine").textContent.toUInt()
                val blankPageDetected = xmlRoot.findUniqueElementWithName("scan:BlankPageDetected")?.textContent.toBoolean()

                return ScanImageInfo(jobUri, jobUUID, actualWidth, actualHeight, actualBytesPerLine, blankPageDetected)
            }
        }
    }
