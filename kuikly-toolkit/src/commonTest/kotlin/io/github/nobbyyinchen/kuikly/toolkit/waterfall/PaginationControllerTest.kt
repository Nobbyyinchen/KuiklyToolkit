package io.github.nobbyyinchen.kuikly.toolkit.waterfall

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PaginationControllerTest {

    @Test
    fun successfulPagesAppendAndCarryToken() {
        val controller = PaginationController<String>(pageSize = 2)
        val first = controller.requestNextPage()!!
        assertEquals(1, first.page)
        assertTrue(controller.complete(first, listOf("A", "B"), "next"))

        val second = controller.requestNextPage()!!
        assertEquals(2, second.page)
        assertEquals("next", second.pageToken)
        assertTrue(controller.complete(second, listOf("C"), hasMore = false))
        assertEquals(listOf("A", "B", "C"), controller.items)
        assertFalse(controller.hasMore)
        assertNull(controller.requestNextPage())
    }

    @Test
    fun refreshRejectsAnOlderInFlightResponse() {
        val controller = PaginationController<String>()
        val oldRequest = controller.requestNextPage()!!
        val refreshRequest = controller.refresh()

        assertFalse(controller.complete(oldRequest, listOf("stale")))
        assertTrue(controller.complete(refreshRequest, listOf("fresh")))
        assertEquals(listOf("fresh"), controller.items)
    }

    @Test
    fun duplicateRequestsAndPagesBeyondLimitAreBlocked() {
        val controller = PaginationController<String>(maxPageCount = 1)
        val first = controller.requestNextPage()!!
        assertNull(controller.requestNextPage())
        assertTrue(controller.complete(first, listOf("A"), hasMore = true))
        assertFalse(controller.hasMore)
        assertNull(controller.requestNextPage())
    }

    @Test
    fun failedPageCanBeRetriedWithoutAdvancingPage() {
        val controller = PaginationController<String>()
        val first = controller.requestNextPage()!!
        assertTrue(controller.fail(first))
        assertEquals(PaginationLoadState.ERROR, controller.loadState)

        val retry = controller.requestNextPage()!!
        assertEquals(first.page, retry.page)
    }
}
