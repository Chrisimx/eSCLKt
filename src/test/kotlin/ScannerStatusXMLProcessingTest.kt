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
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ScannerStatusXMLProcessingTest {
    @Test
    fun parseScannerStatus() {
        javaClass.getResource("/testResources/status/example1.xml")!!.openStream().use {
            val scannerStatus =
                ScannerStatus.fromXML(it)
            assertEquals(
                ScannerStatus(
                    version = "2.62",
                    state = ScannerState.Idle,
                    jobs = listOf(),
                    adfState = AdfState.ScannerAdfEmpty,
                ),
                scannerStatus,
            )
            println("ScannerStatus Deserialization example 1 returned:\n   $scannerStatus")
        }
        javaClass.getResource("/testResources/status/example2.xml")!!.openStream().use {
            val scannerStatus = ScannerStatus.fromXML(it)
            assertEquals(
                ScannerStatus(
                    version = "2.6",
                    state = ScannerState.Processing,
                    jobs = listOf(
                        JobInfo(
                            "/ScanJobs/893e6fcd-487f-4056-a8c9-a87709b85daf",
                            "893e6fcd-487f-4056-a8c9-a87709b85daf",
                            10u,
                            1u,
                            1u,
                            29u,
                            JobState.Processing,
                            "JobScanning"
                        ),
                        JobInfo(
                            "/ScanJobs/898d6fcd-487f-4056-a8c9-a87709b85daf",
                            "898d6fcd-487f-4056-a8c9-a87709b85daf",
                            220u,
                            5u,
                            0u,
                            null,
                            JobState.Completed,
                            "JobCompletedSuccessfully"
                        )
                    )
                ),
                scannerStatus
            )
            println(
                "ScannerStatus Deserialization example 2 returned:\n   ${
                    scannerStatus
                }",
            )
        }
        javaClass.getResource("/testResources/status/HPColorLaserjetMFPM283fdw.xml")!!.openStream().use {
            println(
                "ScannerStatus Deserialization example 3 (HP Color Laserjet MFPM283fdw) returned:\n   ${
                    ScannerStatus.fromXML(it)
                }",
            )
        }
    }
}
