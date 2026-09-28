plugins {
    kotlin("multiplatform")
    id("com.android.library")
    `maven-publish`
}

group = providers.gradleProperty("GROUP_ID").get()
version = providers.gradleProperty("VERSION_NAME").get()

kotlin {
    androidTarget { publishLibraryVariants("release") }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    ohosArm64()

    sourceSets {
        commonMain.dependencies {
            api("org.jetbrains.kotlinx:kotlinx-serialization-json:${providers.gradleProperty("OHOS_SERIALIZATION_VERSION").get()}")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "io.github.nobbyyinchen.kuikly.serialization"
    compileSdk = 34
    defaultConfig { minSdk = 21 }
}
