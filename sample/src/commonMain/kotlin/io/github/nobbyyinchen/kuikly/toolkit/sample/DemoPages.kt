/*
 * MIT License
 * Copyright (c) 2026 KuiklyToolkit contributors
 */
package io.github.nobbyyinchen.kuikly.toolkit.sample

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import io.github.nobbyyinchen.kuikly.toolkit.debug.DebugConsole
import io.github.nobbyyinchen.kuikly.toolkit.debug.DebugLogLevel
import io.github.nobbyyinchen.kuikly.toolkit.debug.DebugLogStore
import io.github.nobbyyinchen.kuikly.toolkit.image.StableImage
import io.github.nobbyyinchen.kuikly.toolkit.lazy.LazyMountContainer
import io.github.nobbyyinchen.kuikly.toolkit.pager.AdaptiveHeightPager
import io.github.nobbyyinchen.kuikly.toolkit.waterfall.PaginatedWaterfall
import io.github.nobbyyinchen.kuikly.toolkit.waterfall.PaginatedWaterfallView
import io.github.nobbyyinchen.kuikly.toolkit.waterfall.PaginationLoadState

/** Optional service supplied by the independent demo host, never by the libraries. */
object DemoPlatform {
    var copyToClipboard: (String) -> Unit = {}
}

private fun ViewContainer<*, *>.DemoText(value: String, large: Boolean = false) {
    Text {
        attr {
            text(value)
            fontSize(if (large) 22f else 15f)
            color(Color(0xFF172554))
            margin(16f)
        }
    }
}

private fun ViewContainer<*, *>.DemoButton(label: String, action: () -> Unit) {
    View {
        attr {
            height(44f)
            marginLeft(16f)
            marginRight(16f)
            marginBottom(8f)
            borderRadius(10f)
            backgroundColor(Color(0xFFE0EAFF))
            allCenter()
        }
        Text { attr { text(label); fontSize(15f); color(Color(0xFF1D4ED8)) } }
        event { click { action() } }
    }
}

@Page("toolkit_pager")
class AdaptivePagerDemoPage : Pager() {
    private var liveHeight by observable(120f)

    override fun body(): ViewBuilder {
        val ctx = this
        val width = (pageData.pageViewWidth - 32f).coerceAtLeast(1f)
        val cards = listOf(
            PagerExampleItem("01 / Short · 120", 120f, Color(0xFF2563EB)),
            PagerExampleItem("02 / Tall · 220", 220f, Color(0xFF0F766E)),
            PagerExampleItem("03 / Medium · 160", 160f, Color(0xFF7C3AED)),
        )
        return {
            attr { backgroundColor(Color.WHITE) }
            DemoText("AdaptiveHeightPager", large = true)
            DemoText("Drag slowly. The next section follows the interpolated height.")
            AdaptiveHeightPager {
                attr {
                    marginLeft(16f)
                    pageWidth = width
                    initPageItems(cards, height = { it.height }) { card ->
                        View {
                            attr { size(width, card.height); borderRadius(16f); backgroundColor(card.color); allCenter() }
                            Text { attr { text(card.title); fontSize(22f); color(Color.WHITE) } }
                        }
                    }
                }
                event { heightDidChange { ctx.liveHeight = it } }
            }
            View {
                attr { margin(16f); height(100f); borderRadius(12f); backgroundColor(Color(0xFFF1F5F9)); allCenter() }
                Text {
                    attr {
                        text("Following content\nHeight: ${ctx.liveHeight.toInt()}")
                        fontSize(18f)
                        color(Color(0xFF334155))
                    }
                }
            }
            DemoText("Heights are supplied by the caller. No automatic measurement or looping.")
        }
    }
}

@Page("toolkit_image")
class StableImageDemoPage : Pager() {
    private var source by observable("assets://toolkit/success.png")
    private var status by observable("Loading local fixture")

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr { backgroundColor(Color.WHITE) }
            DemoText("StableImage", large = true)
            DemoText("Local fixtures keep the demo repeatable without a network.")
            StableImage {
                attr {
                    margin(16f)
                    size(240f, 150f)
                    fallbackSrc("assets://toolkit/placeholder.png")
                    src(ctx.source)
                    hideFallbackOnSuccess(true)
                    image { resizeCover() }
                }
                event { loadFailure { ctx.status = "Load failed; fallback remains visible" } }
            }
            DemoText(ctx.status)
            DemoButton("Load blue image") { ctx.status = "Blue source selected"; ctx.source = "assets://toolkit/success.png" }
            DemoButton("Load missing image") { ctx.status = "Missing source selected"; ctx.source = "assets://toolkit/missing.png" }
            DemoButton("Switch to green image") { ctx.status = "Green source selected"; ctx.source = "assets://toolkit/alternate.png" }
        }
    }
}

@Page("toolkit_lazy")
class LazyMountDemoPage : Pager() {
    private var mountedCount by observable(0)

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr { backgroundColor(Color.WHITE) }
            DemoText("LazyMountContainer", large = true)
            DemoText("Mounted: ${ctx.mountedCount} / 8. Scroll down, then return.")
            Scroller {
                attr { flex(1f) }
                for (index in 1..8) {
                    LazyMountContainer {
                        attr {
                            margin(16f)
                            placeholderHeight = 200f
                            preloadOffset = 160f
                            placeholder {
                                View {
                                    attr { height(200f); backgroundColor(Color(0xFFE2E8F0)); allCenter() }
                                    Text { attr { text("Placeholder $index") } }
                                }
                            }
                            content {
                                View {
                                    attr { height(200f); borderRadius(12f); backgroundColor(Color(0xFFDBEAFE)); allCenter() }
                                    Text { attr { text("Card $index · mounted once"); fontSize(20f) } }
                                }
                            }
                        }
                        event { mounted { ctx.mountedCount += 1 } }
                    }
                }
            }
        }
    }
}

@Page("toolkit_waterfall")
class WaterfallDemoPage : Pager() {
    private var rejectedOnce = false
    private var waterfall: PaginatedWaterfallView<PagerExampleItem>? = null

    override fun body(): ViewBuilder {
        val ctx = this
        val width = (pageData.pageViewWidth - 32f).coerceAtLeast(1f)
        val height = (pageData.pageViewHeight - 220f).coerceAtLeast(160f)
        return {
            attr { backgroundColor(Color.WHITE) }
            DemoText("PaginatedWaterfall", large = true)
            DemoText("Scroll to load. Page 2 fails once; tap Retry.")
            DemoButton("Refresh") { ctx.rejectedOnce = false; ctx.waterfall?.refresh() }
            PaginatedWaterfall<PagerExampleItem> {
                ctx.waterfall = this
                val list = this
                attr {
                    marginLeft(16f)
                    this.width = width
                    this.height = height
                    pageSize = 6
                    columnCount = 2
                    items { item, _ ->
                        View {
                            attr { height(item.height); borderRadius(12f); backgroundColor(item.color); allCenter() }
                            Text { attr { text(item.title); color(Color.WHITE); fontSize(18f) } }
                        }
                    }
                    state { loadState, _, hasMore ->
                        if (loadState == PaginationLoadState.ERROR) {
                            DemoButton("Retry page 2") { list.retry() }
                        } else if (!hasMore) {
                            DemoText("End of 24 local cards")
                        }
                    }
                }
                event {
                    requestPage { request ->
                        if (request.page == 2 && !ctx.rejectedOnce) {
                            ctx.rejectedOnce = true
                            list.failPage(request)
                        } else {
                            val start = (request.page - 1) * request.pageSize
                            val items = (start until (start + request.pageSize).coerceAtMost(24)).map { index ->
                                PagerExampleItem("Card ${index + 1}", 100f + (index % 3) * 40f,
                                    if (index % 2 == 0) Color(0xFF2563EB) else Color(0xFF0F766E))
                            }
                            list.submitPage(request, items, hasMore = start + items.size < 24)
                        }
                    }
                }
            }
        }
    }
}

@Page("toolkit_console")
class DebugConsoleDemoPage : Pager() {
    private val logs = DebugLogStore(maxLogSize = 50, pageNameProvider = { "console-demo" }).apply {
        append("demo", "Ready")
        append("demo", "Ready")
        append("network", "Simulated failure", DebugLogLevel.ERROR)
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr { backgroundColor(Color.WHITE) }
            DemoText("DebugConsole", large = true)
            DemoText("Open the console button. Filter, fold, clear and copy logs.")
            DemoButton("Append repeated message") { ctx.logs.append("demo", "Repeated message") }
            DemoButton("Append warning") { ctx.logs.append("demo", "A sample warning", DebugLogLevel.WARN) }
            DebugConsole {
                attr { store(ctx.logs) }
                event { copyRequested { DemoPlatform.copyToClipboard(it.displayText) } }
            }
        }
    }
}

@Page("toolkit_serialization")
class SerializationDemoPage : Pager() {
    private var input by observable("""{"count":"","enabled":"","tags":""}""")
    private var result by observable("Tap Decode to inspect the result.")

    private fun decode() {
        result = runCatching { decodeLenientPayload(input).toString() }
            .getOrElse { "Rejected: ${it.message}" }
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr { backgroundColor(Color.WHITE) }
            DemoText("KMP lenient serialization", large = true)
            DemoText(ctx.input)
            DemoText(ctx.result)
            DemoButton("Decode empty-string fields") {
                ctx.input = """{"count":"","enabled":"","tags":""}"""; ctx.decode()
            }
            DemoButton("Decode valid fields") {
                ctx.input = """{"count":7,"enabled":true,"tags":["kmp"]}"""; ctx.decode()
            }
            DemoButton("Reject wrong list type") {
                ctx.input = """{"count":7,"enabled":true,"tags":42}"""; ctx.decode()
            }
            DemoText("Apply serializers per field. Non-empty wrong container types remain errors.")
        }
    }
}
