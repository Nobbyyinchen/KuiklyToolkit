plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    androidTarget()
    js(IR) { browser() }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":kuikly-toolkit"))
            implementation(project(":kuikly-lenient-serialization"))
        }
    }
}

android {
    namespace = "io.github.nobbyyinchen.kuikly.toolkit.sample"
    compileSdk = 34
    defaultConfig { minSdk = 21 }
}
