/*
 * MIT License
 * Copyright (c) 2026 KuiklyToolkit contributors
 */
package io.github.nobbyyinchen.kuikly.toolkit.pager

import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.event.EventHandlerFn
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.PageList
import com.tencent.kuikly.core.views.PageListEvent
import com.tencent.kuikly.core.views.PageListView
import com.tencent.kuikly.core.views.ScrollParams
import com.tencent.kuikly.core.views.View
import kotlin.math.ceil
import kotlin.math.floor

/** Calculates the interpolated height for a bounded horizontal pager. */
fun interpolatePageHeight(pageHeights: List<Float>, offsetX: Float, pageWidth: Float): Float {
    if (pageHeights.isEmpty() || pageWidth <= 0f) return 0f
    if (pageHeights.size == 1) return pageHeights.first().coerceAtLeast(0f)

    val rawPage = (offsetX / pageWidth).coerceIn(0f, pageHeights.lastIndex.toFloat())
    val fromIndex = floor(rawPage.toDouble()).toInt().coerceIn(pageHeights.indices)
    val toIndex = ceil(rawPage.toDouble()).toInt().coerceIn(pageHeights.indices)
    val progress = rawPage - fromIndex
    val fromHeight = pageHeights[fromIndex].coerceAtLeast(0f)
    val toHeight = pageHeights[toIndex].coerceAtLeast(0f)
    return fromHeight + (toHeight - fromHeight) * progress
}

class AdaptiveHeightPagerAttr : ComposeAttr() {
    var pageWidth: Float by observable(0f)
    var scrollEnable: Boolean by observable(true)
    var defaultPageIndex: Int by observable(0)
    var keepItemAlive: Boolean by observable(true)

    internal var pageHeights: List<Float> = emptyList()
    internal var pageItemsCreator: (PageListView<*, *>.() -> Unit)? = null

    /**
     * Supplies a bounded page list and its natural heights. The input list is copied so later
     * mutations cannot desynchronise page indices and heights.
     */
    fun <T> initPageItems(
        dataList: List<T>,
        height: (T) -> Float,
        creator: AdaptiveHeightPagerItemCreator<T>,
    ) {
        val items = dataList.toList()
        pageHeights = items.map { height(it).coerceAtLeast(0f) }
        pageItemsCreator = {
            items.forEach { item -> creator(item) }
        }
    }

    internal fun initialHeight(): Float {
        if (pageHeights.isEmpty()) return 0f
        return pageHeights[defaultPageIndex.coerceIn(pageHeights.indices)]
    }
}

class AdaptiveHeightPagerEvent : ComposeEvent() {
    internal var heightDidChangeHandler: ((Float) -> Unit)? = null

    fun heightDidChange(handler: (Float) -> Unit) {
        heightDidChangeHandler = handler
    }

    fun scroll(handler: (ScrollParams) -> Unit) {
        registerEvent(SCROLL_EVENT) { params ->
            (params as? ScrollParams)?.let(handler)
        }
    }

    fun pageIndexDidChanged(handler: EventHandlerFn) {
        register(PageListEvent.PageListEventConst.PAGE_INDEX_DID_CHANGED, handler)
    }

    internal companion object {
        const val SCROLL_EVENT = "adaptiveHeightPagerScroll"
    }
}

/** A PageList whose clipped viewport height follows the current horizontal page offset. */
class AdaptiveHeightPagerView : ComposeView<AdaptiveHeightPagerAttr, AdaptiveHeightPagerEvent>() {
    private var currentHeight by observable(0f)
    private var lastReportedHeight = -1f

    override fun createAttr() = AdaptiveHeightPagerAttr()

    override fun createEvent() = AdaptiveHeightPagerEvent()

    override fun body(): ViewBuilder {
        val ctx = this
        if (currentHeight <= 0f) {
            currentHeight = attr.initialHeight()
            reportHeight(currentHeight)
        }
        return {
            View {
                attr {
                    if (ctx.attr.pageWidth > 0f) width(ctx.attr.pageWidth)
                    height(ctx.currentHeight)
                    overflow(false)
                }
                PageList {
                    attr {
                        val maxHeight = ctx.attr.pageHeights.maxOrNull()?.coerceAtLeast(0f) ?: 0f
                        if (ctx.attr.pageWidth > 0f) {
                            width(ctx.attr.pageWidth)
                            pageItemWidth(ctx.attr.pageWidth)
                        }
                        if (maxHeight > 0f) {
                            height(maxHeight)
                            pageItemHeight(maxHeight)
                        }
                        scrollEnable(ctx.attr.scrollEnable && ctx.attr.pageHeights.size > 1)
                        defaultPageIndex(
                            if (ctx.attr.pageHeights.isEmpty()) 0
                            else ctx.attr.defaultPageIndex.coerceIn(ctx.attr.pageHeights.indices)
                        )
                        pageDirection(true)
                        showScrollerIndicator(false)
                        keepItemAlive(ctx.attr.keepItemAlive)
                    }
                    ctx.attr.pageItemsCreator?.let { apply(it) }
                    event {
                        scroll { params ->
                            ctx.updateHeight(params.offsetX)
                            ctx.event.onFireEvent(AdaptiveHeightPagerEvent.SCROLL_EVENT, params)
                        }
                        pageIndexDidChanged {
                            ctx.event.onFireEvent(PageListEvent.PageListEventConst.PAGE_INDEX_DID_CHANGED, it)
                        }
                    }
                }
            }
        }
    }

    private fun updateHeight(offsetX: Float) {
        val nextHeight = interpolatePageHeight(attr.pageHeights, offsetX, attr.pageWidth)
        if (nextHeight <= 0f || kotlin.math.abs(nextHeight - currentHeight) <= HEIGHT_EPSILON) return
        currentHeight = nextHeight
        reportHeight(nextHeight)
    }

    private fun reportHeight(height: Float) {
        if (height <= 0f || kotlin.math.abs(height - lastReportedHeight) <= HEIGHT_EPSILON) return
        lastReportedHeight = height
        event.heightDidChangeHandler?.invoke(height)
    }

    private companion object {
        const val HEIGHT_EPSILON = 0.1f
    }
}

typealias AdaptiveHeightPagerItemCreator<T> = PageListView<*, *>.(item: T) -> Unit

fun ViewContainer<*, *>.AdaptiveHeightPager(init: AdaptiveHeightPagerView.() -> Unit) {
    addChild(AdaptiveHeightPagerView(), init)
}

