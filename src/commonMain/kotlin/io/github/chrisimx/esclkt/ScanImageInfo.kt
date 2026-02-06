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
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@OptIn(ExperimentalUuidApi::class)
@XmlSerialName("ScanImageInfo", NS_SCAN, "scan")
data class ScanImageInfo(
        @XmlSerialName("JobUri", NS_PWG, "pwg")
        val jobURI: String,
        @XmlSerialName("JobUuid", NS_PWG, "pwg")
        val jobUuid: Uuid,
        @XmlSerialName("ActualWidth", NS_SCAN, "scan")
        val actualWidth: UInt,
        @XmlSerialName("ActualHeight", NS_SCAN, "scan")
        val actualHeight: UInt,
        @XmlSerialName("ActualBytesPerLine", NS_SCAN, "scan")
        val actualBytesPerLine: UInt,
        @XmlSerialName("BlankPageDetected", NS_SCAN, "scan")
        val blankPageDetected: Boolean?,
)
