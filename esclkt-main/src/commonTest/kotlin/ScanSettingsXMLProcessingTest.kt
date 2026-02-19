import io.github.chrisimx.esclkt.*
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
                    scanRegion {
                        height = 20u.threeHundredthsOfInch()
                        width = 10u.threeHundredthsOfInch()
                        xOffset = 2u.threeHundredthsOfInch()
                        yOffset = 1u.threeHundredthsOfInch()
                    },
                documentFormatExt = "image/jpeg",
                contentType = EnumOrRaw.Known(ContentType.Text),
                inputSource = InputSource.Platen,
                xResolution = 600u,
                yResolution = 200u,
                colorMode = EnumOrRaw.Known(ColorMode.RGB24),
                colorSpace = "sRGB",
            )

        println("ScanSettings Serialization returned:\n   ${esclXml.xml.encodeToString(testScanSettings)}")

        esclXml.xml.encodeToString(testScanSettings)
            .replace("^<\\?.*?\\?>".toRegex(), "") shouldBe expected
    }
})