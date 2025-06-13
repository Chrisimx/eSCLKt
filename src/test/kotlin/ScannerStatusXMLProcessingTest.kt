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
import java.nio.charset.StandardCharsets
import kotlin.test.assertEquals

class ScannerStatusXMLProcessingTest {
    @Test
    fun createScannerStatus() {
        val xml =
            XML {
            }
        val testScannerStatus =
            ScannerStatus(
                "2.63",
                ScannerState.Processing,
                Jobs(
                    listOf(
                        JobInfo(
                            jobURI =
                                "/ScanJobs/893e6fcd-487f-4056-a8c9-\n" +
                                    "a87709b85daf",
                            jobState = JobState.Pending,
                            jobUUID = "dfasdfdsf",
                            age = 10u,
                            imagesCompleted = 1u,
                        ),
                    ),
                ),
            )
        println("ScannerStatus Serialization returned:\n   ${xml.encodeToString<ScannerStatus>(testScannerStatus)}")
    }

    @Test
    fun parseScannerStatus() {
        val xml =
            XML {
            }

        javaClass.getResource("/testResources/status/example1.xml")!!.openStream().use {
            val scannerStatus =
                xml.decodeFromString(ScannerStatus.serializer(), it.readAllBytes().toString(StandardCharsets.UTF_8))
            assertEquals(
                scannerStatus,
                ScannerStatus(
                    version = "2.62",
                    state = ScannerState.Idle,
                    jobs = null,
                    adfState = AdfState.ScannerAdfEmpty,
                ),
            )
            println("ScannerStatus Deserialization example 1 returned:\n   $scannerStatus")
        }
        javaClass.getResource("/testResources/status/example2.xml")!!.openStream().use {
            println(
                "ScannerStatus Deserialization example 2 returned:\n   ${
                    xml.decodeFromString(
                        ScannerStatus.serializer(),
                        it.readAllBytes().toString(StandardCharsets.UTF_8),
                    )
                }",
            )
        }
        javaClass.getResource("/testResources/status/HPColorLaserjetMFPM283fdw.xml")!!.openStream().use {
            val status = it.readAllBytes().toString(StandardCharsets.UTF_8)
            println(
                "ScannerStatus Deserialization example 3 (HP Color Laserjet MFPM283fdw) returned:\n   ${
                    xml.decodeFromString(
                        ScannerStatus.serializer(),
                        status,
                    )
                }",
            )
        }
    }
}
