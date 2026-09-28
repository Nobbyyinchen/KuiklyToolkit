# Quick start / 快速接入

[中文首页](../README.md) · [English](../README.en.md)

## 1. 先运行独立 Android Demo

需要 JDK 17、Android SDK platform 34、Python 3。配置 ANDROID_HOME 或未提交的 local.properties。公开依赖首次下载需要网络。

```shell
git clone https://github.com/Nobbyyinchen/KuiklyToolkit.git
cd KuiklyToolkit
./gradlew :android-demo:assembleDebug
adb install -r android-demo/build/outputs/apk/debug/android-demo-debug.apk
```

Windows 将 ./gradlew 换为 gradlew.bat。在 Android Studio 中直接选择 android-demo 运行。菜单提供六个演示入口；[操作说明](DEMO.md)。

## 2. 接入已有 Kuikly 工程

当前 Maven Central 尚未发布，不提供无法解析的远程依赖安装命令。

使用标准 Kotlin 2.1.21 / Kuikly 2.23.2-2.1.21 工程时：

1. 将需要的 kuikly-toolkit 或 kuikly-lenient-serialization 模块目录复制到你的独立工程。
2. 在 settings.gradle.kts 中 include 对应模块。
3. 根工程声明 Kotlin Multiplatform 2.1.21 与 Android library 8.2.2 插件；这些模块读取的属性见本仓库 gradle.properties。
4. 为 UI 库配置 Google Maven、Maven Central 与下列公开 Kuikly 仓库；只用序列化库时不需要 Kuikly 仓库。
5. 在消费模块的 commonMain 中引用需要的 project 依赖。

```kotlin
// settings.gradle.kts
include(":kuikly-toolkit")
include(":kuikly-lenient-serialization")

// 公开 Kuikly 依赖源
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

// 消费模块 build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":kuikly-toolkit"))
            // 仅在需要 JSON 容错时添加。
            implementation(project(":kuikly-lenient-serialization"))
        }
    }
}
```

最小属性：

```properties
GROUP_ID=io.github.nobbyyinchen.kuikly
VERSION_NAME=0.1.0
KUIKLY_VERSION=2.23.2-2.1.21
SERIALIZATION_VERSION=1.7.3
```

可按需移除模块中未使用的 targets，保留与你的消费工程一致的目标。其他 Kotlin/Kuikly 组合需要重新验证，不能直接认定兼容。

另一种方式是复制所需 commonMain 源码并保留 MIT 许可；UI 源码需要公开 Kuikly core，序列化源码只需要 kotlinx.serialization-json。

## 3. 在任意 Kuikly Pager 中使用

使用传统 DSL 的 ViewBuilder。完整 import、API 与边界见[组件示例](COMPONENTS.md)。

数据、内容构建器、请求结果、日志源与图片 URI 由调用方提供。库不请求业务接口，也不依赖应用的路由、模型或私有平台桥接。

Demo 自身的 Android 宿主提供图片加载和剪贴板服务；库不会自动为消费应用安装这些服务。

## 4. 只使用序列化模块

不需要 Kuikly。添加公开 kotlinx.serialization-json 1.7.3 与模块源码后，可直接使用 KSerializer：

```kotlin
import io.github.nobbyyinchen.kuikly.serialization.EmptyStringAsZeroIntSerializer
import kotlinx.serialization.json.Json

val count = Json.decodeFromString(EmptyStringAsZeroIntSerializer, "\"\"")
check(count == 0)
```

只给需要容错的字段应用适配器。List/Map/Object 对非空错误类型保持失败；数值适配器的默认值行为见[模块说明](../kuikly-lenient-serialization/README.md)。

## 5. 验证本地源码

```shell
python3 scripts/check_independence.py
python3 scripts/check_docs.py
./gradlew :kuikly-toolkit:jsNodeTest :kuikly-lenient-serialization:jsNodeTest :android-demo:assembleDebug
```

Windows 使用 python 与 gradlew.bat。JS 测试验证算法和序列化规则，UI 需按[验证矩阵](COMPATIBILITY.md)单独验收。
