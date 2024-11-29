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

import io.github.chrisimx.esclkt.ESCLRequestClient
import io.github.chrisimx.esclkt.ESCLRequestClient.ScannerCapabilitiesResult
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ESCLRequestClientTest {

    @Test
    fun testScannerCapabilitiesRetrieval() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(404))
        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(MockResponse().setResponseCode(503))
        server.enqueue(MockResponse().setResponseCode(200))
        server.enqueue(
            MockResponse().setResponseCode(200).setBody("sfdsf")
                .setHeader("Content-Type", "application/json; charset=utf-8")
        )
        val resource = javaClass.getResource("/testResources/capabilities/brother-mfc-l8690cdw-caps.xml")!!
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "text/xml")
                .setBody(resource.readText(charset = Charsets.UTF_8))
        )
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "text/xml; charset=utf-8")
                .setBody(resource.readText(charset = Charsets.UTF_8))
        )

        server.start()

        val baseUrl = server.url("/eSCL")

        val testedESCLClient = ESCLRequestClient(baseUrl)

        // Fail cases
        assertEquals(
            ScannerCapabilitiesResult.NotSuccessfulCode(404),
            testedESCLClient.getScannerCapabilities()
        )
        assertEquals(
            ScannerCapabilitiesResult.NotSuccessfulCode(500),
            testedESCLClient.getScannerCapabilities()
        )
        assertEquals(
            ScannerCapabilitiesResult.NotSuccessfulCode(503),
            testedESCLClient.getScannerCapabilities()
        )

        assertEquals(
            ScannerCapabilitiesResult.NoBodyReturned,
            testedESCLClient.getScannerCapabilities()
        )
        assertEquals(
            ScannerCapabilitiesResult.WrongContentType,
            testedESCLClient.getScannerCapabilities()
        )

        // Success cases
        assertTrue(
            testedESCLClient.getScannerCapabilities() is ScannerCapabilitiesResult.Success
        )
        assertTrue(
            testedESCLClient.getScannerCapabilities() is ScannerCapabilitiesResult.Success
        )

        server.shutdown()

        assertTrue(testedESCLClient.getScannerCapabilities() is ScannerCapabilitiesResult.NetworkError)
    }

}