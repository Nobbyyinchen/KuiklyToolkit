/*
 * MIT License
 * Copyright (c) 2026 KuiklyToolkit contributors
 */
package io.github.nobbyyinchen.kuikly.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer

/** Decodes an empty JSON string as an empty list. Other JSON values retain strict semantics. */
open class EmptyStringAsListSerializer<T>(elementSerializer: KSerializer<T>) :
    JsonTransformingSerializer<List<T>>(ListSerializer(elementSerializer)) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        if (element.isEmptyString()) JsonArray(emptyList()) else element
}

object EmptyStringAsStringListSerializer :
    KSerializer<List<String>> by EmptyStringAsListSerializer(String.serializer())

/**
 * Decodes an empty JSON string as an empty object. The target serializer should provide defaults
 * for fields that may be absent from that empty object.
 */
open class EmptyStringAsObjectSerializer<T : Any>(serializer: KSerializer<T>) :
    JsonTransformingSerializer<T>(serializer) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        if (element.isEmptyString()) JsonObject(emptyMap()) else element
}

/** Decodes an empty JSON string as an empty map. */
open class EmptyStringAsMapSerializer<K, V>(
    keySerializer: KSerializer<K>,
    valueSerializer: KSerializer<V>,
) : JsonTransformingSerializer<Map<K, V>>(MapSerializer(keySerializer, valueSerializer)) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        if (element.isEmptyString()) JsonObject(emptyMap()) else element
}

object EmptyStringAsStringMapSerializer : KSerializer<Map<String, String>> by
    EmptyStringAsMapSerializer(String.serializer(), String.serializer())

private class PrimitiveWithDefaultSerializer<T>(
    private val delegate: KSerializer<T>,
    private val defaultValue: T,
    private val parse: (String) -> T?,
) : KSerializer<T> {
    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: T) {
        delegate.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): T {
        val jsonDecoder = decoder as? JsonDecoder ?: return delegate.deserialize(decoder)
        val element = jsonDecoder.decodeJsonElement()
        val primitive = element as? JsonPrimitive
            ?: throw SerializationException("Expected a JSON primitive, but found $element")
        if (primitive === JsonNull) return defaultValue
        return parse(primitive.content) ?: defaultValue
    }
}

open class LenientBooleanSerializer(
    private val defaultValue: Boolean = false,
) : KSerializer<Boolean> {
    private val delegate = Boolean.serializer()
    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: Boolean) {
        delegate.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): Boolean {
        val jsonDecoder = decoder as? JsonDecoder ?: return delegate.deserialize(decoder)
        val element = jsonDecoder.decodeJsonElement()
        if (element === JsonNull) return defaultValue
        val primitive = element as? JsonPrimitive
            ?: throw SerializationException("Expected a Boolean JSON primitive, but found $element")
        return when (primitive.content.trim().lowercase()) {
            "" -> defaultValue
            "0", "false" -> false
            "1", "true" -> true
            else -> throw SerializationException("Invalid Boolean value: ${primitive.content}")
        }
    }
}

object EmptyStringAsFalseSerializer : KSerializer<Boolean> by LenientBooleanSerializer(false)
object EmptyStringAsTrueSerializer : KSerializer<Boolean> by LenientBooleanSerializer(true)

open class LenientIntSerializer(defaultValue: Int = 0) : KSerializer<Int> by
    PrimitiveWithDefaultSerializer(Int.serializer(), defaultValue, { it.trim().toIntOrNull() })

open class LenientLongSerializer(defaultValue: Long = 0L) : KSerializer<Long> by
    PrimitiveWithDefaultSerializer(Long.serializer(), defaultValue, { it.trim().toLongOrNull() })

open class LenientFloatSerializer(defaultValue: Float = 0f) : KSerializer<Float> by
    PrimitiveWithDefaultSerializer(Float.serializer(), defaultValue, { it.trim().toFloatOrNull() })

open class LenientDoubleSerializer(defaultValue: Double = 0.0) : KSerializer<Double> by
    PrimitiveWithDefaultSerializer(Double.serializer(), defaultValue, { it.trim().toDoubleOrNull() })

object EmptyStringAsZeroIntSerializer : KSerializer<Int> by LenientIntSerializer()
object EmptyStringAsZeroLongSerializer : KSerializer<Long> by LenientLongSerializer()
object EmptyStringAsZeroFloatSerializer : KSerializer<Float> by LenientFloatSerializer()
object EmptyStringAsZeroDoubleSerializer : KSerializer<Double> by LenientDoubleSerializer()

/** Converts JSON string, number and Boolean primitives to String; JSON null becomes an empty string. */
object PrimitiveAsStringSerializer : KSerializer<String> {
    private val delegate = String.serializer()
    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: String) {
        delegate.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): String {
        val jsonDecoder = decoder as? JsonDecoder ?: return delegate.deserialize(decoder)
        val element = jsonDecoder.decodeJsonElement()
        return when {
            element === JsonNull -> ""
            element is JsonPrimitive -> element.content
            else -> throw SerializationException("Expected a JSON primitive, but found $element")
        }
    }
}

private fun JsonElement.isEmptyString(): Boolean =
    this is JsonPrimitive && this !== JsonNull && isString && content.isEmpty()

// Migration aliases for projects already using the original names.
typealias StringAsListSerializer<T> = EmptyStringAsListSerializer<T>
typealias StringAsObjectSerializer<T> = EmptyStringAsObjectSerializer<T>
typealias StringAsMapSerializer<K, V> = EmptyStringAsMapSerializer<K, V>
typealias StringAsBooleanSerializer = LenientBooleanSerializer
typealias StringAsIntSerializer = LenientIntSerializer
typealias StringAsLongSerializer = LenientLongSerializer
typealias StringAsFloatSerializer = LenientFloatSerializer
typealias StringAsDoubleSerializer = LenientDoubleSerializer

object StringListSerializer : KSerializer<List<String>> by EmptyStringAsStringListSerializer
object StringMapSerializer : KSerializer<Map<String, String>> by EmptyStringAsStringMapSerializer
object StringAsBooleanFalseSerializer : KSerializer<Boolean> by EmptyStringAsFalseSerializer
object StringAsBooleanTrueSerializer : KSerializer<Boolean> by EmptyStringAsTrueSerializer
object StringAsIntDefaultSerializer : KSerializer<Int> by EmptyStringAsZeroIntSerializer
object StringAsLongDefaultSerializer : KSerializer<Long> by EmptyStringAsZeroLongSerializer
object StringAsFloatDefaultSerializer : KSerializer<Float> by EmptyStringAsZeroFloatSerializer
object StringAsDoubleDefaultSerializer : KSerializer<Double> by EmptyStringAsZeroDoubleSerializer
object PrimitiveAsStringDefaultSerializer : KSerializer<String> by PrimitiveAsStringSerializer
