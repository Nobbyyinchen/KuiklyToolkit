/*
 * MIT License
 * Copyright (c) 2026 KuiklyToolkit contributors
 */
package io.github.nobbyyinchen.kuikly.toolkit.sample

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import io.github.nobbyyinchen.kuikly.toolkit.image.StableImage
import io.github.nobbyyinchen.kuikly.toolkit.lazy.LazyMountContainer
import io.github.nobbyyinchen.kuikly.toolkit.waterfall.PaginatedWaterfall
import io.github.nobbyyinchen.kuikly.toolkit.waterfall.PaginatedWaterfallView
import io.github.nobbyyinchen.kuikly.toolkit.waterfall.PaginationLoadState

@Page("toolkit_image")
class StableImageDemoPage : Pager() {
    private var source by observable("assets://toolkit/success.png")
    private var status by observable("Blue source selected")

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
                event {
                    loadResolution { ctx.status = "Loaded local fixture" }
                    loadFailure { ctx.status = "Load failed; fallback remains visible" }
                }
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
