package io.github.nobbyyinchen.kuikly.toolkit.sample

import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import io.github.nobbyyinchen.kuikly.toolkit.debug.DebugConsole
import io.github.nobbyyinchen.kuikly.toolkit.debug.DebugLogLevel
import io.github.nobbyyinchen.kuikly.toolkit.debug.DebugLogStore
import io.github.nobbyyinchen.kuikly.toolkit.image.StableImage
import io.github.nobbyyinchen.kuikly.toolkit.lazy.LazyMountContainer
import io.github.nobbyyinchen.kuikly.toolkit.pager.AdaptiveHeightPager
import io.github.nobbyyinchen.kuikly.toolkit.waterfall.PaginatedWaterfall
import io.github.nobbyyinchen.kuikly.toolkit.waterfall.PaginationLoadState

data class PagerExampleItem(
    val title: String,
    val height: Float,
    val color: Color,
)

private val pagerItems = listOf(
    PagerExampleItem("Short page", 120f, Color(0xFF5B8FF9)),
    PagerExampleItem("Tall page", 220f, Color(0xFF61DDAA)),
    PagerExampleItem("Medium page", 160f, Color(0xFF65789B)),
)

val exampleDebugLogStore = DebugLogStore(
    maxLogSize = 500,
    pageNameProvider = { "toolkit-example" },
)

/** Source-only examples that can be embedded in an existing Kuikly Pager. */
fun ViewContainer<*, *>.ToolkitExamples(
    copyToClipboard: (String) -> Unit = {},
) {
    View {
        attr {
            width(375f)
            flexDirectionColumn()
        }

        AdaptiveHeightPager {
            attr {
                pageWidth = 375f
                initPageItems(pagerItems, height = { it.height }) { item ->
                    View {
                        attr {
                            size(375f, item.height)
                            backgroundColor(item.color)
                            allCenter()
                        }
                        Text {
                            attr {
                                text(item.title)
                                color(Color.WHITE)
                                fontSize(18f)
                            }
                        }
                    }
                }
            }
            event {
                heightDidChange { height ->
                    exampleDebugLogStore.append(
                        tag = "pager",
                        message = "height=$height",
                        level = DebugLogLevel.DEBUG,
                    )
                }
            }
        }

        StableImage {
            attr {
                marginTop(16f)
                size(120f, 80f)
                fallbackSrc("assets://toolkit/image-placeholder.png")
                src("https://example.com/image.png")
                hideFallbackOnSuccess(true)
                image { resizeCover() }
            }
            event {
                loadFailure {
                    exampleDebugLogStore.append("image", "load failed", DebugLogLevel.ERROR)
                }
            }
        }

        LazyMountContainer {
            attr {
                marginTop(16f)
                lazy = true
                placeholderHeight = 180f
                preloadOffset = 240f
                placeholder {
                    View {
                        attr {
                            height(180f)
                            backgroundColor(Color(0xFFF2F3F5))
                        }
                    }
                }
                content {
                    View {
                        attr {
                            height(180f)
                            backgroundColor(Color(0xFFE6F7FF))
                            allCenter()
                        }
                        Text {
                            attr {
                                text("Mounted once near the viewport")
                                color(Color(0xFF1D2129))
                                fontSize(15f)
                            }
                        }
                    }
                }
            }
        }

        PaginatedWaterfall<PagerExampleItem> {
            val waterfall = this
            attr {
                marginTop(16f)
                width = 375f
                height = 280f
                columnCount = 2
                pageSize = 2
                preloadDistance = 120f
                items { item, _ ->
                    View {
                        attr {
                            height(item.height)
                            borderRadius(10f)
                            backgroundColor(item.color)
                            allCenter()
                        }
                        Text {
                            attr {
                                text(item.title)
                                color(Color.WHITE)
                                fontSize(15f)
                            }
                        }
                    }
                }
                state { state, hasItems, hasMore ->
                    if (state == PaginationLoadState.LOADING) {
                        Text { attr { text("Loading…") } }
                    } else if (hasItems && !hasMore) {
                        Text { attr { text("No more items") } }
                    }
                }
            }
            event {
                requestPage { request ->
                    val start = (request.page - 1) * request.pageSize
                    val pageItems = pagerItems.drop(start).take(request.pageSize)
                    waterfall.submitPage(
                        request = request,
                        items = pageItems,
                        hasMore = start + pageItems.size < pagerItems.size,
                    )
                }
            }
        }

        DebugConsole {
            attr {
                store(exampleDebugLogStore)
            }
            event {
                copyRequested { copyToClipboard(it.displayText) }
            }
        }
    }
}
