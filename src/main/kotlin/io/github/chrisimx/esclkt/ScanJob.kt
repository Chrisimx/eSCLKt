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

import okhttp3.HttpUrl

class ScanJob(
    val jobUri: String,
    val esclClient: ESCLRequestClient,
    val scanSettings: ScanSettings,
) {
    private var isCancelled = false

    constructor(jobUrl: HttpUrl, esclClient: ESCLRequestClient, scanSettings: ScanSettings) : this(
        jobUrl.encodedPath,
        esclClient,
        scanSettings,
    ) {
    }

    /** Tries to cancle the scan job
     * @return A ScannerDeleteJobResult, which shows if the action was successful or not
     * **/
    fun cancle(): ESCLRequestClient.ScannerDeleteJobResult {
        val cancellationResult = esclClient.deleteJob(jobUri)
        if (cancellationResult is ESCLRequestClient.ScannerDeleteJobResult.Success) isCancelled = true
        return cancellationResult
    }

    /** Executes a ScannerStatus request to the scanner, finds the job in the ScannerStatus and returns the current job status as JobInfo
     * @return A JobInfo containing the current status of the job. If null the job could not be retrieved or found in the ScannerStatus
     * **/
    fun getJobStatus(): JobInfo? {
        val scannerStatus = esclClient.getScannerStatus()
        if (scannerStatus !is ESCLRequestClient.ScannerStatusResult.Success) return null

        return scannerStatus.scannerStatus.jobs?.firstOrNull {
            this.jobUri.trimEnd('/') ==
                it.jobURI.trimEnd(
                    '/',
                )
        }
    }

    /** Executes a NextPage request to the scanner to retrieve the next page of the scan job
     * @return A ScannerNextPageResult. If the retrieval was successful this contains a ScannedPage.
     * **/
    fun retrieveNextPage(): ESCLRequestClient.ScannerNextPageResult = esclClient.retrieveNextPageForJob(jobUri)

    /** Gets the ScanImageInfo for the last retrieved page **/
    fun getScanImageInfoForRetrievedPage(): ESCLRequestClient.RetrieveScanImageInfoResult = esclClient.retrieveScanImageInfoForJob(jobUri)

    override fun toString(): String =
        "ScanJob(isCancelled='$isCancelled', jobUri=$jobUri, jobSettings=$scanSettings, esclClient=$esclClient"
}
