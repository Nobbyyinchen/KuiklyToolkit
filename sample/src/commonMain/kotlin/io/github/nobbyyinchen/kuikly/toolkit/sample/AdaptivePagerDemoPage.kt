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
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import io.github.nobbyyinchen.kuikly.toolkit.pager.AdaptiveHeightPager
import kotlin.math.roundToInt

private const val MENU_COLUMN_COUNT = 4
private const val MENU_ROW_HEIGHT = 60f

private data class MenuItem(
    val title: String,
    val symbol: String,
    val color: Color,
)

private data class MenuCategory(
    val title: String,
    val items: List<MenuItem>,
) {
    val rowCount: Int = (items.size + MENU_COLUMN_COUNT - 1) / MENU_COLUMN_COUNT
    val height: Float = rowCount * MENU_ROW_HEIGHT
}

@Page("toolkit_pager")
class AdaptivePagerDemoPage : Pager() {
    private var liveHeight by observable(120f)
    private var selectedCategoryIndex by observable(0)
    private var pendingCategoryIndex = 0

    override fun body(): ViewBuilder {
        val ctx = this
        val width = (pageData.pageViewWidth - 32f).coerceAtLeast(1f)
        val categories = menuCategories()
        return {
            attr { backgroundColor(Color.WHITE) }

            Text {
                attr {
                    height(38f)
                    marginTop(10f)
                    marginLeft(16f)
                    marginRight(16f)
                    text("AdaptiveHeightPager")
                    fontSize(22f)
                    fontWeightBold()
                    color(Color(0xFF172554))
                }
            }
            Text {
                attr {
                    height(42f)
                    marginLeft(16f)
                    marginRight(16f)
                    text("不同分类拥有不同数量的菜单。\n左右滑动时，下方内容会跟随菜单高度平滑移动。")
                    fontSize(14f)
                    color(Color(0xFF64748B))
                }
            }

            CategoryTabs(categories, ctx.selectedCategoryIndex, width)

            AdaptiveHeightPager {
                attr {
                    marginLeft(16f)
                    pageWidth = width
                    initPageItems(categories, height = { it.height }) { category ->
                        MenuGrid(category, width)
                    }
                }
                event {
                    heightDidChange { ctx.liveHeight = it }
                    scroll { params ->
                        ctx.pendingCategoryIndex = (params.offsetX / width)
                            .roundToInt()
                            .coerceIn(categories.indices)
                    }
                    pageIndexDidChanged { params ->
                        val reportedIndex = params as? Int
                        ctx.selectedCategoryIndex = (reportedIndex ?: ctx.pendingCategoryIndex)
                            .coerceIn(categories.indices)
                    }
                }
            }

            FeaturedContent(ctx.liveHeight)

            Text {
                attr {
                    height(34f)
                    marginLeft(16f)
                    marginRight(16f)
                    text("适用于首页菜单、分类入口、宫格导航等不同页面高度的横向切换场景。")
                    fontSize(12f)
                    color(Color(0xFF94A3B8))
                }
            }
        }
    }
}

private fun ViewContainer<*, *>.CategoryTabs(
    categories: List<MenuCategory>,
    selectedIndex: Int,
    width: Float,
) {
    View {
        attr {
            marginLeft(16f)
            width(width)
            height(42f)
            flexDirectionRow()
        }
        categories.forEachIndexed { index, category ->
            View {
                attr {
                    width(width / categories.size)
                    height(42f)
                    allCenter()
                }
                Text {
                    attr {
                        text(category.title)
                        fontSize(15f)
                        color(if (index == selectedIndex) Color(0xFF2563EB) else Color(0xFF64748B))
                        if (index == selectedIndex) fontWeightBold()
                    }
                }
                if (index == selectedIndex) {
                    View {
                        attr {
                            positionAbsolute()
                            bottom(0f)
                            width(28f)
                            height(3f)
                            borderRadius(2f)
                            backgroundColor(Color(0xFF2563EB))
                        }
                    }
                }
            }
        }
    }
}

private fun ViewContainer<*, *>.MenuGrid(category: MenuCategory, width: Float) {
    View {
        attr {
            width(width)
            height(category.height)
            flexDirectionColumn()
        }
        category.items.chunked(MENU_COLUMN_COUNT).forEach { rowItems ->
            View {
                attr {
                    width(width)
                    height(MENU_ROW_HEIGHT)
                    flexDirectionRow()
                }
                rowItems.forEach { item ->
                    View {
                        attr {
                            width(width / MENU_COLUMN_COUNT)
                            height(MENU_ROW_HEIGHT)
                            flexDirectionColumn()
                            alignItemsCenter()
                        }
                        View {
                            attr {
                                marginTop(4f)
                                size(32f, 32f)
                                borderRadius(10f)
                                backgroundColor(item.color)
                                allCenter()
                            }
                            Text {
                                attr {
                                    text(item.symbol)
                                    fontSize(13f)
                                    fontWeightBold()
                                    color(Color.WHITE)
                                }
                            }
                        }
                        Text {
                            attr {
                                marginTop(3f)
                                text(item.title)
                                fontSize(11f)
                                color(Color(0xFF334155))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun ViewContainer<*, *>.FeaturedContent(liveHeight: Float) {
    View {
        attr {
            height(30f)
            marginLeft(16f)
            marginRight(16f)
            flexDirectionRow()
            alignItemsCenter()
        }
        Text {
            attr {
                flex(1f)
                text("精选内容")
                fontSize(15f)
                fontWeightBold()
                color(Color(0xFF172554))
            }
        }
        Text {
            attr {
                text("Pager height: ${liveHeight.roundToInt()}")
                fontSize(11f)
                color(Color(0xFF94A3B8))
            }
        }
    }
    View {
        attr {
            height(76f)
            marginLeft(16f)
            marginRight(16f)
            marginBottom(4f)
            padding(12f)
            borderRadius(12f)
            backgroundColor(Color(0xFFF1F5F9))
            flexDirectionColumn()
        }
        Text {
            attr {
                text("今日推荐")
                fontSize(15f)
                fontWeightBold()
                color(Color(0xFF1E3A8A))
            }
        }
        Text {
            attr {
                marginTop(5f)
                text("这里会随着上方菜单高度连续移动，而不是在翻页后突然跳动。")
                fontSize(13f)
                color(Color(0xFF475569))
            }
        }
    }
}

private fun menuCategories(): List<MenuCategory> = listOf(
    MenuCategory(
        title = "推荐",
        items = menuItems("扫一扫", "付款码", "卡包", "出行", "手机充值", "快递", "天气", "更多"),
    ),
    MenuCategory(
        title = "服务",
        items = menuItems(
            "转账", "信用卡", "理财", "保险", "缴费", "医疗", "公积金", "社保",
            "电影", "酒店", "火车票", "外卖", "打车", "购物", "会员", "全部服务",
        ),
    ),
    MenuCategory(
        title = "生活",
        items = menuItems("美食", "电影", "旅行", "运动", "游戏", "阅读", "音乐", "摄影", "宠物", "更多"),
    ),
)

private fun menuItems(vararg titles: String): List<MenuItem> {
    val colors = listOf(
        Color(0xFF2563EB),
        Color(0xFF0F766E),
        Color(0xFF7C3AED),
        Color(0xFFEA580C),
    )
    return titles.mapIndexed { index, title ->
        MenuItem(title = title, symbol = title.take(1), color = colors[index % colors.size])
    }
}
