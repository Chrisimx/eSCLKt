plugins {
    kotlin("multiplatform") apply false
    alias(libs.plugins.kotlinCocoapods) apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}
