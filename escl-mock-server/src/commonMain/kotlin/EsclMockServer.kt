
package io.github.chrisimx.esclmockserver

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.appendPathSegments
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.request.uri
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.url
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.also
import kotlin.uuid.Uuid

class EsclMockServer(args: EsclMockServerArgs.Builder.() -> Unit): AutoCloseable {

    private var server: EmbeddedServer<*, *>? = null
    private var serverMutex: Mutex = Mutex()

    private val scanJobs = mutableMapOf<Uuid, ScanJob>()
    private val scanJobsMutex = Mutex()

    val args: EsclMockServerArgs

    private val supervisorJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + supervisorJob)

    init {
        val buildedArgs = EsclMockServerArgs.Builder().apply(args).build()
        this.args = buildedArgs

        val initialized = CompletableDeferred<Unit>(supervisorJob)

        scope.launch {
            serverMutex.withLock {
                if (server != null) return@withLock
                server = embeddedServer(CIO, port = buildedArgs.port.toInt()) {
                    module(this)
                }
                server?.start(wait = false)
            }
            initialized.complete(Unit)
        }

        // Wait for server initialization
        runBlocking {
            initialized.await()
        }
    }

    override fun close() {
        runBlocking {
            serverMutex.withLock {
                server?.stop()
                server = null
            }
            supervisorJob.cancelAndJoin()
        }
    }

    private fun module(app: Application) {
        app.routing {
            route(args.resourcePath) {
                get("/ScannerCapabilities") {
                    app.log.info("ScannerCaps downloaded")

                    call.respondText(
                        text = args.scannerCaps,
                        contentType = ContentType.Text.Xml,
                        status = HttpStatusCode.OK
                    )
                }

                post("/ScanJobs") {
                    val scanJob = ScanJob()
                    scanJobsMutex.withLock {
                        scanJobs[scanJob.uuid] = scanJob
                    }
                    val scanJobUrl = call.url {
                        appendPathSegments("/${scanJob.uuid}")
                    }
                    app.log.info("Scan job created: $scanJob at $scanJobUrl")
                    call.response.header(HttpHeaders.Location, scanJobUrl)
                    call.respond(HttpStatusCode.Created, null)
                }

                get("/ScanJobs/{uuid}/NextDocument") {
                    val uuidParam = call.parameters["uuid"]
                    if (uuidParam == null) {
                        call.respond(HttpStatusCode.BadRequest, "Missing UUID")
                        return@get
                    }

                    val uuid = try {
                        Uuid.parse(uuidParam)
                    } catch (_: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid UUID")
                        return@get
                    }

                    val fullUrl = call.request.uri
                    app.log.info("Document is retrieved: $fullUrl")

                    val job = scanJobsMutex.withLock {
                        scanJobs.getOrPut(uuid) { ScanJob() }.also { it.retrievedPages += 1u }
                    }

                    app.log.info("Document job data: $job")

                    // If retrieved pages exceed count, return 404
                    if (job.retrievedPages > args.count) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }

                    val imageBytes = args.servedImage
                    call.response.header(HttpHeaders.ContentLocation, fullUrl)
                    call.respondBytes(
                        bytes = imageBytes,
                        contentType = ContentType.Image.JPEG,
                        status = HttpStatusCode.OK
                    )
                }
            }
        }
    }
}
