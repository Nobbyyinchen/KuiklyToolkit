/*
 * MIT License
 * Copyright (c) 2026 KuiklyToolkit contributors
 */
package io.github.nobbyyinchen.kuikly.toolkit.debug

import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.Input
import com.tencent.kuikly.core.views.Modal
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

class DebugConsoleAttr : ComposeAttr() {
    var enabled: Boolean by observable(true)
    var maxDisplayCount: Int by observable(300)
    var buttonRight: Float by observable(12f)
    var buttonBottom: Float by observable(96f)
    var panelHeightRatio: Float by observable(0.65f)

    internal var logStore: DebugLogStore = DebugLogStore()

    fun store(value: DebugLogStore) {
        logStore = value
    }
}

class DebugConsoleEvent : ComposeEvent() {
    internal var copyRequestedHandler: ((DebugLogEntry) -> Unit)? = null
    internal var expandedChangedHandler: ((Boolean) -> Unit)? = null

    /** The host decides how to access the platform clipboard. */
    fun copyRequested(handler: (DebugLogEntry) -> Unit) {
        copyRequestedHandler = handler
    }

    fun expandedChanged(handler: (Boolean) -> Unit) {
        expandedChangedHandler = handler
    }
}

/**
 * Lightweight in-app log viewer. It deliberately delegates clipboard, storage, time and current
 * page resolution to the host instead of depending on a business Pager or native bridge.
 */
class DebugConsoleView : ComposeView<DebugConsoleAttr, DebugConsoleEvent>() {
    private var expanded by observable(false)
    private var errorOnly by observable(false)
    private var searchKeyword by observable("")
    private var displayLogs by observableList<DebugLogEntry>()
    private var listenerId = 0

    override fun createAttr() = DebugConsoleAttr()

    override fun createEvent() = DebugConsoleEvent()

    override fun created() {
        super.created()
        listenerId = attr.logStore.addListener { refresh(it) }
    }

    override fun viewDestroyed() {
        if (listenerId != 0) {
            attr.logStore.removeListener(listenerId)
            listenerId = 0
        }
        super.viewDestroyed()
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            vif({ ctx.attr.enabled }) {
                Text {
                    attr {
                        positionAbsolute()
                        right(ctx.attr.buttonRight)
                        bottom(ctx.attr.buttonBottom)
                        width(76f)
                        height(34f)
                        borderRadius(17f)
                        backgroundColor(Color(0xDD1D2129))
                        color(Color.WHITE)
                        fontSize(13f)
                        text("Console")
                        textAlignCenter()
                    }
                    event {
                        click { ctx.updateExpandedState(true) }
                    }
                }

                vif({ ctx.expanded }) {
                    Modal {
                        View {
                            attr {
                                size(ctx.pagerData.pageViewWidth, ctx.pagerData.pageViewHeight)
                                backgroundColor(Color(0x55000000))
                            }
                            View {
                                attr {
                                    positionAbsolute()
                                    left(0f)
                                    right(0f)
                                    bottom(0f)
                                    height(
                                        ctx.pagerData.pageViewHeight *
                                            ctx.attr.panelHeightRatio.coerceIn(0.25f, 1f)
                                    )
                                    padding(12f)
                                    backgroundColor(Color(0xFF111827))
                                    flexDirectionColumn()
                                }

                                View {
                                    attr {
                                        height(36f)
                                        flexDirectionRow()
                                        alignItemsCenter()
                                    }
                                    Text {
                                        attr {
                                            flex(1f)
                                            text("Logs ${ctx.displayLogs.size}")
                                            color(Color.WHITE)
                                            fontSize(16f)
                                            fontWeightBold()
                                        }
                                    }
                                    Text {
                                        attr {
                                            marginRight(16f)
                                            text(if (ctx.errorOnly) "All" else "Errors")
                                            color(Color(0xFF93C5FD))
                                            fontSize(13f)
                                        }
                                        event {
                                            click {
                                                ctx.errorOnly = !ctx.errorOnly
                                                ctx.refresh(ctx.attr.logStore.snapshot())
                                            }
                                        }
                                    }
                                    Text {
                                        attr {
                                            marginRight(16f)
                                            text("Clear")
                                            color(Color(0xFFFCA5A5))
                                            fontSize(13f)
                                        }
                                        event { click { ctx.attr.logStore.clear() } }
                                    }
                                    Text {
                                        attr {
                                            text("Close")
                                            color(Color.WHITE)
                                            fontSize(13f)
                                        }
                                        event { click { ctx.updateExpandedState(false) } }
                                    }
                                }

                                View {
                                    attr {
                                        height(38f)
                                        marginBottom(8f)
                                        flexDirectionRow()
                                        alignItemsCenter()
                                    }
                                    Input {
                                        attr {
                                            flex(1f)
                                            height(34f)
                                            borderRadius(8f)
                                            backgroundColor(Color(0xFF1F2937))
                                            text(ctx.searchKeyword)
                                            placeholder("Search method, params, response…")
                                            placeholderColor(Color(0xFF6B7280))
                                            color(Color.WHITE)
                                            fontSize(12f)
                                            returnKeyTypeSearch()
                                        }
                                        event {
                                            textDidChange { params ->
                                                ctx.searchKeyword = params.text
                                                ctx.refresh(ctx.attr.logStore.snapshot())
                                            }
                                        }
                                    }
                                    Text {
                                        attr {
                                            marginLeft(12f)
                                            text("Reset")
                                            color(Color(0xFF93C5FD))
                                            fontSize(12f)
                                        }
                                        event {
                                            click {
                                                ctx.searchKeyword = ""
                                                ctx.refresh(ctx.attr.logStore.snapshot())
                                            }
                                        }
                                    }
                                }

                                Scroller {
                                    attr {
                                        flex(1f)
                                        showScrollerIndicator(true)
                                    }
                                    vfor({ ctx.displayLogs }) { entry ->
                                        Text {
                                            attr {
                                                height(40f)
                                                marginBottom(1f)
                                                backgroundColor(
                                                    when (entry.level) {
                                                        DebugLogLevel.ERROR -> Color(0x553B0A0A)
                                                        DebugLogLevel.WARN -> Color(0x55422D0A)
                                                        else -> Color(0x331F2937)
                                                    }
                                                )
                                                color(
                                                    when (entry.level) {
                                                        DebugLogLevel.ERROR -> Color(0xFFFCA5A5)
                                                        DebugLogLevel.WARN -> Color(0xFFFDE68A)
                                                        else -> Color(0xFFD1D5DB)
                                                    }
                                                )
                                                fontSize(11f)
                                                text(entry.displayText)
                                            }
                                            event {
                                                click { ctx.event.copyRequestedHandler?.invoke(entry) }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateExpandedState(value: Boolean) {
        if (expanded == value) return
        expanded = value
        event.expandedChangedHandler?.invoke(value)
    }

    private fun refresh(entries: List<DebugLogEntry>) {
        val filtered = entries
            .filter { !errorOnly || it.level == DebugLogLevel.ERROR }
            .filter { searchKeyword.isBlank() || searchKeyword.trim().lowercase() in it.searchText }
            .takeLast(attr.maxDisplayCount.coerceAtLeast(1))
        displayLogs.diffUpdate(filtered)
    }
}

fun ViewContainer<*, *>.DebugConsole(init: DebugConsoleView.() -> Unit) {
    addChild(DebugConsoleView(), init)
}
