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

@Page("toolkit_console")
class DebugConsoleDemoPage : Pager() {
    private var lastAction by observable("等待操作")
    private var lastStatus by observable("Console 已预置页面初始化日志")

    private val logs = DebugLogStore(maxLogSize = 100, pageNameProvider = { "console-demo" }).apply {
        append("PAGE", "DebugConsoleDemo entered")
        append("ENV", "Kuikly demo initialized")
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr { backgroundColor(Color.WHITE) }
            Scroller {
                attr { flex(1f) }
                DemoText("DebugConsole", large = true)
                DemoText("在真机页面内查看接口调用、参数、返回值和异常信息。日志由业务代码主动写入。")
                SectionTitle("模拟业务操作")
                MockActionButton("获取用户信息", "GET /api/user/profile") { ctx.mockUserRequest() }
                MockActionButton("加载商品列表", "GET /api/product/list?page=1") { ctx.mockProductRequest() }
                MockActionButton("提交订单", "POST /api/order/create") { ctx.mockCreateOrder() }
                MockActionButton("模拟接口失败", "GET /api/order/detail?id=10086", destructive = true) {
                    ctx.mockFailedRequest()
                }
                MockActionButton("模拟轮询请求", "GET /api/message/unread · repeated x4") { ctx.mockPolling() }
                SectionTitle("当前状态")
                StatusCard(ctx.lastAction, ctx.lastStatus)
                DemoText("打开右下角 Console：可搜索 order、筛选 Error、查看重复折叠，点击日志即可复制。")
            }
            DebugConsole {
                attr { store(ctx.logs) }
                event { copyRequested { DemoPlatform.copyToClipboard(it.displayText) } }
            }
        }
    }

    private fun mockUserRequest() {
        lastAction = "GET /api/user/profile"
        lastStatus = "200 OK · 用户信息已加载"
        logs.append("API", "GET /api/user/profile")
        logs.append("PARAM", "userId=10001")
        logs.append("RESPONSE", "{\"code\":0,\"name\":\"Kuikly User\",\"level\":8}")
    }

    private fun mockProductRequest() {
        lastAction = "GET /api/product/list?page=1"
        lastStatus = "200 OK · 返回 20 件商品"
        logs.append("API", "GET /api/product/list")
        logs.append("PARAM", "page=1&pageSize=20")
        logs.append("CACHE", "Local cache missed; using mock response", DebugLogLevel.WARN)
        logs.append("RESPONSE", "code=0, items=20")
    }

    private fun mockCreateOrder() {
        lastAction = "POST /api/order/create"
        lastStatus = "200 OK · KU202609290001"
        logs.append("API", "POST /api/order/create")
        logs.append("PARAM", "productId=1001, count=2")
        logs.append("STATE", "Creating order...")
        logs.append("RESPONSE", "orderId=KU202609290001")
    }

    private fun mockFailedRequest() {
        lastAction = "GET /api/order/detail?id=10086"
        lastStatus = "500 Internal Server Error"
        logs.append("API", "GET /api/order/detail")
        logs.append("PARAM", "orderId=10086")
        logs.append("NETWORK", "HTTP 500 Internal Server Error", DebugLogLevel.ERROR)
        logs.append("RESPONSE", "{\"code\":50001,\"message\":\"Service unavailable\"}", DebugLogLevel.ERROR)
    }

    private fun mockPolling() {
        lastAction = "GET /api/message/unread"
        lastStatus = "200 OK · 相同日志已折叠为 x4"
        repeat(4) { logs.append("POLLING", "GET /api/message/unread") }
    }
}

private fun ViewContainer<*, *>.SectionTitle(title: String) {
    Text {
        attr {
            height(28f)
            marginLeft(16f)
            marginRight(16f)
            marginTop(4f)
            text(title)
            fontSize(13f)
            fontWeightBold()
            color(Color(0xFF64748B))
        }
    }
}

private fun ViewContainer<*, *>.MockActionButton(
    title: String,
    request: String,
    destructive: Boolean = false,
    action: () -> Unit,
) {
    View {
        attr {
            height(56f)
            marginLeft(16f)
            marginRight(16f)
            marginBottom(8f)
            paddingLeft(14f)
            paddingRight(14f)
            borderRadius(10f)
            backgroundColor(if (destructive) Color(0xFFFEF2F2) else Color(0xFFF1F5F9))
            flexDirectionColumn()
            justifyContentCenter()
        }
        Text {
            attr {
                text(title)
                fontSize(15f)
                fontWeightBold()
                color(if (destructive) Color(0xFFB91C1C) else Color(0xFF1E3A8A))
            }
        }
        Text {
            attr {
                marginTop(3f)
                text(request)
                fontSize(11f)
                color(Color(0xFF64748B))
            }
        }
        event { click { action() } }
    }
}

private fun ViewContainer<*, *>.StatusCard(lastAction: String, lastStatus: String) {
    View {
        attr {
            height(90f)
            marginLeft(16f)
            marginRight(16f)
            marginBottom(4f)
            padding(12f)
            borderRadius(12f)
            backgroundColor(Color(0xFFDBEAFE))
            flexDirectionColumn()
        }
        Text { attr { text("Last Action"); fontSize(11f); color(Color(0xFF64748B)) } }
        Text { attr { marginTop(3f); text(lastAction); fontSize(14f); fontWeightBold(); color(Color(0xFF1E3A8A)) } }
        Text { attr { marginTop(8f); text("Result  $lastStatus"); fontSize(12f); color(Color(0xFF334155)) } }
    }
}
