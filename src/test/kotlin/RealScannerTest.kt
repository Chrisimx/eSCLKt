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
import io.github.chrisimx.esclkt.ESCLRequestClient.ScannerCapabilitiesResult
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.File
import java.lang.Thread.sleep
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.test.Test
import kotlin.test.assertTrue

class RealScannerTest {


    @Test
    fun testScanFull() {
        val scannerURL: String? = System.getenv("TEST_SCANNER_URL")
        val shouldAfdBeTested: Boolean = System.getenv("TEST_FEEDER").toBoolean()
        if (scannerURL == null) {
            println("Functional test with real scanner did not run because TEST_SCANNER_URL is not specified")
            return
        }

        File(".").listFiles()?.forEach { it.delete() }

        val testedESCLClient = ESCLRequestClient(scannerURL.toHttpUrl())

        val scannerCapabilitiesResult = testedESCLClient.getScannerCapabilities()
        println("Retrieved scanner capabilities: $scannerCapabilitiesResult")
        assertTrue(scannerCapabilitiesResult is ScannerCapabilitiesResult.Success)

        val scannerCapabilities = scannerCapabilitiesResult.scannerCapabilities

        val scannerStatus = testedESCLClient.getScannerStatus()
        println("Retrieved scanner status: $scannerStatus")
        assertTrue(scannerStatus is ESCLRequestClient.ScannerStatusResult.Success)

        executeScanJob(
            testedESCLClient, scannerCapabilitiesResult,
            inputSource = InputSource.Platen,
            duplex = false,
            scanRegion = ScanRegion(
                height = scannerCapabilities.platen!!.inputSourceCaps.maxHeight,
                width = scannerCapabilities.platen!!.inputSourceCaps.maxWidth,
                xOffset = 0u.threeHundredthsOfInch(),
                yOffset = 0u.threeHundredthsOfInch()
            ),
            fileNamePrefix = "platen_scan"
        )

        if (shouldAfdBeTested) {
            assert(scannerCapabilities.adf != null)

            val duplexSupported = scannerCapabilities.adf!!.duplexCaps != null
            val inputSourceCaps =
                if (duplexSupported) scannerCapabilities.adf!!.duplexCaps!! else scannerCapabilities.adf!!.simplexCaps

            executeScanJob(
                testedESCLClient, scannerCapabilitiesResult,
                inputSource = InputSource.Feeder,
                duplex = duplexSupported,
                scanRegion = ScanRegion(
                    height = inputSourceCaps.maxHeight,
                    width = inputSourceCaps.maxWidth,
                    xOffset = 0u.threeHundredthsOfInch(),
                    yOffset = 0u.threeHundredthsOfInch()
                ),
                fileNamePrefix = "feeder_scan"
            )
        }
    }

    // According to spec this should use the maximum size available, when the regions dimensions are over limits. It should not error
    @Test
    fun regionOutOfRange() {
        val scannerURL: String? = System.getenv("TEST_SCANNER_URL")
        if (scannerURL == null) {
            println("Functional test with real scanner did not run because TEST_SCANNER_URL is not specified")
            return
        }

        File(".").listFiles()?.forEach { it.delete() }

        val testedESCLClient = ESCLRequestClient(scannerURL.toHttpUrl())

        val scannerCapabilitiesResult = testedESCLClient.getScannerCapabilities()
        println("Retrieved scanner capabilities: $scannerCapabilitiesResult")
        assertTrue(scannerCapabilitiesResult is ScannerCapabilitiesResult.Success)

        val scannerCapabilities = scannerCapabilitiesResult.scannerCapabilities

        val scannerStatus = testedESCLClient.getScannerStatus()
        println("Retrieved scanner status: $scannerStatus")
        assertTrue(scannerStatus is ESCLRequestClient.ScannerStatusResult.Success)

        executeScanJob(
            testedESCLClient, scannerCapabilitiesResult,
            inputSource = InputSource.Platen,
            duplex = false,
            scanRegion = ScanRegion(
                height = 300000.threeHundredthsOfInch(),
                width = 300000.threeHundredthsOfInch(),
                xOffset = 0u.threeHundredthsOfInch(),
                yOffset = 0u.threeHundredthsOfInch()
            ),
            fileNamePrefix = "platen_scan"
        )
    }

    private fun executeScanJob(
        testedESCLClient: ESCLRequestClient,
        scannerCapabilitiesResult: ScannerCapabilitiesResult.Success,
        inputSource: InputSource,
        duplex: Boolean,
        scanRegion: ScanRegion,
        fileNamePrefix: String
    ) {
        val scanJob = testedESCLClient.createJob(
            ScanSettings(
                version = scannerCapabilitiesResult.scannerCapabilities.interfaceVersion,
                intent = ScanIntentData.ScanIntentEnum(ScanIntent.Document),
                inputSource = inputSource,
                colorMode = ColorMode.RGB24,
                scanRegions = ScanRegions(
                    listOf(
                        scanRegion
                    ), mustHonor = true
                ),
                xResolution = 600u,
                yResolution = 600u,
                duplex = duplex,
                documentFormatExt = "image/jpeg"
            )
        )
        println("ScanJob result: $scanJob")
        assertTrue(scanJob is ESCLRequestClient.ScannerCreateJobResult.Success)

        val scanJobStatus = scanJob.scanJob.getJobStatus()
        println("ScanJob status: $scanJobStatus")

        var counter = 0
        while (true) {
            val pageRequest = scanJob.scanJob.retrieveNextPage()

            println("page request result: $pageRequest")

            if (pageRequest is ESCLRequestClient.ScannerNextPageResult.Success) {
                println("Received one page")
                pageRequest.page.data.use {
                    Files.copy(
                        it.body!!.byteStream(),
                        Path.of("$fileNamePrefix$counter.jpg"),
                        StandardCopyOption.REPLACE_EXISTING
                    )
                }
                val scanImageInfo = scanJob.scanJob.getScanImageInfoForRetrievedPage()
                if (scanImageInfo is ESCLRequestClient.RetrieveScanImageInfoResult.Success) {
                    println("Received ScanImageInfo: $scanImageInfo")
                } else {
                    println("Couldn't get ScanImageInfo: $scanImageInfo")
                }
                counter++
            } else if (pageRequest is ESCLRequestClient.ScannerNextPageResult.NoFurtherPages) {
                println("There seem to be no futher pages. Checking completion with JobStatus")
                val currentJobStatus = scanJob.scanJob.getJobStatus()!!
                val currentState = currentJobStatus.jobState
                val currentStateReasons = currentJobStatus.jobStateReasons
                println("Current state: $currentState $currentStateReasons")
                val jobDoneOrFailed =
                    currentState == JobState.Completed || currentState == JobState.Aborted || currentState == JobState.Canceled
                if (jobDoneOrFailed) {
                    println("Job is done or failed. Current state of job is $currentState $currentStateReasons")
                    break
                }
                sleep(3000)

            } else if (pageRequest is ESCLRequestClient.ScannerNextPageResult.NotSuccessfulCode) {
                println("Can't retrieve nextPage now. Fails with ${pageRequest.responseCode}")
                val currentJobStatus = scanJob.scanJob.getJobStatus()!!
                val currentState = currentJobStatus.jobState
                val currentStateReasons = currentJobStatus.jobStateReasons
                println("Current state: $currentState $currentStateReasons")
                val jobDoneOrFailed =
                    currentState == JobState.Completed || currentState == JobState.Aborted || currentState == JobState.Canceled
                if (jobDoneOrFailed) {
                    println("Job is done or failed. Current state of job is $currentState $currentStateReasons")
                    break
                }
                assert(false) // This should usually not happen
                sleep(3000)
            }
        }
    }

}