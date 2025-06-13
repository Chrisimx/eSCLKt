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

import io.github.chrisimx.esclkt.ScannerCapabilities
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ScannerCapabilitiesTest {
    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun testScannerCapabilitiesXMLParsing() {
        val resource = javaClass.getResource("/testResources/capabilities/brother-mfc-l8690cdw-caps.xml")!!
        resource.openStream().use {
            assertDoesNotThrow("Failed to parse brother-mfc-l8690cdw-caps.xml") {
                val scannerCapabilities = ScannerCapabilities.fromXML(it)
                assertEquals("2.62", scannerCapabilities.interfaceVersion)
                assertEquals("Brother MFC-L8690CDW series", scannerCapabilities.makeAndModel)
                assertEquals(null, scannerCapabilities.manufacturer)
                assertEquals(Uuid.parse("00000000-0000-1000-8000-0018d7024a10"), scannerCapabilities.deviceUuid)
                assertEquals("http://192.168.200.122/net/net/airprint.html", scannerCapabilities.adminURI)
                assertEquals("http://192.168.200.122/icons/device-icons-128.png", scannerCapabilities.iconURI)
            }
        }
        val capabilitiesDir = javaClass.getResource("/testResources/capabilities")!!
        val capabilitiesPath = Paths.get(capabilitiesDir.toURI())
        Files
            .walk(capabilitiesPath)
            .filter { it.toString().endsWith("-caps.xml") }
            .forEach { path ->
                javaClass.getResourceAsStream("/testResources/capabilities/${path.fileName}")?.use {
                    assertDoesNotThrow("Failed to parse ${path.fileName}") {
                        ScannerCapabilities.fromXML(it)
                    }
                }
            }
    }
}
