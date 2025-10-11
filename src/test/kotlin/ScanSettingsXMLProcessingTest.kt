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

import io.github.chrisimx.esclkt.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ScanSettingsXMLProcessingTest {
    val expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><scan:ScanSettings xmlns:scan=\"http://schemas.hp.com/imaging/escl/2011/05/03\" xmlns:pwg=\"http://www.pwg.org/schemas/2010/12/sm\"><pwg:Version>2.63</pwg:Version><scan:Intent>Document</scan:Intent><pwg:ScanRegions pwg:MustHonor=\"true\"><pwg:ScanRegion><pwg:Height>20</pwg:Height><pwg:ContentRegionUnits>escl:ThreeHundredthsOfInches</pwg:ContentRegionUnits><pwg:Width>10</pwg:Width><pwg:XOffset>2</pwg:XOffset><pwg:YOffset>1</pwg:YOffset></pwg:ScanRegion></pwg:ScanRegions><scan:DocumentFormatExt>image/jpeg</scan:DocumentFormatExt><pwg:ContentType>Text</pwg:ContentType><pwg:InputSource>Platen</pwg:InputSource><scan:XResolution>600</scan:XResolution><scan:YResolution>200</scan:YResolution><scan:ColorMode>RGB24</scan:ColorMode><scan:ColorSpace>sRGB</scan:ColorSpace></scan:ScanSettings>"

    @Test
    fun createScanRequest() {
        val testScanSettings =
            ScanSettings(
                version = "2.63",
                intent = ScanIntentData.ScanIntentEnum(ScanIntent.Document),
                scanRegions =
                    ScanRegions(
                        listOf(
                            ScanRegion(
                                20u.threeHundredthsOfInch(),
                                10u.threeHundredthsOfInch(),
                                2u.threeHundredthsOfInch(),
                                1u.threeHundredthsOfInch(),
                            ),
                        ),
                        true,
                    ),
                documentFormatExt = "image/jpeg",
                contentType = ContentType.Text,
                inputSource = InputSource.Platen,
                xResolution = 600u,
                yResolution = 200u,
                colorMode = ColorMode.RGB24,
                colorSpace = "sRGB",
            )
        assertEquals(expected, testScanSettings.toXMLString())
        println("ScanSettings Serialization returned:\n   ${testScanSettings.toXMLString()}")
    }
}
