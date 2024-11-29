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

package io.github.chrisimx.esclkt/*
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

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlNamespaceDeclSpec
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName(value = "State", prefix = "pwg", namespace = "http://www.pwg.org/schemas/2010/12/sm")
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

@Serializable
@XmlSerialName(value = "AdfState", prefix = "scan", namespace = "http://schemas.hp.com/imaging/escl/2011/05/03")
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
    ScannerAdfInputTrayOverloaded
}

@OptIn(ExperimentalXmlUtilApi::class)
@Serializable
@XmlSerialName(value = "ScannerStatus", prefix = "scan", namespace = "http://schemas.hp.com/imaging/escl/2011/05/03")
@XmlNamespaceDeclSpec("pwg=http://www.pwg.org/schemas/2010/12/sm;scan=http://schemas.hp.com/imaging/escl/2011/05/03")
data class ScannerStatus(
    @XmlElement
    @XmlSerialName(value = "Version", prefix = "pwg", namespace = "http://www.pwg.org/schemas/2010/12/sm")
    val version: String,
    @XmlElement
    @XmlSerialName(value = "State", prefix = "pwg", namespace = "http://www.pwg.org/schemas/2010/12/sm")
    val state: ScannerState,
    @XmlElement
    val jobs: Jobs? = null,
    @XmlElement
    val adfState: AdfState? = null,
)

@Serializable
@XmlSerialName(value = "Jobs", prefix = "scan", namespace = "http://schemas.hp.com/imaging/escl/2011/05/03")
data class Jobs(
    @XmlElement
    val jobInfos: List<JobInfo>,
)

@Serializable
@XmlSerialName(value = "JobInfo", prefix = "scan", namespace = "http://schemas.hp.com/imaging/escl/2011/05/03")
data class JobInfo(
    @XmlElement
    @XmlSerialName(value = "JobUri", prefix = "pwg", namespace = "http://www.pwg.org/schemas/2010/12/sm")
    val jobURI: String,

    @XmlElement
    @XmlSerialName(value = "JobUuid", prefix = "pwg", namespace = "http://www.pwg.org/schemas/2010/12/sm")
    val jobUUID: String,

    @XmlElement
    @XmlSerialName(value = "Age", prefix = "scan", namespace = "http://schemas.hp.com/imaging/escl/2011/05/03")
    /** time in seconds since the job info has been updated. The duration is the difference between the time of the
     * latest update to job info element relative to the time of the status request. **/
    val age: UInt,

    @XmlElement
    @XmlSerialName(value = "ImagesCompleted", prefix = "pwg", namespace = "http://www.pwg.org/schemas/2010/12/sm")
    val imagesCompleted: UInt,

    @XmlElement
    @XmlSerialName(value = "ImagesToTransfer", prefix = "pwg", namespace = "http://www.pwg.org/schemas/2010/12/sm")
    val imagesToTransfer: UInt? = null,

    @XmlElement
    @XmlSerialName(value = "JobState", prefix = "pwg", namespace = "http://www.pwg.org/schemas/2010/12/sm")
    val jobState: JobState,

    @XmlElement
    @XmlSerialName(value = "JobStateReasons", prefix = "pwg", namespace = "http://www.pwg.org/schemas/2010/12/sm")
    val jobStateReasons: JobStateReasons? = null
)

@Serializable
@XmlSerialName(value = "JobState", prefix = "pwg", namespace = "http://www.pwg.org/schemas/2010/12/sm")
enum class JobState {
    /// End state - indicates that the job was canceled either by the remote client application
    /// (through the eSCL interface) or by the user interacting with the scanner directly. Check
    /// [JobStateReasons] for more details.
    Canceled,

    /// End state - either an internal device error, or a communication error or a security error
    Aborted,

    /// Job is finished successfully
    Completed,

    /// The job was initiated, and the scanner is preparing the scan engine
    Pending,

    /// The scanner is processing the job and is transmitting the scan data
    Processing,
}

@Serializable
@XmlSerialName(value = "JobStateReasons", prefix = "pwg", namespace = "http://www.pwg.org/schemas/2010/12/sm")
data class JobStateReasons(
    @XmlElement
    @XmlSerialName(value = "JobStateReason", prefix = "pwg", namespace = "http://www.pwg.org/schemas/2010/12/sm")
    val jobStateReason: String,
)