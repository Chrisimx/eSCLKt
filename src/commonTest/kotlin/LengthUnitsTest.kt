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

import io.github.chrisimx.esclkt.inches
import io.github.chrisimx.esclkt.millimeters
import io.github.chrisimx.esclkt.threeHundredthsOfInch
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe


class LengthUnitsTest: FunSpec({
    test("Test LengthUnit conversions") {
        val threeHundredthsOfInch = 300u.threeHundredthsOfInch()
        val threeHundredthsOfInchInMM = threeHundredthsOfInch.toMillimeters()
        val threeHundredthsOfInchInInches = threeHundredthsOfInch.toInches()

        1.inches() shouldBe threeHundredthsOfInchInInches
        25.4.millimeters() shouldBe threeHundredthsOfInchInMM

        threeHundredthsOfInch
            .toInches()
            .toMillimeters()
            .toInches()
            .toThreeHundredthsOfInch() shouldBe threeHundredthsOfInch


        threeHundredthsOfInchInMM.toThreeHundredthsOfInch() shouldBe threeHundredthsOfInch
        threeHundredthsOfInchInInches.toThreeHundredthsOfInch() shouldBe threeHundredthsOfInch
    }
})