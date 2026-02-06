import com.goncalossilva.resources.Charset
import com.goncalossilva.resources.Resource
import io.github.chrisimx.esclkt.AdfState
import io.github.chrisimx.esclkt.ESCLXml
import io.github.chrisimx.esclkt.JobInfo
import io.github.chrisimx.esclkt.JobState
import io.github.chrisimx.esclkt.ScannerState
import io.github.chrisimx.esclkt.ScannerStatus
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.charsets.Charsets

class ScannerStatusXMLProcessingTest: StringSpec({
    "Parse example1 scanner status" {
        val esclXml = ESCLXml()

        Resource("testResources/status/example1.xml").readText().let { text ->
            val scannerStatus = esclXml.decodeFromString<ScannerStatus>(text)
            scannerStatus shouldBe ScannerStatus(
                version = "2.62",
                state = ScannerState.Idle,
                jobs = listOf(),
                adfState = AdfState.ScannerAdfEmpty,
            )
            println("ScannerStatus Deserialization example 1 returned:\n   $scannerStatus")
        }
    }

    "Parse example2 scanner status" {
        val esclXml = ESCLXml()

        Resource("testResources/status/example2.xml").readText().let { text ->
            val scannerStatus = esclXml.decodeFromString<ScannerStatus>(text)
            scannerStatus shouldBe ScannerStatus(
                version = "2.6",
                state = ScannerState.Processing,
                jobs = listOf(
                    JobInfo(
                        "/ScanJobs/893e6fcd-487f-4056-a8c9-a87709b85daf",
                        "893e6fcd-487f-4056-a8c9-a87709b85daf",
                        10u,
                        1u,
                        1u,
                        29u,
                        JobState.Processing,
                        listOf("JobScanning"),
                    ),
                    JobInfo(
                        "/ScanJobs/898d6fcd-487f-4056-a8c9-a87709b85daf",
                        "898d6fcd-487f-4056-a8c9-a87709b85daf",
                        220u,
                        5u,
                        0u,
                        null,
                        JobState.Completed,
                        listOf("JobCompletedSuccessfully"),
                    ),
                ),
            )
            println("ScannerStatus Deserialization example 2 returned:\n   $scannerStatus")
        }
    }

    "Parse scanner status HPColorLaserjetMFPM283fdw" {
        val esclXml = ESCLXml()

        Resource("testResources/status/HPColorLaserjetMFPM283fdw.xml").readText().let { text ->
            val scannerStatus = esclXml.decodeFromString<ScannerStatus>(text)
            println("ScannerStatus Deserialization example 3 (HP Color Laserjet MFPM283fdw) returned:\n   $scannerStatus")
        }
    }
})