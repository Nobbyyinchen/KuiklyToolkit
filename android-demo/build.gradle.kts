plugins {
    id("com.android.application")
    kotlin("android")
}
kotlin { jvmToolchain(17) }
android {
    namespace = "io.github.nobbyyinchen.kuikly.toolkit.demo"
    compileSdk = 34
    defaultConfig {
        applicationId = "io.github.nobbyyinchen.kuikly.toolkit.demo"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = providers.gradleProperty("VERSION_NAME").get()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    implementation(project(":sample"))
    implementation("com.tencent.kuikly-open:core-render-android:${providers.gradleProperty("KUIKLY_VERSION").get()}")
    implementation("com.tencent.kuikly-open:core:${providers.gradleProperty("KUIKLY_VERSION").get()}")
}
