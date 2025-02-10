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

import nl.adaptivity.xmlutil.XmlException
import nl.adaptivity.xmlutil.serialization.XML
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.xml.sax.SAXException
import java.io.Closeable
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * A client for the eSCL scanning protocol
 *
 * @param baseUrl The base url to make requests to. Has to be a URL which is derived with "http URL to printer" + "rs" txt record acquired from mDNS
 * @param usedHttpClient eSCL is an HTTP-based protocol. This OkHttpClient will be used to execute requests
 */
class ESCLRequestClient(
    val baseUrl: HttpUrl, val usedHttpClient: OkHttpClient = OkHttpClient().newBuilder()
        .readTimeout(100, TimeUnit.SECONDS)
        .writeTimeout(100, TimeUnit.SECONDS)
        .connectTimeout(100, TimeUnit.SECONDS)
        .callTimeout(100, TimeUnit.SECONDS)
        .build()
) {
    private val xml = XML {
    }
    private val rootURL: HttpUrl = baseUrl.newBuilder().encodedPath("/").build()


    sealed class ScannerCapabilitiesResult {
        data class Success(val scannerCapabilities: ScannerCapabilities) : ScannerCapabilitiesResult()
        data class NetworkError(val exception: IOException) : ScannerCapabilitiesResult()
        data class NotSuccessfulCode(val responseCode: Int) : ScannerCapabilitiesResult()
        data object NoBodyReturned : ScannerCapabilitiesResult()
        data class WrongContentType(val contentType: String?) : ScannerCapabilitiesResult()
        data class XMLParsingError(val content: String, val saxException: SAXException) : ScannerCapabilitiesResult()
        data class ScannerCapabilitiesMalformed(val content: String, val exception: Exception) :
            ScannerCapabilitiesResult()
        data class InternalBug(val exception: Exception) : ScannerCapabilitiesResult()
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
    fun getScannerCapabilities(): ScannerCapabilitiesResult {
        val req = Request.Builder()
            .url("${baseUrl}ScannerCapabilities")
            .header("Accept", "*/*")
            .get()
            .build()

        val response: Response
        try {
            response = usedHttpClient.newCall(req).execute()
        } catch (e: IOException) {
            return ScannerCapabilitiesResult.NetworkError(e)
        } catch (e: IllegalStateException) {
            return ScannerCapabilitiesResult.InternalBug(e)
        }

        response.use {
            // Checking for response errors on HTTP level
            val error = when {
                !it.isSuccessful -> ScannerCapabilitiesResult.NotSuccessfulCode(it.code)
                it.body!!.contentLength() == 0L -> ScannerCapabilitiesResult.NoBodyReturned
                it.header("Content-Type")
                    ?.contains("text/xml") != true -> ScannerCapabilitiesResult.WrongContentType(it.header("Content-Type"))
                else -> null
            }
            if (error != null) return error

            val scannerCapabilities: ScannerCapabilities
            var body: String? = null
            try {
                body = it.body!!.string()
                if (body.isEmpty()) return ScannerCapabilitiesResult.NoBodyReturned
                scannerCapabilities = ScannerCapabilities.fromXML(body.byteInputStream())
            } catch (exception: IllegalArgumentException) {
                return ScannerCapabilitiesResult.ScannerCapabilitiesMalformed(body.toString(), exception)
            } catch (exception: IOException) {
                return ScannerCapabilitiesResult.InternalBug(exception)
            } catch (exception: SAXException) {
                return ScannerCapabilitiesResult.XMLParsingError(body.toString(), exception)
            }

            return ScannerCapabilitiesResult.Success(scannerCapabilities)
        }
    }

    sealed class ScannerStatusResult {
        data class Success(val scannerStatus: ScannerStatus) : ScannerStatusResult()
        data class NetworkError(val exception: IOException) : ScannerStatusResult()
        data class NotSuccessfulCode(val responseCode: Int) : ScannerStatusResult()
        data object NoBodyReturned : ScannerStatusResult()
        data object WrongContentType : ScannerStatusResult()
        data class ScannerStatusMalformed(val xmlString: String, val exception: Exception) : ScannerStatusResult()
        data class InternalBug(val exception: Exception) : ScannerStatusResult()
    }

    /**
     * Executes a ScannerStatus request to receive the current status of the scanner.
     *
     * This status includes information if the scanner is currently busy or ready to scan pages.
     *
     * @return The result of the request as one of the many possible outcomes specified as ScannerStatusResult
     * @see ScannerStatusResult
     */
    fun getScannerStatus(): ScannerStatusResult {
        val req = Request.Builder()
            .url("${baseUrl}ScannerStatus")
            .header("Accept", "*/*")
            .get()
            .build()

        val response: Response
        try {
            response = usedHttpClient.newCall(req).execute()
        } catch (e: IOException) {
            return ScannerStatusResult.NetworkError(e)
        } catch (e: IllegalStateException) {
            return ScannerStatusResult.InternalBug(e)
        }

        response.use {
            // Checking for response errors on HTTP level
            val body = it.body!!.string()
            val error = when {
                !it.isSuccessful -> ScannerStatusResult.NotSuccessfulCode(it.code)
                body.isEmpty() -> ScannerStatusResult.NoBodyReturned
                it.header("Content-Type")?.contains("text/xml") != true -> ScannerStatusResult.WrongContentType
                else -> null
            }
            if (error != null) return error

            //body = body.removePrefix("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")

            val scannerStatus: ScannerStatus
            try {
                scannerStatus = xml.decodeFromString(ScannerStatus.serializer(), body)
            } catch (exception: Exception) {
                println("Scanner status invalid: $body")
                return ScannerStatusResult.ScannerStatusMalformed(body, exception)
            }

            return ScannerStatusResult.Success(scannerStatus)
        }
    }

    sealed class ScannerCreateJobResult {
        data class Success(val scanJob: ScanJob) : ScannerCreateJobResult()
        data class NetworkError(val exception: IOException) : ScannerCreateJobResult()
        data class NotSuccessfulCode(val responseCode: Int) : ScannerCreateJobResult()
        data object NoLocationGiven : ScannerCreateJobResult()
        data class InternalBug(val exception: Exception) : ScannerCreateJobResult()
    }

    /**
     * Posts a job to the scanner
     *
     * This returns a ScannerCreateJobResult and when creation is successful, this contains a ScanJob instance which can be used to interact with the created job.
     *
     * @return The result of the request as one of the many possible outcomes specified as ScannerCreateJobResult
     * @see ScannerCreateJobResult
     */
    fun createJob(scanSettings: ScanSettings): ScannerCreateJobResult {
        val req = Request.Builder()
            .url("${baseUrl}ScanJobs")
            .post(xml.encodeToString(ScanSettings.serializer(), scanSettings).toRequestBody("text/xml".toMediaType()))
            .build()

        val response: Response
        try {
            response = usedHttpClient.newCall(req).execute()
        } catch (e: IOException) {
            return ScannerCreateJobResult.NetworkError(e)
        } catch (e: IllegalStateException) {
            return ScannerCreateJobResult.InternalBug(e)
        }

        response.use {
            // Checking for response errors on HTTP level
            val jobLocation = it.header("Location")?.toHttpUrlOrNull()
            val error = when {
                it.code != 201 -> ScannerCreateJobResult.NotSuccessfulCode(it.code) // Returned code is not 201 (Created)
                jobLocation == null -> ScannerCreateJobResult.NoLocationGiven
                else -> null
            }
            if (error != null) return error

            return ScannerCreateJobResult.Success(
                ScanJob(
                    jobLocation!!.newBuilder().encodedPath(jobLocation.encodedPath + "/").build(), this, scanSettings
                )
            )
        }
    }

    sealed class ScannerDeleteJobResult {
        data object Success : ScannerDeleteJobResult()
        data object InvalidJobUri : ScannerDeleteJobResult()
        data class NetworkError(val exception: IOException) : ScannerDeleteJobResult()
        data class NotSuccessfulCode(val responseCode: Int) : ScannerDeleteJobResult()
        data class InternalBug(val exception: Exception) : ScannerDeleteJobResult()
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
    fun deleteJob(jobUri: String): ScannerDeleteJobResult {
        val jobURL = rootURL.resolve(jobUri) ?: return ScannerDeleteJobResult.InvalidJobUri
        val req = Request.Builder()
            .url(jobURL)
            .delete()
            .build()

        val response: Response
        try {
            response = usedHttpClient.newCall(req).execute()
        } catch (e: IOException) {
            return ScannerDeleteJobResult.NetworkError(e)
        } catch (e: IllegalStateException) {
            return ScannerDeleteJobResult.InternalBug(e)
        }

        response.use {
            // Checking for response errors on HTTP level
            if (!it.isSuccessful) return ScannerDeleteJobResult.NotSuccessfulCode(it.code)

            return ScannerDeleteJobResult.Success
        }
    }

    data class ScannedPage(
        val contentType: String,
        val contentLocation: String? = null,
        val data: Response,
        val supportRangeHeader: Boolean
    ) : Closeable {
        override fun close() {
            data.close()
        }
    }

    sealed class ScannerNextPageResult {
        data class Success(val page: ScannedPage) : ScannerNextPageResult()
        data object NoFurtherPages : ScannerNextPageResult()
        data object InvalidJobUri : ScannerNextPageResult()
        data class NetworkError(val exception: IOException) : ScannerNextPageResult()
        data object WrongContentType : ScannerNextPageResult()
        data class NotSuccessfulCode(val responseCode: Int) : ScannerNextPageResult()
        data class InternalBug(val exception: Exception) : ScannerNextPageResult()
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
    fun retrieveNextPageForJob(jobUri: String): ScannerNextPageResult {
        val jobURL = rootURL.resolve(jobUri) ?: return ScannerNextPageResult.InvalidJobUri
        val req = Request.Builder()
            .url("${jobURL}NextDocument")
            .get()
            .header("Accept", "*/*")
            .build()

        val response: Response
        try {
            response = usedHttpClient.newCall(req).execute()
        } catch (e: IOException) {
            return ScannerNextPageResult.NetworkError(e)
        } catch (e: IllegalStateException) {
            return ScannerNextPageResult.InternalBug(e)
        }

        val error = when {
            response.code == 404 -> ScannerNextPageResult.NoFurtherPages
            !response.isSuccessful -> ScannerNextPageResult.NotSuccessfulCode(response.code)
            response.header("Content-Type").isNullOrEmpty() -> ScannerNextPageResult.WrongContentType
            else -> null
        }
        if (error != null) return error

        return ScannerNextPageResult.Success(
            ScannedPage(
                contentType = response.header("Content-Type")!!,
                contentLocation = response.header("Content-Location"),
                data = response,
                supportRangeHeader = response.header("Accept-Ranges", "none") == "bytes"
            )
        )
    }

    sealed class RetrieveScanImageInfoResult {
        data class Success(val scanImageInfo: ScanImageInfo) : RetrieveScanImageInfoResult()
        data class NetworkError(val exception: IOException) : RetrieveScanImageInfoResult()
        data object InvalidJobUri : RetrieveScanImageInfoResult()
        data object WrongContentType : RetrieveScanImageInfoResult()
        data class NotSuccessfulCode(val responseCode: Int) : RetrieveScanImageInfoResult()
        data class ScanImageInfoMalformed(val errorCausingXml: String) : RetrieveScanImageInfoResult()
        data object NoBodyReturned : RetrieveScanImageInfoResult()
        data class InternalBug(val exception: Exception) : RetrieveScanImageInfoResult()
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
    fun retrieveScanImageInfoForJob(jobUri: String): RetrieveScanImageInfoResult {
        val jobURL = rootURL.resolve(jobUri) ?: return RetrieveScanImageInfoResult.InvalidJobUri
        val req = Request.Builder()
            .url("${jobURL}ScanImageInfo")
            .get()
            .build()

        val response: Response
        try {
            response = usedHttpClient.newCall(req).execute()
        } catch (e: IOException) {
            return RetrieveScanImageInfoResult.NetworkError(e)
        } catch (e: IllegalStateException) {
            return RetrieveScanImageInfoResult.InternalBug(e)
        }

        response.use {
            val body = it.body!!.string()
            val error = when {
                !it.isSuccessful -> RetrieveScanImageInfoResult.NotSuccessfulCode(it.code)
                body.isEmpty() -> RetrieveScanImageInfoResult.NoBodyReturned
                it.header("Content-Type")
                    ?.startsWith("text/xml") == true -> RetrieveScanImageInfoResult.WrongContentType

                else -> null
            }
            if (error != null) return error

            val scanImageInfo: ScanImageInfo
            try {
                scanImageInfo = xml.decodeFromString(ScanImageInfo.serializer(), body)
            } catch (exception: XmlException) {
                println("ScanImageInfo XML invalid: $body")
                return RetrieveScanImageInfoResult.ScanImageInfoMalformed(body)
            }

            return RetrieveScanImageInfoResult.Success(scanImageInfo)
        }
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