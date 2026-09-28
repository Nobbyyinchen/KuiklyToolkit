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
