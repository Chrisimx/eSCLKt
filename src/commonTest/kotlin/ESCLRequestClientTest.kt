import com.goncalossilva.resources.Resource
import io.github.chrisimx.esclkt.ESCLHttpCallResult
import io.github.chrisimx.esclkt.ESCLRequestClient
import io.github.chrisimx.esclkt.ESCLRequestClient.ScannerCapabilitiesResult
import io.github.chrisimx.esclkt.ScanSettings
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.*

fun MockEngine.Queue.enqueueStatus(status: Int) {
    enqueue {
        respond(
            content = "",
            status = HttpStatusCode.fromValue(status),
        )
    }
}

suspend fun ESCLRequestClient.testForCode(code: Int) {
    val result1 = this.getScannerCapabilities()
    result1 shouldBe ScannerCapabilitiesResult.RequestFailure(ESCLHttpCallResult.Error.HttpError(code, ""))
}

class ESCLRequestClientTest: FunSpec({
    test("Unreachable device") {
        val testedESCLClient = ESCLRequestClient(Url("http://test.local/eSCL/"))
        val scannerCapsResult = testedESCLClient.getScannerCapabilities()

        (scannerCapsResult is ScannerCapabilitiesResult.RequestFailure) shouldBe true

        println(scannerCapsResult)

    }
     test("Untrusted TLS certificate") {
         val httpClient = HttpClient()
         print(httpClient.engine)
        val testedESCLClient = ESCLRequestClient(Url("https://self-signed.badssl.com/"))

        val scannerCaps = testedESCLClient.getScannerCapabilities() //shouldBe
        //        ScannerCapabilitiesResult.RequestFailure(ESCLHttpCallResult.Error.UntrustedCertificate)

         (scannerCaps is ScannerCapabilitiesResult.RequestFailure) shouldBe true

         println(testedESCLClient.getScannerCapabilities())
         println(testedESCLClient.getScannerCapabilities())
    }

    test("test response code checking") {
        val mockEngine = MockEngine.Queue()

        mockEngine.enqueueStatus(404)
        mockEngine.enqueueStatus(500)
        mockEngine.enqueueStatus(503)
        mockEngine.enqueueStatus(200)

        val client = HttpClient(mockEngine)
        val testedESCLClient = ESCLRequestClient(Url("http://test.local/eSCL/"), client)

        testedESCLClient.testForCode(404)
        testedESCLClient.testForCode(500)
        testedESCLClient.testForCode(503)

        testedESCLClient.getScannerCapabilities().shouldBeInstanceOf<ScannerCapabilitiesResult.ScannerCapabilitiesMalformed>()
    }

    test("test ScannerCapabilitiesRetrieval") {

        val mockEngine = MockEngine.Queue()

        val resource = Resource("testResources/capabilities/brother-mfc-l8690cdw-caps.xml").readText()
        mockEngine.enqueue {
            respond(
                resource,
                HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Xml.toString()))
        }

        mockEngine.enqueue {
            respond(
                resource,
                HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "text/xml; charset=utf-8"))
        }

        val statusResource = Resource("testResources/status/HPColorLaserjetMFPM283fdw.xml").readText()
        mockEngine.enqueue {
            respond(
                statusResource,
                HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "text/xml; charset=utf-8"))
        }

        val client = HttpClient(mockEngine)
        val testedESCLClient = ESCLRequestClient(Url("http://test.local/eSCL/"), client)

        // Success cases
        testedESCLClient.getScannerCapabilities()
            .shouldBeInstanceOf<ScannerCapabilitiesResult.Success>()

        testedESCLClient.getScannerCapabilities()
            .shouldBeInstanceOf<ScannerCapabilitiesResult.Success>()

        testedESCLClient.getScannerStatus()
            .shouldBeInstanceOf<ESCLRequestClient.ScannerStatusResult.Success>()
    }

    test("testLocationJobURIInsteadOfURL") {
        val mockEngine = MockEngine {
            respond(
                "",
                HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.Location, "/eSCL/ScanJobs/123")
            )
        }

        val client = HttpClient(mockEngine)

        val testedESCLClient = ESCLRequestClient(Url("http://localhost.test/eSCL"), client)
        val result = testedESCLClient.createJob(ScanSettings(version = "2.63"))

        println(result)

        result.shouldBeInstanceOf<ESCLRequestClient.ScannerCreateJobResult.Success>()

        println(result.scanJob)

        result.scanJob.jobUri shouldBe "/eSCL/ScanJobs/123/"
    }
})