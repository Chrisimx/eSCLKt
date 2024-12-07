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

import io.github.chrisimx.esclkt.*
import kotlinx.serialization.encodeToString
import nl.adaptivity.xmlutil.serialization.XML
import org.junit.jupiter.api.Test

class ScanSettingsXMLProcessingTest {
    @Test
    fun createScanRequest() {
        val xml = XML {
        }
        val testScanSettings = ScanSettings(
            version = "2.63",
            intent = ScanIntentData.ScanIntentEnum(ScanIntent.Document),
            scanRegions = ScanRegions(listOf(ScanRegion(20u, 10u, 2u, 1u)), true),
            documentFormatExt = "image/jpeg",
            contentType = ContentType.Text,
            inputSource = InputSource.Platen,
            xResolution = 600u,
            yResolution = 200u,
            colorMode = ColorMode.RGB24,
            colorSpace = "sRGB",
            )
        println("ScanSettings Serialization returned:\n   ${xml.encodeToString<ScanSettings>(testScanSettings)}")
    }
}