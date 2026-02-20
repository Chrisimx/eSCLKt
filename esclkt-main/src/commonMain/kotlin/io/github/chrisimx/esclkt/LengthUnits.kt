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
import kotlin.jvm.JvmInline
import kotlin.math.roundToInt

// Conversion factors
const val MILLIMETERS_PER_INCH = 25.4
const val THREE_HUNDREDTHS_INCHES_PER_INCH = 300.0
const val THREE_HUNDREDTHS_INCHES_PER_MM = 1.0 / 25.4 * THREE_HUNDREDTHS_INCHES_PER_INCH

sealed interface LengthUnit {
    fun toInches(): Inches

    fun toThreeHundredthsOfInch(): ThreeHundredthsOfInch

    fun toMillimeters(): Millimeters

    fun toPoints(): Points
}

fun LengthUnit.equalsLength(other: LengthUnit?): Boolean {
    if (other == null) return false
    return this.toThreeHundredthsOfInch().value ==
            other.toThreeHundredthsOfInch().value
}

// Units
@Serializable
@JvmInline
value class Inches(
    val value: Double,
) : LengthUnit {
    override fun toInches() = this

    override fun toMillimeters() = Millimeters(this.value * MILLIMETERS_PER_INCH)

    override fun toPoints(): Points = Points(this.value * 72)

    override fun toThreeHundredthsOfInch() = ThreeHundredthsOfInch((this.value * THREE_HUNDREDTHS_INCHES_PER_INCH).toUInt())
}

@Serializable
@JvmInline
value class Millimeters(
    val value: Double,
) : LengthUnit {
    override fun toMillimeters() = this
    override fun toPoints(): Points = this.toInches().toPoints()

    override fun toInches() = Inches(this.value / MILLIMETERS_PER_INCH)

    override fun toThreeHundredthsOfInch() = ThreeHundredthsOfInch((this.value * THREE_HUNDREDTHS_INCHES_PER_MM).roundToInt().toUInt())
}

@Serializable
@JvmInline
value class ThreeHundredthsOfInch(
    val value: UInt,
) : LengthUnit {
    override fun toThreeHundredthsOfInch() = this
    override fun toPoints(): Points = this.toInches().toPoints()

    override fun toMillimeters() = Millimeters(this.value.toDouble() / THREE_HUNDREDTHS_INCHES_PER_MM)

    override fun toInches() = Inches(this.value.toDouble() / THREE_HUNDREDTHS_INCHES_PER_INCH)
}

@Serializable
@JvmInline
value class Points(
    val value: Double,
) : LengthUnit {
    override fun toThreeHundredthsOfInch() = this.toInches().toThreeHundredthsOfInch()

    override fun toMillimeters() = this.toInches().toMillimeters()
    override fun toInches() = Inches(this.value / 72.0)

    override fun toPoints(): Points = this
}

fun Number.inches(): Inches = Inches(this.toDouble())

fun Number.millimeters(): Millimeters = Millimeters(this.toDouble())

fun Number.threeHundredthsOfInch(): ThreeHundredthsOfInch = ThreeHundredthsOfInch(this.toInt().toUInt())

fun Number.pts(): Points = Points(this.toDouble())

fun UInt.inches(): Inches = Inches(this.toDouble())

fun UInt.millimeters(): Millimeters = Millimeters(this.toDouble())

fun UInt.threeHundredthsOfInch(): ThreeHundredthsOfInch = ThreeHundredthsOfInch(this)

fun UInt.pts(): Points = Points(this.toDouble())