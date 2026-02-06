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

import com.goncalossilva.resources.Resource
import io.github.chrisimx.esclkt.*
import io.github.chrisimx.esclkt.ESCLRequestClient.ScannerCapabilitiesResult
import io.kotest.assertions.fail
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.http.*
import kotlinx.coroutines.delay

class RealScannerTest : FunSpec({
    test("Full scanning process test") {
        val scannerURLRes: Resource = Resource("testResources/scanner_url.txt")
        if (!scannerURLRes.exists()) {
            println("Functional test with real scanner did not run because testResources/scanner_url.txt does not exist")
            return@test
        }
        val scannerURL = scannerURLRes.readText().trim()

        val shouldAfdBeTested = Resource("testResources/test_adf").exists()

        val testedESCLClient = ESCLRequestClient(Url(scannerURL))

        val scannerCapabilitiesResult = testedESCLClient.getScannerCapabilities()
        println("Retrieved scanner capabilities: $scannerCapabilitiesResult")

        scannerCapabilitiesResult.shouldBeInstanceOf<ScannerCapabilitiesResult.Success>()

        val scannerCapabilities = scannerCapabilitiesResult.scannerCapabilities

        val scannerStatus = testedESCLClient.getScannerStatus()
        println("Retrieved scanner status: $scannerStatus")
        scannerStatus.shouldBeInstanceOf<ESCLRequestClient.ScannerStatusResult.Success>()

        executeScanJob(
            testedESCLClient,
            scannerCapabilitiesResult,
            inputSource = InputSource.Platen,
            duplex = false,
            scanRegion =
                ScanRegion(
                    height = scannerCapabilities.platen!!.inputSourceCaps.maxHeight,
                    width = scannerCapabilities.platen.inputSourceCaps.maxWidth,
                    xOffset = 0u.threeHundredthsOfInch(),
                    yOffset = 0u.threeHundredthsOfInch(),
                ),
            fileNamePrefix = "platen_scan",
        )

        if (shouldAfdBeTested) {
            scannerCapabilities.adf shouldNotBe null

            val duplexSupported = scannerCapabilities.adf!!.duplexCaps != null
            val inputSourceCaps =
                if (duplexSupported) scannerCapabilities.adf.duplexCaps else scannerCapabilities.adf.simplexCaps

            executeScanJob(
                testedESCLClient,
                scannerCapabilitiesResult,
                inputSource = InputSource.Feeder,
                duplex = duplexSupported,
                scanRegion =
                    ScanRegion(
                        height = inputSourceCaps.maxHeight,
                        width = inputSourceCaps.maxWidth,
                        xOffset = 0u.threeHundredthsOfInch(),
                        yOffset = 0u.threeHundredthsOfInch(),
                    ),
                fileNamePrefix = "feeder_scan",
            )
        }
    }

    // According to spec this should use the maximum size available, when the regions dimensions are over limits. It should not error
    test("Test what happens if you specificy extremely large size for scanning job") {
        val scannerURLRes: Resource = Resource("testResources/scanner_url.txt")
        if (!scannerURLRes.exists()) {
            println("Functional test with real scanner did not run because testResources/scanner_url.txt does not exist")
            return@test
        }
        val scannerURL = scannerURLRes.readText().trim()

        val testedESCLClient = ESCLRequestClient(Url(scannerURL))

        val scannerCapabilitiesResult = testedESCLClient.getScannerCapabilities()
        println("Retrieved scanner capabilities: $scannerCapabilitiesResult")
        scannerCapabilitiesResult.shouldBeInstanceOf<ScannerCapabilitiesResult.Success>()

        val scannerStatus = testedESCLClient.getScannerStatus()
        println("Retrieved scanner status: $scannerStatus")
        scannerStatus.shouldBeInstanceOf<ESCLRequestClient.ScannerStatusResult.Success>()

        executeScanJob(
            testedESCLClient,
            scannerCapabilitiesResult,
            inputSource = InputSource.Platen,
            duplex = false,
            scanRegion =
                ScanRegion(
                    height = 300000.threeHundredthsOfInch(),
                    width = 300000.threeHundredthsOfInch(),
                    xOffset = 0u.threeHundredthsOfInch(),
                    yOffset = 0u.threeHundredthsOfInch(),
                ),
            fileNamePrefix = "platen_scan",
        )
    }
})

suspend fun executeScanJob(
    testedESCLClient: ESCLRequestClient,
    scannerCapabilitiesResult: ScannerCapabilitiesResult.Success,
    inputSource: InputSource,
    duplex: Boolean,
    scanRegion: ScanRegion,
    fileNamePrefix: String,
) {
    val scanJob =
        testedESCLClient.createJob(
            ScanSettings(
                version = scannerCapabilitiesResult.scannerCapabilities.interfaceVersion,
                intent = EnumOrRaw.Known(ScanIntent.Document),
                inputSource = inputSource,
                colorMode = ColorMode.RGB24,
                scanRegions =
                    ScanRegions(
                        listOf(
                            scanRegion,
                        ),
                        mustHonor = true,
                    ),
                xResolution = 600u,
                yResolution = 600u,
                duplex = duplex,
                documentFormatExt = "image/jpeg",
            ),
        )
    println("ScanJob result: $scanJob")
    scanJob.shouldBeInstanceOf<ESCLRequestClient.ScannerCreateJobResult.Success>()

    val scanJobStatus = scanJob.scanJob.getJobStatus()
    println("ScanJob status: $scanJobStatus")

    var counter = 0
    while (true) {
        val pageRequest = scanJob.scanJob.retrieveNextPage()

        println("page request result: $pageRequest")

        if (pageRequest is ESCLRequestClient.ScannerNextPageResult.Success) {
            println("Received one page")
            maybeSaveDebugFile("page${counter}", pageRequest.page.data)
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
            delay(3000)
        } else if (pageRequest is ESCLRequestClient.ScannerNextPageResult.RequestFailure) {
            println("Can't retrieve nextPage now. Fails with ${pageRequest}")
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
            fail("Unexpected JobStatus after Failure") // This should usually not happen
            delay(3000)
        }
    }
    scanJob.scanJob.cancle()
}

expect fun maybeSaveDebugFile(fileName: String, data: ByteArray)

