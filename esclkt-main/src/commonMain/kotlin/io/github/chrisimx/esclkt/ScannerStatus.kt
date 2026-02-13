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
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlNamespaceDeclSpecs
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@XmlSerialName("State", NS_PWG, "pwg")
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

@XmlSerialName("AdfState", NS_SCAN, "scan")
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

@OptIn(ExperimentalXmlUtilApi::class)
@Serializable
@XmlSerialName("ScannerStatus", NS_SCAN, "scan")
@XmlNamespaceDeclSpecs("pwg=http://www.pwg.org/schemas/2010/12/sm", "scan=http://schemas.hp.com/imaging/escl/2011/05/03")
data class ScannerStatus(
    @XmlSerialName("Version", NS_PWG, "pwg")
    val version: String,
    @XmlSerialName("State", NS_PWG, "pwg")
    val state: ScannerState,
    @XmlSerialName("Jobs", NS_SCAN, "scan")
    @XmlChildrenName("JobInfo", NS_SCAN, "scan")
    val jobs: List<JobInfo> = listOf(),
    @XmlSerialName("AdfState", NS_SCAN, "scan")
    val adfState: AdfState? = null,
)

@Serializable
@XmlSerialName("JobInfo", NS_SCAN, "scan")
data class JobInfo(
    @XmlSerialName("JobUri", NS_PWG, "pwg")
    val jobURI: String,
    @XmlSerialName("JobUuid", NS_PWG, "pwg")
    val jobUUID: String,
    /** time in seconds since the job info has been updated. The duration is the difference between the time of the
     * latest update to job info element relative to the time of the status request. **/
    @XmlSerialName("Age", NS_SCAN, "scan")
    val age: UInt,
    @XmlSerialName("ImagesCompleted", NS_PWG, "pwg")
    val imagesCompleted: UInt,
    @XmlSerialName("ImagesToTransfer", NS_PWG, "pwg")
    val imagesToTransfer: UInt? = null,
    @XmlSerialName("TransferRetryCount", NS_SCAN, "scan")
    val transferRetryCount: UInt? = null,
    @XmlSerialName("JobState", NS_PWG, "pwg")
    val jobState: JobState,
    @XmlSerialName("JobStateReasons", NS_PWG, "pwg")
    @XmlChildrenName("JobStateReason", NS_PWG, "pwg")
    val jobStateReasons: List<String>? = null,
)
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
