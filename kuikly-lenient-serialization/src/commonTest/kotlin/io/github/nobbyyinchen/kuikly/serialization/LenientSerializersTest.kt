package io.github.nobbyyinchen.kuikly.serialization

import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LenientSerializersTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun emptyStringBecomesEmptyContainersAndDefaultObject() {
        assertEquals(
            emptyList(),
            json.decodeFromString(EmptyStringAsListSerializer(String.serializer()), "\"\""),
        )
        assertEquals(
            emptyMap(),
            json.decodeFromString(
                EmptyStringAsMapSerializer(String.serializer(), String.serializer()),
                "\"\"",
            ),
        )
        assertEquals(
            JsonObject(emptyMap()),
            json.decodeFromString(EmptyStringAsObjectSerializer(JsonObject.serializer()), "\"\""),
        )
    }

    @Test
    fun validContainersKeepStrictSerializationSemantics() {
        assertEquals(
            listOf("a", "b"),
            json.decodeFromString(EmptyStringAsListSerializer(String.serializer()), "[\"a\",\"b\"]"),
        )
        assertFailsWith<SerializationException> {
            json.decodeFromString(EmptyStringAsListSerializer(String.serializer()), "{}")
        }
    }

    @Test
    fun booleanAcceptsCommonBackendRepresentations() {
        assertEquals(false, json.decodeFromString(EmptyStringAsFalseSerializer, "\"\""))
        assertEquals(true, json.decodeFromString(EmptyStringAsTrueSerializer, "null"))
        assertEquals(false, json.decodeFromString(EmptyStringAsFalseSerializer, "0"))
        assertEquals(true, json.decodeFromString(EmptyStringAsFalseSerializer, "\"1\""))
        assertEquals(true, json.decodeFromString(EmptyStringAsFalseSerializer, "true"))
        assertFailsWith<SerializationException> {
            json.decodeFromString(EmptyStringAsFalseSerializer, "\"yes\"")
        }
    }

    @Test
    fun numericSerializersUseConfiguredDefaultsForMalformedPrimitives() {
        assertEquals(0, json.decodeFromString(EmptyStringAsZeroIntSerializer, "\"\""))
        assertEquals(7, json.decodeFromString(LenientIntSerializer(7), "\"invalid\""))
        assertEquals(42L, json.decodeFromString(EmptyStringAsZeroLongSerializer, "42"))
        assertEquals(1.5f, json.decodeFromString(EmptyStringAsZeroFloatSerializer, "\"1.5\""))
        assertEquals(2.75, json.decodeFromString(EmptyStringAsZeroDoubleSerializer, "2.75"))
        assertEquals(0.0, json.decodeFromString(EmptyStringAsZeroDoubleSerializer, "null"))
    }

    @Test
    fun primitiveAsStringHandlesStringsNumbersBooleansAndNull() {
        assertEquals("hello", json.decodeFromString(PrimitiveAsStringSerializer, "\"hello\""))
        assertEquals("12", json.decodeFromString(PrimitiveAsStringSerializer, "12"))
        assertEquals("true", json.decodeFromString(PrimitiveAsStringSerializer, "true"))
        assertEquals("", json.decodeFromString(PrimitiveAsStringSerializer, "null"))
        assertFailsWith<SerializationException> {
            json.decodeFromString(PrimitiveAsStringSerializer, "[]")
        }
    }

    @Test
    fun encodingRemainsCanonical() {
        assertEquals("5", json.encodeToString(EmptyStringAsZeroIntSerializer, 5))
        assertEquals("true", json.encodeToString(EmptyStringAsFalseSerializer, true))
        assertEquals("\"value\"", json.encodeToString(PrimitiveAsStringSerializer, "value"))
    }
}
