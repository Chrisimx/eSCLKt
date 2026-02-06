import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jreleaser.model.Active
import org.jreleaser.model.Changelog
import org.jreleaser.model.Signing

plugins {
    kotlin("multiplatform") version libs.versions.kotlin.get()
    alias(libs.plugins.kotlin.serialization)
    id("maven-publish")
    alias(libs.plugins.jreleaser)
    alias(libs.plugins.dokka)
    id("signing")
    alias(libs.plugins.versions)
}

kotlin {
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinxSerializationJson)
                implementation("io.ktor:ktor-client-core:3.4.0")
                implementation("io.ktor:ktor-client-content-negotiation:3.4.0")
                implementation("io.ktor:ktor-client-mock:3.4.0")
                implementation("io.ktor:ktor-serialization-kotlinx-xml:3.4.0")
                implementation("io.github.pdvrieze.xmlutil:core:0.91.3")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmMain by getting {
            dependencies {
                // JVM engine for Ktor
                implementation("io.ktor:ktor-client-cio:2.3.7")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(libs.mockWebserver)
            }
        }
    }
}

KotlinPlatformType.setupAttributesMatchingStrategy(dependencies.attributesSchema)

group = "io.github.chrisimx"
version = "1.5.5"

publishing {
    publications {
        withType<MavenPublication> {
            groupId = group.toString()
            artifactId = "esclkt"
            pom {
                packaging = "jar"
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
        active.set(Active.ALWAYS)
        mode.set(Signing.Mode.MEMORY)
        armored.set(true)
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
