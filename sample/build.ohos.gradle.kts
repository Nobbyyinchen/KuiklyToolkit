plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    ohosArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":kuikly-toolkit"))
            implementation(project(":kuikly-lenient-serialization"))
            implementation("com.tencent.kuikly-open:core:${providers.gradleProperty("KUIKLY_OHOS_VERSION").get()}")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${providers.gradleProperty("SERIALIZATION_VERSION").get()}")
        }
    }
}

android {
    namespace = "io.github.nobbyyinchen.kuikly.toolkit.sample"
    compileSdk = 34
    defaultConfig { minSdk = 21 }
}
