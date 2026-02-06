import io.github.chrisimx.esclkt.ColorMode
import io.github.chrisimx.esclkt.ContentType
import io.github.chrisimx.esclkt.ESCLXml
import io.github.chrisimx.esclkt.EnumOrRaw
import io.github.chrisimx.esclkt.InputSource
import io.github.chrisimx.esclkt.ScanIntent
import io.github.chrisimx.esclkt.ScanRegion
import io.github.chrisimx.esclkt.ScanRegions
import io.github.chrisimx.esclkt.ScanSettings
import io.github.chrisimx.esclkt.threeHundredthsOfInch
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString

class ScanSettingsXMLProcessingTest: StringSpec( {
    val expected = "<scan:ScanSettings xmlns:scan=\"http://schemas.hp.com/imaging/escl/2011/05/03\" xmlns:pwg=\"http://www.pwg.org/schemas/2010/12/sm\"><pwg:Version>2.63</pwg:Version><scan:Intent>Document</scan:Intent><pwg:ScanRegions pwg:MustHonor=\"true\"><pwg:ScanRegion><pwg:Height>20</pwg:Height><pwg:Width>10</pwg:Width><pwg:XOffset>2</pwg:XOffset><pwg:YOffset>1</pwg:YOffset><pwg:ContentRegionUnits>escl:ThreeHundredthsOfInches</pwg:ContentRegionUnits></pwg:ScanRegion></pwg:ScanRegions><scan:DocumentFormatExt>image/jpeg</scan:DocumentFormatExt><pwg:ContentType>Text</pwg:ContentType><pwg:InputSource>Platen</pwg:InputSource><scan:XResolution>600</scan:XResolution><scan:YResolution>200</scan:YResolution><scan:ColorMode>RGB24</scan:ColorMode><scan:ColorSpace>sRGB</scan:ColorSpace></scan:ScanSettings>"

    "Test ScanSettings serialization" {
        val esclXml = ESCLXml()
        val testScanSettings =
            ScanSettings(
                version = "2.63",
                intent = EnumOrRaw.Known(ScanIntent.Document),
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
                contentType = EnumOrRaw.Known(ContentType.Text),
                inputSource = InputSource.Platen,
                xResolution = 600u,
                yResolution = 200u,
                colorMode = ColorMode.RGB24,
                colorSpace = "sRGB",
            )

        println("ScanSettings Serialization returned:\n   ${esclXml.xml.encodeToString(testScanSettings)}")

        esclXml.xml.encodeToString(testScanSettings)
            .replace("^<\\?.*?\\?>".toRegex(), "") shouldBe expected
    }
})