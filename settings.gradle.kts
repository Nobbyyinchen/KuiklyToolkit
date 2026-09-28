pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id in setOf("com.android.library", "com.android.application")) {
                useModule("com.android.tools.build:gradle:${requested.version}")
            }
        }
    }
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/")
            content { includeGroup("com.tencent.kuikly-open") }
        }
    }
}

rootProject.name = "KuiklyToolkit"
include(":kuikly-toolkit")
include(":kuikly-lenient-serialization")
include(":sample")
include(":android-demo")
