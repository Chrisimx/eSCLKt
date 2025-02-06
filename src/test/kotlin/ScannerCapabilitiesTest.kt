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
import kotlin.test.assertEquals
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ScannerCapabilitiesTest {
    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun testScannerCapabilitiesXMLParsing() {
        val resource = javaClass.getResource("/testResources/capabilities/brother-mfc-l8690cdw-caps.xml")!!
        resource.openStream().use {
            assertDoesNotThrow {
                val scannerCapabilities = ScannerCapabilities.fromXML(it)
                assertEquals("2.62", scannerCapabilities.interfaceVersion)
                assertEquals("Brother MFC-L8690CDW series", scannerCapabilities.makeAndModel)
                assertEquals(null, scannerCapabilities.manufacturer)
                assertEquals(Uuid.parse("00000000-0000-1000-8000-0018d7024a10"), scannerCapabilities.deviceUuid)
                assertEquals("http://192.168.200.122/net/net/airprint.html", scannerCapabilities.adminURI)
                assertEquals("http://192.168.200.122/icons/device-icons-128.png", scannerCapabilities.iconURI)
            }
        }
        val resource2 = javaClass.getResource("/testResources/capabilities/brother-mfc-j497dw-caps.xml")!!
        resource2.openStream().use {
            assertDoesNotThrow {
                ScannerCapabilities.fromXML(it)
            }
        }
        val resource3 = javaClass.getResource("/testResources/capabilities/canon-ts5300-series-caps.xml")!!
        resource3.openStream().use {
            assertDoesNotThrow {
                ScannerCapabilities.fromXML(it)
            }
        }
        val resource4 = javaClass.getResource("/testResources/capabilities/canon-ts7450-caps.xml")!!
        resource4.openStream().use {
            assertDoesNotThrow {
                ScannerCapabilities.fromXML(it)
            }
        }
        val resource5 = javaClass.getResource("/testResources/capabilities/hp-deskjet-3630-caps.xml")!!
        resource5.openStream().use {
            assertDoesNotThrow {
                ScannerCapabilities.fromXML(it)
            }
        }
    }
}