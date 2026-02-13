
import com.goncalossilva.resources.Resource
import io.github.chrisimx.esclkt.*
import io.kotest.assertions.throwables.shouldNotThrowMessage
import io.kotest.core.spec.style.StringSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class TestFileContentPair(
    val fileName: String,
    val content: String,
) {
    override fun toString(): String {
        return fileName
    }
}

@OptIn(ExperimentalUuidApi::class)
class ScannerCapabilitiesTest : StringSpec({
    "Explicitly test brother-mfc-l8690cdw scanner caps parsing" {
        val esclXml = ESCLXml()

        val resource = Resource("testResources/capabilities/brother-mfc-l8690cdw-caps.xml")
        resource.readText().let {
            shouldNotThrowMessage("Failed to parse brother-mfc-l8690cdw caps") {
                val scannerCapabilities = esclXml.decodeFromString<ScannerCapabilities>(it)
                scannerCapabilities.interfaceVersion shouldBe "2.62"
                scannerCapabilities.makeAndModel shouldBe "Brother MFC-L8690CDW series"
                scannerCapabilities.manufacturer shouldBe null
                scannerCapabilities.deviceUuid shouldBe Uuid.parse("00000000-0000-1000-8000-0018d7024a10")
                scannerCapabilities.adminURI shouldBe "http://192.168.200.122/net/net/airprint.html"
                scannerCapabilities.iconURI shouldBe "http://192.168.200.122/icons/device-icons-128.png"
            }
        }
    }

    val capabilitiesIndex = Resource("testResources/capabilities.txt").readText()
    val capFiles = capabilitiesIndex.split("\n")
        .map { TestFileContentPair(it, Resource("testResources/capabilities/$it").readText()) }


    withData(capFiles) {
        val esclXml = ESCLXml()
        val result = esclXml.decodeFromString<ScannerCapabilities>(it.content)
        println(result.toString())
        // expectSelfie(result.toString()).toMatchDisk_TODO()
    }

    "ScannerCapabilities serialization" {
        val testCaps = ScannerCapabilities(interfaceVersion = "2.0", makeAndModel = "dsfdsf", serialNumber = "32432434", deviceUuid = Uuid.generateV4())
        val esclXml = ESCLXml()
        val xml = esclXml.xml
        println(xml.encodeToString(ScannerCapabilities.serializer(), testCaps))

        val testResolution = SupportedResolutions(listOf(DiscreteResolution(2u, 1u)))

        println(xml.encodeToString(SupportedResolutions.serializer(),testResolution))


        val testDocumentFormats = DocumentFormats(listOf("test2", "dsfsdf"), listOf("test3"))
        println(xml.encodeToString(DocumentFormats.serializer(),testDocumentFormats))

        val testInputSourceCaps = InputSourceCaps(
            minWidth = 300.threeHundredthsOfInch(),
            maxWidth = 1200.threeHundredthsOfInch(),
            minHeight = 400.threeHundredthsOfInch(),
            maxHeight = 1600.threeHundredthsOfInch(),
            maxScanRegions = null,
            maxOpticalXResolution = null,
            maxOpticalYResolution = null,
            riskyLeftMargin = null,
            riskyRightMargin = null,
            riskyTopMargin = null,
            riskyBottomMargin = null,
            maxPhysicalWidth = null,
            maxPhysicalHeight = null,
            settingProfiles = emptyList(),
            supportedIntents = listOf(
                EnumOrRaw.Known(ScanIntent.BusinessCard),
                EnumOrRaw.Unknown("CUSTOM_INTENT")
            ),
            edgeAutoDetection = emptyList()
        )

        println(xml.encodeToString(InputSourceCaps.serializer(),testInputSourceCaps))
    }
})
