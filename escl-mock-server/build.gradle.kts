

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlinx.resources)
    id("com.vanniktech.maven.publish")
}

group = "io.github.chrisimx"
version = "1.0.1"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
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

    compilerOptions {
        freeCompilerArgs.add(
            "-opt-in=kotlin.uuid.ExperimentalUuidApi"
        )
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinxSerializationJson)
                implementation("io.ktor:ktor-server-core:${libs.versions.ktor.get()}")
                implementation("io.ktor:ktor-server-cio:${libs.versions.ktor.get()}")

                implementation(libs.kotlinxResources)
            }
        }
    }

    mavenPublishing {
        publishToMavenCentral(automaticRelease = true)

        signAllPublications()

        coordinates(group.toString(), "escl-mock-server", version.toString())

        pom {
            name.set("escl-mock-server")
            description.set("eSCL Mock Server: A small implementation of mock server for the eSCL protocol")
            url.set("https://github.com/Chrisimx/eSCLKt")
            inceptionYear.set("2026")
            licenses {
                license {
                    name.set("GPL-3.0-or-later")
                    url.set("https://www.gnu.org/licenses/gpl-3.0.html")
                }
            }
            developers {
                developer {
                    id.set("Chrisimx")
                    name.set("Christian Nagel")
                    email.set("chris.imx@online.de")
                }
            }
            scm {
                connection.set("scm:git:https://github.com/Chrisimx/eSCLKt.git")
                developerConnection.set("scm:git:ssh://github.com/Chrisimx/eSCLKt.git")
                url.set("https://github.com/Chrisimx/eSCLKt/")
            }
        }
    }
}