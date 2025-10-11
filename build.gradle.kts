import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jreleaser.model.Active
import org.jreleaser.model.Changelog
import org.jreleaser.model.Signing

plugins {
    kotlin("jvm") version "2.2.20"
    id("maven-publish")
    id("org.jreleaser") version "1.20.0"
    id("org.jetbrains.dokka") version "2.0.0"
    id("signing")
    id("com.github.ben-manes.versions") version "0.53.0"
}

java {
    withJavadocJar()
    withSourcesJar()
}
kotlin {
    target {
        attributes {
            if (KotlinPlatformType.attribute !in this) {
                attribute(KotlinPlatformType.attribute, KotlinPlatformType.androidJvm)
            }
        }
    }
}

KotlinPlatformType.setupAttributesMatchingStrategy(dependencies.attributesSchema)

group = "io.github.chrisimx"
version = "1.5.0"

publishing {
    publications {
        create<MavenPublication>("Maven") {
            from(components["java"])
            groupId = group.toString()
            artifactId = "esclkt"
            description =
                "An implementation of AirScan (eSCL) in Kotlin, making it easy to use network-attached scanners"
        }
        withType<MavenPublication> {
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

dependencies {
    implementation("com.squareup.okhttp3:okhttp:5.2.1")
    testImplementation(kotlin("test"))
    testImplementation("com.squareup.okhttp3:mockwebserver:5.2.1")
}

tasks.test {
    useJUnitPlatform()
    val testOutputDir = "testOutput"
    mkdir(testOutputDir)
    workingDir("$testOutputDir/")
}

tasks.jar {
    enabled = true
    // Remove `plain` postfix from jar file name
    archiveClassifier.set("")
}

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
