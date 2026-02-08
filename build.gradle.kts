import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jreleaser.model.Active
import org.jreleaser.model.Changelog
import org.jreleaser.model.Signing

plugins {
    kotlin("multiplatform") version libs.versions.kotlin.get()
    id("io.kotest") version libs.versions.kotest.get()
    id("com.google.devtools.ksp") version libs.versions.kotlin.get()
    alias(libs.plugins.kotlin.serialization)
    id("maven-publish")
    alias(libs.plugins.jreleaser)
    alias(libs.plugins.dokka)
    id("signing")
    jacoco
    alias(libs.plugins.versions)
    id("com.goncalossilva.resources") version "0.14.4"
}

jacoco {
    toolVersion = "0.8.14"
    //reportsDirectory = layout.buildDirectory.dir("customJacocoReportDir") // optional
}

val generateScannerCapIndex by tasks.registering {
    val capDir = file("src/commonTest/resources/testResources/capabilities")
    val outputFile = file("src/commonTest/resources/testResources/capabilities.txt")

    inputs.dir(capDir)
    outputs.file(outputFile)

    doLast {
        outputFile.parentFile.mkdirs()
        outputFile.writeText(
            capDir.listFiles()
                ?.filter { it.isFile }
                ?.joinToString("\n") { it.name } ?: ""
        )
        println("Generated index.txt with file list in ${outputFile.absolutePath}")
    }
}

tasks.matching { it.name.endsWith("ProcessResources") }.configureEach {
    dependsOn(generateScannerCapIndex)
}

tasks.matching { it.name.endsWith("CopyResources") }.configureEach {
    dependsOn(generateScannerCapIndex)
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.named("jvmTest"))

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    // Make sure the file tree points to the actual compiled classes
    classDirectories.setFrom(
        fileTree("${layout.buildDirectory}/classes/kotlin/jvm/main") {
            exclude(
                "**/R.class",
                "**/BuildConfig.*",
                "**/Manifest*.*",
                "**/*Test*.*"
            )
        }
    )

    // Source directories for HTML report mapping
    sourceDirectories.setFrom(
        files("src/jvmMain/kotlin"),
        files("src/commonMain/kotlin")
    )

    // Point to the exec data from the jvmTest task
    executionData.setFrom(
        files("${layout.buildDirectory}/jacoco/jvmTest.exec")
    )
}


kotlin {
    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(IR) {
        nodejs {
            testTask {
                useMocha {
                    timeout = "30s"
                    testLogging {
                        events("started", "passed", "skipped", "failed")
                        showStandardStreams = true
                    }
                }
            }
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()
    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()
    watchosSimulatorArm64()
    macosX64()
    macosArm64()

    linuxX64()
    linuxArm64()

    mingwX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinxSerializationJson)
                implementation("io.ktor:ktor-client-core:3.4.0")
                implementation("io.ktor:ktor-client-mock:3.4.0")
                implementation("io.ktor:ktor-serialization-kotlinx-xml:3.4.0")
                implementation("io.github.pdvrieze.xmlutil:core:0.91.3")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
                implementation("io.kotest:kotest-framework-engine:${libs.versions.kotest.get()}")
                implementation("io.kotest:kotest-assertions-core:${libs.versions.kotest.get()}")
                implementation("io.ktor:ktor-client-core:3.4.0")
                implementation("io.ktor:ktor-client-mock:3.4.0")
                implementation("com.goncalossilva:resources:0.14.4")
                implementation(kotlin("test"))
            }
        }

        val jvmMain by getting {
            dependencies {
                // JVM engine for Ktor
                implementation("io.ktor:ktor-client-cio:3.4.0")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation("io.kotest:kotest-framework-engine:${libs.versions.kotest.get()}")
                implementation("io.ktor:ktor-client-core:3.4.0")
                implementation("io.ktor:ktor-client-mock:3.4.0")
                implementation("io.kotest:kotest-assertions-core:${libs.versions.kotest.get()}")
                implementation("io.kotest:kotest-runner-junit5:${libs.versions.kotest.get()}")
                implementation("io.ktor:ktor-client-cio:3.4.0")
            }

        }

        val jsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-js:3.4.0")
            }
        }

        linuxMain.dependencies {
            implementation("io.ktor:ktor-client-curl:3.4.0")
        }

        appleMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:3.4.0")
        }
    }
}

KotlinPlatformType.setupAttributesMatchingStrategy(dependencies.attributesSchema)

group = "io.github.chrisimx"
version = "2.0.0"

publishing {
    publications {
        withType<MavenPublication> {
            pom {
                name.set("esclkt")
                description.set("eSCLKt: AirScan protocol (eSCL) in Kotlin")
                url.set("https://github.com/chrisimx/eSCLKt")
                inceptionYear.set("2024")
                licenses {
                    license {
                        name.set("GPL-3.0-or-later")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.html")
                    }
                }
                developers {
                    developer {
                        id.set("chrisimx")
                        name.set("Christian Nagel")
                        email.set("chris.imx@online.de")
                    }
                }
                scm {
                    connection.set("scm:git:git@github.com:Chrisimx/eSCLKt.git")
                    developerConnection.set("scm:git:ssh:git@github.com:Chrisimx/eSCLKt.git")
                    url.set("https://github.com/chrisimx/eSCLKt")
                }
            }
        }
    }
    repositories {
        maven {
            url =
                layout.buildDirectory
                    .dir("staging-deploy")
                    .get()
                    .asFile
                    .toURI()
        }
    }
}
jreleaser {
    project {
        name = rootProject.name
        versionPattern.set("SEMVER")
        copyright.set("Christian Nagel and contributors")
    }
    gitRootSearch.set(true)
    release {
        github {
            enabled = true
            draft = true

            changelog {
                enabled = true
                links = true
                sort.set(Changelog.Sort.DESC)
                formatted.set(Active.ALWAYS)
                preset.set("conventional-commits")
                format.set("- {{commitShortHash}} {{commitTitle}}")
                append {
                    // Enables appending to an existing changelog file.
                    // Defaults to `false`.
                    //
                    enabled = true
                }
                contributors {
                    enabled = false
                }
            }
        }
    }
    signing {
        pgp {
            active = Active.ALWAYS
            mode = Signing.Mode.MEMORY
            armored = true
        }

    }
    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    active.set(Active.ALWAYS)
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    stagingRepositories.add("build/staging-deploy")
                    applyMavenCentralRules = true
                    maxRetries = 200
                }
            }
        }
    }
}

repositories {
    mavenCentral()
}

tasks.withType<Test>().configureEach {
    logger.lifecycle("UP-TO-DATE check for $name is disabled, forcing it to run.")
    outputs.upToDateWhen { false }
}

/*tasks.test {
    useJUnitPlatform()
    val testOutputDir = "testOutput"
    mkdir(testOutputDir)
    workingDir("$testOutputDir/")
}

tasks.jar {
    enabled = true
    // Remove `plain` postfix from jar file name
    archiveClassifier.set("")
}*/

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}
