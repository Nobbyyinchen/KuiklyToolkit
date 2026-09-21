package io.github.nobbyyinchen.kuikly.toolkit.sample

import io.github.nobbyyinchen.kuikly.serialization.EmptyStringAsFalseSerializer
import io.github.nobbyyinchen.kuikly.serialization.EmptyStringAsListSerializer
import io.github.nobbyyinchen.kuikly.serialization.EmptyStringAsZeroIntSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject

object StringListFromEmptyStringSerializer : KSerializer<List<String>> by
    EmptyStringAsListSerializer(String.serializer())

data class LenientPayload(
    val count: Int = 0,
    val enabled: Boolean = false,
    val tags: List<String> = emptyList(),
)

private val exampleJson = Json { ignoreUnknownKeys = true }

fun decodeLenientPayload(jsonText: String): LenientPayload {
    val payload = exampleJson.parseToJsonElement(jsonText).jsonObject
    return LenientPayload(
        count = exampleJson.decodeFromJsonElement(
            EmptyStringAsZeroIntSerializer,
            payload["count"] ?: JsonNull,
        ),
        enabled = exampleJson.decodeFromJsonElement(
            EmptyStringAsFalseSerializer,
            payload["enabled"] ?: JsonNull,
        ),
        tags = exampleJson.decodeFromJsonElement(
            StringListFromEmptyStringSerializer,
            payload["tags"] ?: JsonNull,
        ),
    )
}
