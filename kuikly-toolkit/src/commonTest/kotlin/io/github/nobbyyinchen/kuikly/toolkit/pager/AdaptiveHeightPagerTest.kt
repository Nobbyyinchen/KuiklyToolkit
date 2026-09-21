package io.github.nobbyyinchen.kuikly.toolkit.pager

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveHeightPagerTest {
    @Test
    fun interpolatesAdjacentPageHeights() {
        assertEquals(100f, interpolatePageHeight(listOf(100f, 200f, 140f), 0f, 300f))
        assertEquals(150f, interpolatePageHeight(listOf(100f, 200f, 140f), 150f, 300f))
        assertEquals(200f, interpolatePageHeight(listOf(100f, 200f, 140f), 300f, 300f))
        assertEquals(170f, interpolatePageHeight(listOf(100f, 200f, 140f), 450f, 300f))
    }

    @Test
    fun clampsOffsetsAndInvalidDimensions() {
        assertEquals(0f, interpolatePageHeight(emptyList(), 0f, 300f))
        assertEquals(0f, interpolatePageHeight(listOf(100f), 0f, 0f))
        assertEquals(100f, interpolatePageHeight(listOf(100f, 200f), -100f, 300f))
        assertEquals(200f, interpolatePageHeight(listOf(100f, 200f), 900f, 300f))
    }

    @Test
    fun negativeHeightsAreTreatedAsZero() {
        assertEquals(0f, interpolatePageHeight(listOf(-10f), 0f, 300f))
        assertEquals(50f, interpolatePageHeight(listOf(-10f, 100f), 150f, 300f))
    }
}

