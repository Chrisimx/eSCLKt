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

import kotlinx.serialization.Serializable
import org.w3c.dom.Element
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.uuid.ExperimentalUuidApi

enum class ScannerState {
    /** Idle state **/
    Idle,

    /** Busy with some job or activity **/
    Processing,

    /** Calibrating, preparing the unit **/
    Testing,

    /** Error condition occurred **/
    Stopped,

    /** Unit is unavailable **/
    Down,
}

enum class AdfState {
    ScannerAdfProcessing, // the OK state, other states are errors or require 'user attention'
    ScannerAdfEmpty,
    ScannerAdfJam,
    ScannerAdfLoaded,
    ScannerAdfMispick,
    ScannerAdfHatchOpen,
    ScannerAdfDuplexPageTooShort,
    ScannerAdfDuplexPageTooLong,
    ScannerAdfMultipickDetected,
    ScannerAdfInputTrayFailed,
    ScannerAdfInputTrayOverloaded,
}

@Serializable
data class ScannerStatus(
    val version: String,
    val state: ScannerState,
    val jobs: List<JobInfo> = listOf(),
    val adfState: AdfState? = null,
) {
    companion object {
        @OptIn(ExperimentalUuidApi::class)
        fun fromXML(xml: InputStream): ScannerStatus {
            val documentBuilderFactory = DocumentBuilderFactory.newInstance()
            val docBuilder = documentBuilderFactory.newDocumentBuilder()
            val parsedDoc = docBuilder.parse(xml)
            parsedDoc.documentElement.normalize()
            val xmlRoot = parsedDoc.documentElement

            if (xmlRoot.tagName !=
                "scan:ScannerStatus"
            ) {
                throw IllegalArgumentException("Malformed ScannerStatus: root tag not 'scan:ScannerStatus'")
            }

            val version = xmlRoot.findRequiredUniqueElementWithName("pwg:Version").textContent
            if (!version.matches(
                    Regex("[0-9]+.[0-9]+"),
                )
            ) {
                throw IllegalArgumentException("Malformed ScannerStatus version '$version'")
            }

            val jobsElement = xmlRoot.findUniqueElementWithName("scan:Jobs")
            val jobInfoElements = jobsElement?.getElementsByTagName("scan:JobInfo")

            val jobInfosResult = mutableListOf<JobInfo>()

            if (jobInfoElements != null) {
                for (i in 0 until jobInfoElements.length) {
                    val jobElement = jobInfoElements.item(i) as Element
                    jobInfosResult.add(JobInfo.fromElement(jobElement))
                }
            }

            val stateString = xmlRoot.findRequiredUniqueElementWithName("pwg:State").textContent
            val state = ScannerState.valueOf(stateString)

            val adfStateString = xmlRoot.findUniqueElementWithName("scan:AdfState")?.textContent
            val adfState = adfStateString?.let { AdfState.valueOf(it) }

            return ScannerStatus(
                version = version,
                state = state,
                jobs = jobInfosResult,
                adfState = adfState
            )
        }
    }
}

@Serializable
data class JobInfo(
    val jobURI: String,
    val jobUUID: String,
    /** time in seconds since the job info has been updated. The duration is the difference between the time of the
     * latest update to job info element relative to the time of the status request. **/
    val age: UInt,
    val imagesCompleted: UInt,
    val imagesToTransfer: UInt? = null,
    val transferRetryCount: UInt? = null,
    val jobState: JobState,
    val jobStateReason: String? = null,
) {
    companion object {
        fun fromElement(elem: Element): JobInfo {
            val jobURI = elem.findRequiredUniqueElementWithName("pwg:JobUri").textContent
            val jobUUID = elem.findRequiredUniqueElementWithName("pwg:JobUuid").textContent
            val age = elem.findRequiredUniqueElementWithName("scan:Age").textContent.toUInt()
            val imagesCompleted = elem.findRequiredUniqueElementWithName("pwg:ImagesCompleted").textContent.toUInt()
            val imagesToTransfer = elem.findUniqueElementWithName("pwg:ImagesToTransfer")?.textContent?.toUIntOrNull()
            val transferRetryCount = elem.findUniqueElementWithName("scan:TransferRetryCount")?.textContent?.toUIntOrNull()
            val jobState = elem.findRequiredUniqueElementWithName("pwg:JobState").textContent.let {
                JobState.valueOf(it)
            }
            val jobStateReasons = elem.findUniqueElementWithName("pwg:JobStateReasons")
            val jobStateReason = jobStateReasons?.findUniqueElementWithName("pwg:JobStateReason")?.textContent

            return JobInfo(jobURI, jobUUID, age, imagesCompleted, imagesToTransfer, transferRetryCount, jobState, jobStateReason)
        }
    }
}

enum class JobState {
    // / End state - indicates that the job was canceled either by the remote client application
    // / (through the eSCL interface) or by the user interacting with the scanner directly. Check
    // / [JobStateReasons] for more details.
    Canceled,

    // / End state - either an internal device error, or a communication error or a security error
    Aborted,

    // / Job is finished successfully
    Completed,

    // / The job was initiated, and the scanner is preparing the scan engine
    Pending,

    // / The scanner is processing the job and is transmitting the scan data
    Processing,
}