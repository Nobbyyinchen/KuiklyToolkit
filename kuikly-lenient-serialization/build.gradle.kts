plugins {
    kotlin("multiplatform")
    id("com.android.library")
    `maven-publish`
}

group = providers.gradleProperty("GROUP_ID").get()
version = providers.gradleProperty("VERSION_NAME").get()

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
            api("org.jetbrains.kotlinx:kotlinx-serialization-json:${providers.gradleProperty("SERIALIZATION_VERSION").get()}")
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
