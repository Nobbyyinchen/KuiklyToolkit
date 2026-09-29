package io.github.nobbyyinchen.kuikly.toolkit.debug

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DebugLogStoreTest {
    @Test
    fun collapsesAdjacentDuplicates() {
        val store = DebugLogStore()
        store.append("network", "request", DebugLogLevel.INFO, "home", "10:00")
        store.append("network", "request", DebugLogLevel.INFO, "home", "10:01")

        val entry = store.snapshot().single()
        assertEquals(2, entry.repeatCount)
        assertEquals("10:01", entry.timeText)
        assertEquals("[10:01][home][INFO][network][x2] request", entry.displayText)
    }

    @Test
    fun maintainsBoundedBufferAndNotifiesListeners() {
        val store = DebugLogStore(maxLogSize = 2)
        var notifications = 0
        val listenerId = store.addListener { notifications += 1 }

        store.append("a", "one")
        store.append("b", "two")
        store.append("c", "three")

        assertEquals(listOf("two", "three"), store.snapshot().map { it.message })
        assertEquals(4, notifications)
        store.removeListener(listenerId)
        store.clear()
        assertEquals(4, notifications)
    }

    @Test
    fun filtersByKeywordPageTagAndLevel() {
        val store = DebugLogStore()
        store.append("network", "loaded profile", DebugLogLevel.INFO, "home")
        store.append("db", "profile failed", DebugLogLevel.ERROR, "detail")

        assertEquals(2, store.search(keyword = "profile").size)
        assertEquals(1, store.search(pageName = "home").size)
        assertEquals(1, store.search(tag = "db").size)
        assertTrue(store.search(minimumLevel = DebugLogLevel.ERROR).all { it.level == DebugLogLevel.ERROR })
    }
}

