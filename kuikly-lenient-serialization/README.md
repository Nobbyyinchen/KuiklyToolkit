# KuiklyLenientSerialization

按字段处理异常 JSON 的 KMP kotlinx.serialization 适配器。仅依赖 Kotlin 与 kotlinx.serialization，**不依赖 Kuikly UI**。

[中文首页](../README.md) · [English overview](../README.en.md) · [Quick start](../docs/QUICK_START.md) · [Example](../sample/src/commonMain/kotlin/io/github/nobbyyinchen/kuikly/toolkit/sample/SerializationExample.kt)

Maven Central 尚未发布；当前使用模块或 commonMain 源码集成。标准构建使用 kotlinx.serialization-json 1.7.3。

## Minimal example

```kotlin
import io.github.nobbyyinchen.kuikly.serialization.EmptyStringAsZeroIntSerializer
import kotlinx.serialization.json.Json

val count = Json.decodeFromString(EmptyStringAsZeroIntSerializer, "\"\"")
check(count == 0)
```

In a serializable model, apply an adapter only to fields that require tolerance:

```kotlin
@Serializable
data class Payload(
    @Serializable(with = EmptyStringAsZeroIntSerializer::class)
    val count: Int = 0,
    @Serializable(with = EmptyStringAsFalseSerializer::class)
    val enabled: Boolean = false,
)
```

This model syntax also requires the normal Kotlin serialization compiler plugin.

## Behavior

- Numeric adapters use their configured defaults for empty strings, null and unparseable primitives.
- Boolean adapters accept empty values, 0/1 and true/false; see source and tests for error behavior.
- List, Map and Object adapters preserve failure for non-empty values of an incorrect type.
- Generic containers require a caller-supplied KSerializer for their contents.
- Encoding follows each adapter's documented value type; this is not a global JSON parser replacement.

[Source](src/commonMain/kotlin/io/github/nobbyyinchen/kuikly/serialization/LenientSerializers.kt) · [Tests](src/commonTest/kotlin/io/github/nobbyyinchen/kuikly/serialization/LenientSerializersTest.kt) · [Platform matrix](../docs/COMPATIBILITY.md)
