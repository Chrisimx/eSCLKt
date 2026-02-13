val kspVersion: String by project

plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation("com.squareup:javapoet:1.12.1")
                implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
            }
            kotlin.srcDir("src/main/kotlin")
            resources.srcDir("src/main/resources")
        }
    }
}
