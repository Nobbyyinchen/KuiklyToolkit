plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

val localSerializationRoot = providers.gradleProperty("LOCAL_SERIALIZATION_ROOT").orNull

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
            implementation("com.tencent.kuikly-open:core:${providers.gradleProperty("KUIKLY_VERSION").get()}")
            if (localSerializationRoot == null) {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${providers.gradleProperty("SERIALIZATION_VERSION").get()}")
            }
        }
    }
}

android {
    namespace = "io.github.nobbyyinchen.kuikly.toolkit.sample"
    compileSdk = 34
    defaultConfig { minSdk = 21 }
}
