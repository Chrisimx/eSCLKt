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
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@XmlSerialName(value = "ScanImageInfo", prefix = "scan", namespace = "http://schemas.hp.com/imaging/escl/2011/05/03")
data class ScanImageInfo @OptIn(ExperimentalUuidApi::class) constructor(
    @XmlElement
    @XmlSerialName(value = "JobUri", prefix = "pwg", namespace = "http://www.pwg.org/schemas/2010/12/sm")
    val jobURI: String,
    @XmlElement
    @XmlSerialName(value = "JobUuid", prefix = "pwg", namespace = "http://www.pwg.org/schemas/2010/12/sm")
    val jobUuid: Uuid,
    @XmlElement
    @XmlSerialName(value = "ActualWidth", prefix = "scan", namespace = "http://schemas.hp.com/imaging/escl/2011/05/03")
    val actualWidth: UInt,
    @XmlElement
    @XmlSerialName(value = "ActualHeight", prefix = "scan", namespace = "http://schemas.hp.com/imaging/escl/2011/05/03")
    val actualHeight: UInt,
    @XmlElement
    @XmlSerialName(
        value = "ActualBytesPerLine",
        prefix = "scan",
        namespace = "http://schemas.hp.com/imaging/escl/2011/05/03"
    )
    val actualBytesPerLine: UInt,
    @XmlElement
    @XmlSerialName(
        value = "BlankPageDetected",
        prefix = "scan",
        namespace = "http://schemas.hp.com/imaging/escl/2011/05/03"
    )
    val blankPageDetected: Boolean?,
)