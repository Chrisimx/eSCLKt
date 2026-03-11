plugins {
    kotlin("multiplatform") apply false
    alias(libs.plugins.kotlinCocoapods) apply false
    id("com.vanniktech.maven.publish") version "0.36.0" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}
