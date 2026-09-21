/*
 * MIT License
 * Copyright (c) 2026 KuiklyToolkit contributors
 */
package io.github.nobbyyinchen.kuikly.toolkit.lazy

import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.event.willAppear
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.View

class LazyMountContainerAttr : ComposeAttr() {
    var lazy: Boolean by observable(true)
    var placeholderHeight: Float by observable(0f)
    var preloadOffset: Float by observable(240f)

    internal var contentBuilder: ViewContainer<*, *>.() -> Unit = {}
    internal var placeholderBuilder: (ViewContainer<*, *>.() -> Unit)? = null

    fun content(builder: ViewContainer<*, *>.() -> Unit) {
        contentBuilder = builder
    }

    fun placeholder(builder: ViewContainer<*, *>.() -> Unit) {
        placeholderBuilder = builder
    }
}

class LazyMountContainerEvent : ComposeEvent() {
    internal var mountedHandler: (() -> Unit)? = null

    fun mounted(handler: () -> Unit) {
        mountedHandler = handler
    }
}

/** Mounts expensive content once it approaches the nearest scrolling viewport. */
class LazyMountContainerView : ComposeView<LazyMountContainerAttr, LazyMountContainerEvent>() {
    private var hasMounted by observable(false)

    override fun createAttr() = LazyMountContainerAttr()

    override fun createEvent() = LazyMountContainerEvent()

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            val lazyEnabled = ctx.attr.lazy && ctx.attr.placeholderHeight > 0f
            View {
                attr {
                    if (lazyEnabled && !ctx.hasMounted) {
                        height(ctx.attr.placeholderHeight)
                    }
                }
                event {
                    willAppear { ctx.mountOnce() }
                }

                vif({ lazyEnabled && !ctx.hasMounted && ctx.attr.preloadOffset > 0f }) {
                    View {
                        attr {
                            positionAbsolute()
                            top(-ctx.attr.preloadOffset)
                            left(0f)
                            size(1f, 1f)
                        }
                        event {
                            willAppear { ctx.mountOnce() }
                        }
                    }
                }

                vif({ lazyEnabled && !ctx.hasMounted }) {
                    val placeholder = ctx.attr.placeholderBuilder
                    if (placeholder != null) {
                        placeholder(this)
                    }
                }

                vif({ !lazyEnabled || ctx.hasMounted }) {
                    ctx.attr.contentBuilder(this)
                }
            }
        }
    }

    fun mountNow() {
        mountOnce()
    }

    private fun mountOnce() {
        if (hasMounted) return
        hasMounted = true
        event.mountedHandler?.invoke()
    }
}

fun ViewContainer<*, *>.LazyMountContainer(init: LazyMountContainerView.() -> Unit) {
    addChild(LazyMountContainerView(), init)
}

