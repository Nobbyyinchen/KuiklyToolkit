plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.google.devtools.ksp")
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
            implementation("com.tencent.kuikly-open:core-annotations:${providers.gradleProperty("KUIKLY_VERSION").get()}")
        }
    }
}

dependencies {
    add("kspAndroid", "com.tencent.kuikly-open:core-ksp:${providers.gradleProperty("KUIKLY_VERSION").get()}")
}

android {
    namespace = "io.github.nobbyyinchen.kuikly.toolkit.sample"
    compileSdk = 34
    defaultConfig { minSdk = 21 }
}
