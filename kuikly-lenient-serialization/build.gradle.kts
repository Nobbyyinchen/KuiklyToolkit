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
    js(IR) {
        browser()
        nodejs()
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

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
        if (localSerializationRoot != null) {
            jsMain.dependencies {
                api(files("$localSerializationRoot/core-js.klib"))
                api(files("$localSerializationRoot/json-js.klib"))
            }
            androidMain.dependencies {
                api(files("$localSerializationRoot/core-jvm.jar"))
                api(files("$localSerializationRoot/json-jvm.jar"))
            }
        }
    }
}

android {
    namespace = "io.github.nobbyyinchen.kuikly.serialization"
    compileSdk = 34
    defaultConfig { minSdk = 21 }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("KuiklyLenientSerialization")
            description.set("Lenient kotlinx.serialization adapters for inconsistent JSON payloads in KMP apps.")
            url.set("https://github.com/Nobbyyinchen/KuiklyToolkit")
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                    distribution.set("repo")
                }
            }
        }
    }
}
