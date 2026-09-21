plugins {
    kotlin("multiplatform")
    id("com.android.library")
    `maven-publish`
}

group = providers.gradleProperty("GROUP_ID").get()
version = providers.gradleProperty("VERSION_NAME").get()
val localSerializationRoot = providers.gradleProperty("LOCAL_SERIALIZATION_ROOT").orNull

kotlin {
    androidTarget { publishLibraryVariants("release") }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    ohosArm64()

    sourceSets {
        commonMain.dependencies {
            if (localSerializationRoot == null) {
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:${providers.gradleProperty("SERIALIZATION_VERSION").get()}")
            } else {
                api(files("$localSerializationRoot/core-metadata.jar"))
                api(files("$localSerializationRoot/json-metadata.jar"))
            }
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
