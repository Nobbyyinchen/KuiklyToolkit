pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.android.library") {
                useModule("com.android.tools.build:gradle:${requested.version}")
            }
        }
    }
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/") }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/") }
    }
}

rootProject.name = "KuiklyToolkit"
rootProject.buildFileName = "build.ohos.gradle.kts"

include(":kuikly-toolkit")
project(":kuikly-toolkit").buildFileName = "build.ohos.gradle.kts"

include(":kuikly-lenient-serialization")
project(":kuikly-lenient-serialization").buildFileName = "build.ohos.gradle.kts"

include(":sample")
project(":sample").buildFileName = "build.ohos.gradle.kts"
