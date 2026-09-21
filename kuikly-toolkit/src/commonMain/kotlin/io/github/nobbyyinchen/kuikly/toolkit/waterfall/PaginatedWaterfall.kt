/*
 * MIT License
 * Copyright (c) 2026 KuiklyToolkit contributors
 */
package io.github.nobbyyinchen.kuikly.toolkit.waterfall

import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.directives.vbind
import com.tencent.kuikly.core.directives.vforIndex
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.ScrollParams
import com.tencent.kuikly.core.views.View
import com.tencent.kuikly.core.views.WaterfallList

typealias PaginatedWaterfallItemCreator<T> =
    ViewContainer<*, *>.(item: T, index: Int) -> Unit

typealias PaginatedWaterfallStateCreator =
    ViewContainer<*, *>.(state: PaginationLoadState, hasItems: Boolean, hasMore: Boolean) -> Unit

class PaginatedWaterfallAttr<T> : ComposeAttr() {
    var width: Float by observable(0f)
    var height: Float by observable(0f)
    var autoHeight: Boolean by observable(false)
    var columnCount: Int by observable(2)
    var lineSpacing: Float by observable(12f)
    var itemSpacing: Float by observable(12f)
    var scrollEnable: Boolean by observable(true)
    var preloadDistance: Float by observable(200f)
    var initialPage: Int = 1
    var pageSize: Int = 20
    var maxPageCount: Int = 0
    var autoLoadInitial: Boolean = true

    internal var itemCreator: PaginatedWaterfallItemCreator<T>? = null
    internal var stateCreator: PaginatedWaterfallStateCreator? = null

    fun items(creator: PaginatedWaterfallItemCreator<T>) {
        itemCreator = creator
    }

    /** Optional loading/error/end/empty UI rendered below the WaterfallList. */
    fun state(creator: PaginatedWaterfallStateCreator) {
        stateCreator = creator
    }
}

class PaginatedWaterfallEvent<T> : ComposeEvent() {
    internal var requestPageHandler: ((PageRequest) -> Unit)? = null
    internal var stateChangedHandler: ((PaginationLoadState) -> Unit)? = null

    fun requestPage(handler: (PageRequest) -> Unit) {
        requestPageHandler = handler
    }

    fun stateChanged(handler: (PaginationLoadState) -> Unit) {
        stateChangedHandler = handler
    }
}

/**
 * Business-neutral WaterfallList with guarded paging. Networking, parsing,
 * item content, empty/error UI, analytics and navigation stay with the caller.
 */
class PaginatedWaterfallView<T> :
    ComposeView<PaginatedWaterfallAttr<T>, PaginatedWaterfallEvent<T>>() {

    private val displayItems by observableList<T>()
    private var contentHeight: Float by observable(0f)
    private var currentLoadState: PaginationLoadState by observable(PaginationLoadState.IDLE)
    private var currentHasMore: Boolean by observable(true)
    private lateinit var controller: PaginationController<T>

    val items: List<T>
        get() = displayItems.toList()

    val loadState: PaginationLoadState
        get() = currentLoadState

    val hasMore: Boolean
        get() = currentHasMore

    override fun createAttr() = PaginatedWaterfallAttr<T>()

    override fun createEvent() = PaginatedWaterfallEvent<T>()

    override fun created() {
        super.created()
        controller = PaginationController(
            initialPage = attr.initialPage,
            pageSize = attr.pageSize,
            maxPageCount = attr.maxPageCount,
        )
    }

    override fun viewDidLoad() {
        super.viewDidLoad()
        if (attr.autoLoadInitial) refresh()
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    width(ctx.attr.width)
                    flexDirectionColumn()
                    if (!ctx.attr.autoHeight) height(ctx.attr.height)
                }

                WaterfallList {
                    attr {
                        width(ctx.attr.width)
                        height(if (ctx.attr.autoHeight) ctx.contentHeight else ctx.attr.height)
                        listWidth(ctx.attr.width)
                        columnCount(ctx.attr.columnCount.coerceAtLeast(1))
                        lineSpacing(ctx.attr.lineSpacing.coerceAtLeast(0f))
                        itemSpacing(ctx.attr.itemSpacing.coerceAtLeast(0f))
                        scrollEnable(ctx.attr.scrollEnable)
                        showScrollerIndicator(false)
                    }
                    event {
                        contentSizeChanged { _, height ->
                            if (ctx.attr.autoHeight && ctx.contentHeight != height) {
                                ctx.contentHeight = height
                            }
                        }
                        scroll { ctx.onListScrolled(it) }
                    }

                    vforIndex({ ctx.displayItems }) { item, index, _ ->
                        ctx.attr.itemCreator?.invoke(this, item, index)
                    }
                }

                ctx.attr.stateCreator?.let { creator ->
                    vbind({ ctx.currentLoadState }) {
                        creator(this, ctx.currentLoadState, ctx.displayItems.isNotEmpty(), ctx.currentHasMore)
                    }
                }
            }
        }
    }

    fun refresh() {
        dispatch(controller.refresh())
    }

    fun loadNextPage() {
        controller.requestNextPage()?.let(::dispatch)
        synchronizeState()
    }

    fun retry() {
        if (controller.items.isEmpty()) refresh() else loadNextPage()
    }

    fun submitPage(
        request: PageRequest,
        items: List<T>,
        nextPageToken: String = "",
        hasMore: Boolean = true,
    ): Boolean {
        val accepted = controller.complete(request, items, nextPageToken, hasMore)
        if (accepted) synchronizeState()
        return accepted
    }

    fun failPage(request: PageRequest): Boolean {
        val accepted = controller.fail(request)
        if (accepted) synchronizeState()
        return accepted
    }

    /** Call this when the waterfall is nested in a non-scrolling outer list. */
    fun onOuterListScrolled(params: ScrollParams) {
        maybeLoadMore(params)
    }

    private fun onListScrolled(params: ScrollParams) {
        if (attr.scrollEnable) maybeLoadMore(params)
    }

    private fun maybeLoadMore(params: ScrollParams) {
        val bottomDistance = params.contentHeight - params.viewHeight - params.offsetY
        if (bottomDistance <= attr.preloadDistance.coerceAtLeast(0f)) loadNextPage()
    }

    private fun dispatch(request: PageRequest) {
        synchronizeState()
        val handler = event.requestPageHandler
        if (handler == null) {
            controller.fail(request)
            synchronizeState()
        } else {
            handler(request)
        }
    }

    private fun synchronizeState() {
        if (displayItems.toList() != controller.items) {
            displayItems.clear()
            displayItems.addAll(controller.items)
        }
        currentHasMore = controller.hasMore
        if (currentLoadState != controller.loadState) {
            currentLoadState = controller.loadState
            event.stateChangedHandler?.invoke(currentLoadState)
        }
    }
}

fun <T> ViewContainer<*, *>.PaginatedWaterfall(
    init: PaginatedWaterfallView<T>.() -> Unit,
) {
    addChild(PaginatedWaterfallView<T>(), init)
}
