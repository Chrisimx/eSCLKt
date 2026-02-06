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

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.ContentType
import nl.adaptivity.xmlutil.XmlException

/**
 * A client for the eSCL scanning protocol
 *
 * @param baseUrl The base url to make requests to. Has to be a URL which is derived with "http URL to printer" + "rs" txt record acquired from mDNS
 * @param usedHttpClient eSCL is an HTTP-based protocol. This OkHttpClient will be used to execute requests
 */
class ESCLRequestClient(
    val baseUrl: Url,
    val usedHttpClient: HttpClient = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 100_000
            connectTimeoutMillis = 100_000
            socketTimeoutMillis = 100_000
        }
    }
) {
    private val rootURL: Url = URLBuilder(baseUrl).apply { encodedPath = "/" }.build()
    val esclXml: ESCLXml = ESCLXml()

    sealed class ScannerCapabilitiesResult {
        data class Success(
            val scannerCapabilities: ScannerCapabilities,
        ) : ScannerCapabilitiesResult()

        data class RequestFailure(
            val error: ESCLHttpCallResult.Error,
        ) : ScannerCapabilitiesResult()
        data class ScannerCapabilitiesMalformed(
            val content: String,
            val exception: Exception,
        ) : ScannerCapabilitiesResult()

        data class InternalBug(
            val exception: Exception,
        ) : ScannerCapabilitiesResult()
    }

    /**
     * Executes a ScannerCapabilities request to receive the characteristics of the scanner.
     *
     * This capabilities can include scan inputs (flatbed, AFD), scan size limits, the eSCL version and many other
     * scanner properties, relevant to creating a valid scan job.
     *
     * @return The result of the request as one of the many possible outcomes specified as ScannerCapabilitiesResult
     * @see ScannerCapabilitiesResult
     */
    suspend fun getScannerCapabilities(): ScannerCapabilitiesResult {
        val response = usedHttpClient.safeRequest<String>("${baseUrl}ScannerCapabilities") {
            accept(ContentType.Any)
            expectSuccess = true
        }

        when (response) {
            is ESCLHttpCallResult.Success -> {}
            is ESCLHttpCallResult.Error -> return ScannerCapabilitiesResult.RequestFailure(response)
        }

        val scannerCaps = response.body

        try {
            val scannerCapabilities: ScannerCapabilities = esclXml.decodeFromString<ScannerCapabilities>(scannerCaps)
            return ScannerCapabilitiesResult.Success(scannerCapabilities)
        } catch (e: XmlException) {
            return ScannerCapabilitiesResult.ScannerCapabilitiesMalformed(scannerCaps, e)
        } catch (e: IllegalArgumentException) {
            return ScannerCapabilitiesResult.ScannerCapabilitiesMalformed(scannerCaps, e)
        } catch (e: Exception) {
            return ScannerCapabilitiesResult.InternalBug(e)
        }
    }

    sealed class ScannerStatusResult {
        data class Success(
            val scannerStatus: ScannerStatus,
        ) : ScannerStatusResult()

        data class RequestFailure(
            val exception: ESCLHttpCallResult.Error,
        ) : ScannerStatusResult()

        data class ScannerStatusMalformed(
            val xmlString: String,
            val exception: Exception,
        ) : ScannerStatusResult()

        data class InternalBug(
            val exception: Exception,
        ) : ScannerStatusResult()
    }

    /**
     * Executes a ScannerStatus request to receive the current status of the scanner.
     *
     * This status includes information if the scanner is currently busy or ready to scan pages.
     *
     * @return The result of the request as one of the many possible outcomes specified as ScannerStatusResult
     * @see ScannerStatusResult
     */
    suspend fun getScannerStatus(): ScannerStatusResult {
        val response: ESCLHttpCallResult<String> =
            usedHttpClient.safeRequest<String>("${baseUrl}ScannerStatus") {
                accept(ContentType.Any)
                expectSuccess = true
            }

        when (response) {
            is ESCLHttpCallResult.Success -> {}
            is ESCLHttpCallResult.Error -> return ScannerStatusResult.RequestFailure(response)
        }

        val scannerStatus = response.body

        try {
            val decodedResult = esclXml.decodeFromString<ScannerStatus>(scannerStatus)
            return ScannerStatusResult.Success(decodedResult)
        } catch (e: XmlException) {
            return ScannerStatusResult.ScannerStatusMalformed(scannerStatus, e)
        } catch (e: IllegalArgumentException) {
            return ScannerStatusResult.ScannerStatusMalformed(scannerStatus, e)
        } catch (e: Exception) {
            return ScannerStatusResult.InternalBug(e)
        }
    }

    sealed class ScannerCreateJobResult {
        data class Success(
            val scanJob: ScanJob,
        ) : ScannerCreateJobResult()

        data class RequestFailure(
            val exception: ESCLHttpCallResult.Error,
        ) : ScannerCreateJobResult()

        data class JobUrlBuildingFailed(val exception: Exception) : ScannerCreateJobResult()

        data object NoLocationGiven : ScannerCreateJobResult()
    }

    /**
     * Posts a job to the scanner
     *
     * This returns a ScannerCreateJobResult and when creation is successful, this contains a ScanJob instance which can be used to interact with the created job.
     *
     * @return The result of the request as one of the many possible outcomes specified as ScannerCreateJobResult
     * @see ScannerCreateJobResult
     */
    suspend fun createJob(scanSettings: ScanSettings): ScannerCreateJobResult {
        val requestData = esclXml.xml.encodeToString(ScanSettings.serializer(), scanSettings)

        val req =
            usedHttpClient.safeRequest<String>("${baseUrl}ScanJobs") {
                method = HttpMethod.Post
                expectSuccess = true
                header("Accept", "*/*")
                contentType(ContentType.Text.Xml)
                setBody(requestData)
            }

        when (req) {
            is ESCLHttpCallResult.Success -> {}
            is ESCLHttpCallResult.Error -> return ScannerCreateJobResult.RequestFailure(req)
        }

        val jobURL: Url?
        val finalJobURL: Url?
        val jobLocation: String?
        try {
            jobLocation = req.response.headers["Location"]
            jobURL = jobLocation?.let { Url(it) }
            finalJobURL = jobURL?.let { URLBuilder(jobURL).appendPathSegments("/").build() }
        } catch (e: Exception) {
            return ScannerCreateJobResult.JobUrlBuildingFailed(e)
        }

        // Checking for response errors on HTTP level

        if (jobURL == null) {
            return ScannerCreateJobResult.NoLocationGiven
        }

        return ScannerCreateJobResult.Success(
            if (finalJobURL == null) {
                ScanJob(
                    "$jobLocation/",
                    this,
                    scanSettings,
                )
            } else {
                ScanJob(
                    finalJobURL,
                    this,
                    scanSettings,
                )
            },
        )
    }

    sealed class ScannerDeleteJobResult {
        data object Success : ScannerDeleteJobResult()

        data class CouldNotBuiltJobUrl(val exception: Exception) : ScannerDeleteJobResult()

        data class RequestFailure(
            val exception: ESCLHttpCallResult.Error,
        ) : ScannerDeleteJobResult()
    }

    /**
     * Cancels a job on the scanner
     *
     * This returns a ScannerDeleteJobResult.
     *
     * @param jobUri The so-called JobUri (e.g. "/eSCL/ScanJobs/893e6fcd-487f-4056-a8c9-a87709b85daf")
     * @return The result of the request as one of the many possible outcomes specified as ScannerCreateJobResult
     * @see ScannerDeleteJobResult
     */
    suspend fun deleteJob(jobUri: String): ScannerDeleteJobResult {
        val url: Url
        try {
            val urlBuilder = URLBuilder(rootURL)
            urlBuilder.path(jobUri.removeSuffix("/"))
            url = urlBuilder.build()
        } catch (e: Exception) {
            return ScannerDeleteJobResult.CouldNotBuiltJobUrl(e)
        }

        val req = usedHttpClient.safeRequest<String>(url.toString()) {
                        method = HttpMethod.Delete
                        expectSuccess = true
                    }

        return when (req) {
            is ESCLHttpCallResult.Success -> ScannerDeleteJobResult.Success
            is ESCLHttpCallResult.Error -> ScannerDeleteJobResult.RequestFailure(req)
        }
    }

    data class ScannedPage(
        val contentType: String,
        val contentLocation: String? = null,
        val data: ByteArray,
        val supportRangeHeader: Boolean,
    ) {
        override fun toString(): String {
            return "ScannedPage(contentType='$contentType', contentLocation=$contentLocation, data.size=${data.size}, supportRangeHeader=$supportRangeHeader)"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ScannedPage) return false

            if (supportRangeHeader != other.supportRangeHeader) return false
            if (contentType != other.contentType) return false
            if (contentLocation != other.contentLocation) return false
            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = supportRangeHeader.hashCode()
            result = 31 * result + contentType.hashCode()
            result = 31 * result + (contentLocation?.hashCode() ?: 0)
            result = 31 * result + data.contentHashCode()
            return result
        }
    }

    sealed class ScannerNextPageResult {
        data class Success(
            val page: ScannedPage,
        ) : ScannerNextPageResult()

        data object NoFurtherPages : ScannerNextPageResult()

        data class InvalidJobUri(val exception: Exception) : ScannerNextPageResult()

        data class RequestFailure(
            val exception: ESCLHttpCallResult.Error,
        ) : ScannerNextPageResult()

        data class ContentTypeMissing(
            val responseCode: Int,
            val body: String,
        ) : ScannerNextPageResult()
    }

    /**
     * Retrieves the next page for a job on the scanner
     *
     * This returns a ScannerNextPageResult. Please verify that the jobURL (usually sent by the scanner) actually links to the same device as the scanner.
     * Use the response body immediately. Else it may be incomplete.
     *
     * @param jobUri The so-called JobUri (e.g. "/eSCL/ScanJobs/893e6fcd-487f-
     * 4056-a8c9-a87709b85daf")
     * @return The result of the request as one of the many possible outcomes specified as ScannerCreateJobResult
     * @see ScannerNextPageResult
     */
    suspend fun retrieveNextPageForJob(jobUri: String): ScannerNextPageResult {
        val jobURL: Url
        try {
            val urlBuilder = URLBuilder(rootURL)
            urlBuilder.path(jobUri)
            jobURL = urlBuilder.build()
        } catch (e: Exception) {
            return ScannerNextPageResult.InvalidJobUri(e)
        }

        val req =
            usedHttpClient
                .safeRequest<ByteArray>("${jobURL}NextDocument") {
                    accept(ContentType.Application.Any)
                }

        when (req) {
            is ESCLHttpCallResult.Success -> {}
            is ESCLHttpCallResult.Error -> return ScannerNextPageResult.RequestFailure(req)
        }

        val response = req.response
        val httpStatus = response.status

        val error =
            when {
                httpStatus == HttpStatusCode.NotFound -> ScannerNextPageResult.NoFurtherPages
                !httpStatus.isSuccess() -> ScannerNextPageResult.RequestFailure(ESCLHttpCallResult.Error.HttpError(httpStatus.value,  req.body.decodeToString()))
                response.headers["Content-Type"] == null -> ScannerNextPageResult.ContentTypeMissing(httpStatus.value, req.body.decodeToString())
                else -> null
            }
        if (error != null) return error

        return ScannerNextPageResult.Success(
            ScannedPage(
                contentType = response.headers["Content-Type"]!!,
                contentLocation = response.headers["Content-Location"],
                data = req.body,
                supportRangeHeader = (response.headers["Accept-Ranges"] ?:  "none") == "bytes",
            ),
        )
    }

    sealed class RetrieveScanImageInfoResult {
        data class Success(
            val scanImageInfo: ScanImageInfo,
        ) : RetrieveScanImageInfoResult()

        data class InvalidJobUri(val exception: Exception) : RetrieveScanImageInfoResult()

        data class RequestFailure(
            val exception: ESCLHttpCallResult.Error,
        ) : RetrieveScanImageInfoResult()

        data class ScanImageInfoMalformed(
            val xmlString: String,
            val exception: Exception,
        ) : RetrieveScanImageInfoResult()

        data class InternalBug(
            val exception: Exception,
        ) : RetrieveScanImageInfoResult()
    }

    /**
     * Retrieves the scan image info for the last page retrieved for a job on the scanner
     *
     * This returns a RetrieveScanImageInfoResult. Please verify that the jobUri (usually sent by the scanner) actually links to the same device as the scanner.
     *
     * @param jobUri The so-called JobUri (e.g. "/eSCL/ScanJobs/893e6fcd-487f-4056-a8c9-a87709b85daf")
     * @return The result of the request as one of the many possible outcomes specified as ScannerCreateJobResult
     * @see RetrieveScanImageInfoResult
     */
    suspend fun retrieveScanImageInfoForJob(jobUri: String): RetrieveScanImageInfoResult {
        val jobURL: Url
        try {
            val jobURLBuilder = URLBuilder(rootURL)
            jobURLBuilder.path(jobUri)
            jobURL = jobURLBuilder.build()
        } catch (e: Exception) {
            return RetrieveScanImageInfoResult.InvalidJobUri(e)
        }

        val req = usedHttpClient
                .safeRequest<String>("${jobURL}ScanImageInfo") {
                    accept(ContentType.Any)
                    expectSuccess = true
                }

        when (req) {
            is ESCLHttpCallResult.Success -> {}
            is ESCLHttpCallResult.Error -> return RetrieveScanImageInfoResult.RequestFailure(req)
        }

        val body = req.body

        val scanImageInfo: ScanImageInfo
        try {
            scanImageInfo = esclXml.decodeFromString(body)
        } catch (e: XmlException) {
            return RetrieveScanImageInfoResult.ScanImageInfoMalformed(body, e)
        } catch (e: IllegalArgumentException) {
            return RetrieveScanImageInfoResult.ScanImageInfoMalformed(body, e)
        } catch (e: Exception) {
            return RetrieveScanImageInfoResult.InternalBug(e)
        }
        return RetrieveScanImageInfoResult.Success(scanImageInfo)
    }

    /*sealed class RetrieveRangeResult {
        data class Success(val scannedPage: ScannedPage) : RetrieveRangeResult()
        data class NetworkError(val exception: IOException) : RetrieveRangeResult()
        data object InvalidDocumentUri : RetrieveRangeResult()
        data object WrongContentType : RetrieveRangeResult()
        data object RangeNotSatisfiable : RetrieveRangeResult()
        data class NotSuccessfulCode(val responseCode: Int) : RetrieveRangeResult()
        data object NoBodyReturned : RetrieveRangeResult()
        data class InternalBug(val exception: Exception) : RetrieveRangeResult()
    }


     * Retrieves a specified range with a documentUri
     *
     * Not all scanners support this feature. It is advertised when retrieving the NextPage using the Accept-Ranges header. Whether the scanner supports ranges is stored as a Boolean in the ScannedPage object. This method can be used to resume receiving a document (page) if the connection was lost during the original transfer with NextPage.
     * This returns a RetrieveRangeResult.
     *
     * @param documentUri The so-called DocumentUri (e.g. "/eSCL/ScanJobs/893e6fcd-487f-4056-a8c9-a87709b85daf/photo-1")
     * @param start The index of the first byte to receive
     * @param end The index of the last to byte receive (if omitted this defaults to the last byte of the document)
     * @return The result of the request as one of the many possible outcomes specified as RetrieveRangeResult
     * @see RetrieveRangeResult
    /
    fun retrieveSpecificPortionOfScanPage(
    documentUri: String,
    start: UInt,
    end: UInt?
    ): RetrieveRangeResult {
    val documentURL = rootURL.resolve(documentUri) ?: return RetrieveRangeResult.InvalidDocumentUri
    val req = Request.Builder()
    .url(documentURL)
    .get()
    .header("Range", "bytes=$start-${end?.toString() ?: ""}")
    .build()

    val response: Response
    try {
    response = usedHttpClient.newCall(req).execute()
    } catch (e: IOException) {
    return RetrieveRangeResult.NetworkError(e)
    } catch (e: IllegalStateException) {
    return RetrieveRangeResult.InternalBug(e)
    }
    val error = when {
    response.code == 416 -> RetrieveRangeResult.RangeNotSatisfiable
    !response.isSuccessful -> RetrieveRangeResult.NotSuccessfulCode(response.code)
    response.body!!.contentLength() <= 0 -> RetrieveRangeResult.NoBodyReturned
    response.header("Content-Type").isNullOrEmpty() -> RetrieveRangeResult.WrongContentType
    else -> null
    }
    if (error != null) return error

    println("RECEIVED SIZE:  ${response.body!!.bytes().size}")

    return RetrieveRangeResult.Success(ScannedPage(
    contentType = response.header("Content-Type")!!,
    supportRangeHeader = response.header("Accept-Ranges", "none") == "bytes",
    contentLocation = "$baseUrl",
    ))

    }*/
}
