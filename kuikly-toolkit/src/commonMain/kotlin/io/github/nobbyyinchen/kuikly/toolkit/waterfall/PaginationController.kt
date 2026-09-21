/*
 * MIT License
 * Copyright (c) 2026 KuiklyToolkit contributors
 */
package io.github.nobbyyinchen.kuikly.toolkit.waterfall

enum class PaginationLoadState {
    IDLE,
    LOADING,
    COMPLETE,
    ERROR,
}

data class PageRequest(
    val generation: Int,
    val page: Int,
    val pageSize: Int,
    val pageToken: String,
    val refresh: Boolean,
)

/**
 * Platform-independent pagination state machine. Generation matching prevents
 * a slow response from an earlier refresh from overwriting newer data.
 */
class PaginationController<T>(
    private val initialPage: Int = 1,
    private val pageSize: Int = 20,
    private val maxPageCount: Int = 0,
) {
    private val mutableItems = mutableListOf<T>()
    private var generation = 0
    private var activeRequest: PageRequest? = null

    val items: List<T>
        get() = mutableItems

    var currentPage: Int = initialPage - 1
        private set

    var pageToken: String = ""
        private set

    var hasMore: Boolean = true
        private set

    var loadState: PaginationLoadState = PaginationLoadState.IDLE
        private set

    val isLoading: Boolean
        get() = activeRequest != null

    fun refresh(): PageRequest {
        generation += 1
        val request = PageRequest(
            generation = generation,
            page = initialPage,
            pageSize = pageSize.coerceAtLeast(1),
            pageToken = "",
            refresh = true,
        )
        activeRequest = request
        loadState = PaginationLoadState.LOADING
        return request
    }

    fun requestNextPage(): PageRequest? {
        if (activeRequest != null || !hasMore) return null
        val nextPage = if (mutableItems.isEmpty()) initialPage else currentPage + 1
        if (maxPageCount > 0 && nextPage - initialPage + 1 > maxPageCount) {
            hasMore = false
            loadState = PaginationLoadState.COMPLETE
            return null
        }

        generation += 1
        val request = PageRequest(
            generation = generation,
            page = nextPage,
            pageSize = pageSize.coerceAtLeast(1),
            pageToken = pageToken,
            refresh = mutableItems.isEmpty(),
        )
        activeRequest = request
        loadState = PaginationLoadState.LOADING
        return request
    }

    fun complete(
        request: PageRequest,
        newItems: List<T>,
        nextPageToken: String = "",
        hasMore: Boolean = true,
    ): Boolean {
        if (request != activeRequest) return false
        if (request.refresh) mutableItems.clear()
        mutableItems.addAll(newItems)
        currentPage = request.page
        pageToken = nextPageToken
        val belowLimit = maxPageCount <= 0 || request.page - initialPage + 1 < maxPageCount
        this.hasMore = hasMore && belowLimit
        activeRequest = null
        loadState = PaginationLoadState.COMPLETE
        return true
    }

    fun fail(request: PageRequest): Boolean {
        if (request != activeRequest) return false
        activeRequest = null
        loadState = PaginationLoadState.ERROR
        return true
    }
}
