/*
 * MIT License
 * Copyright (c) 2026 KuiklyToolkit contributors
 */
package io.github.nobbyyinchen.kuikly.toolkit.image

import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Image
import com.tencent.kuikly.core.views.ImageAttr
import com.tencent.kuikly.core.views.LoadFailureParams
import com.tencent.kuikly.core.views.LoadResolutionParams
import com.tencent.kuikly.core.views.View

class StableImageAttr internal constructor(private val owner: StableImageView) : ComposeAttr() {
    internal var imageSrc: String by observable("")
    internal var fallbackImageSrc: String by observable("")
    internal var hideFallbackAfterLoad: Boolean by observable(false)
    internal var imageAttr: ImageAttr.() -> Unit = {}

    fun src(value: String) {
        if (imageSrc == value) return
        imageSrc = value
        owner.onSourceChanged()
    }

    fun fallbackSrc(value: String) {
        fallbackImageSrc = value
    }

    /** Enable for transparent network images that must not reveal the fallback underneath. */
    fun hideFallbackOnSuccess(value: Boolean) {
        hideFallbackAfterLoad = value
    }

    /** Applies additional attributes such as resizeCover, tintColor or blurRadius. */
    fun image(block: ImageAttr.() -> Unit) {
        imageAttr = block
    }
}

class StableImageEvent : ComposeEvent() {
    internal var loadResolutionHandler: ((LoadResolutionParams) -> Unit)? = null
    internal var loadFailureHandler: ((LoadFailureParams) -> Unit)? = null

    fun loadResolution(handler: (LoadResolutionParams) -> Unit) {
        loadResolutionHandler = handler
    }

    fun loadFailure(handler: (LoadFailureParams) -> Unit) {
        loadFailureHandler = handler
    }
}

/**
 * Keeps a fallback Image and a network Image mounted at all times. This stable two-node structure
 * avoids a blank frame during loading and is friendly to Kuikly TurboDisplay tree reuse.
 */
class StableImageView : ComposeView<StableImageAttr, StableImageEvent>() {
    private var imageLoaded by observable(false)

    override fun createAttr() = StableImageAttr(this)

    override fun createEvent() = StableImageEvent()

    internal fun onSourceChanged() {
        imageLoaded = false
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flex(1f)
                    alignSelfStretch()
                    overflow(false)
                }
                Image {
                    attr {
                        absolutePositionAllZero()
                        resizeStretch()
                        src(ctx.attr.fallbackImageSrc)
                        opacity(
                            if (ctx.attr.fallbackImageSrc.isEmpty() ||
                                (ctx.attr.hideFallbackAfterLoad && ctx.imageLoaded)
                            ) 0f else 1f
                        )
                    }
                }
                Image {
                    attr {
                        absolutePositionAllZero()
                        resizeStretch()
                        ctx.attr.imageAttr(this)
                        src(ctx.attr.imageSrc)
                    }
                    event {
                        loadResolution { params ->
                            ctx.imageLoaded = true
                            ctx.event.loadResolutionHandler?.invoke(params)
                        }
                        loadFailure { params ->
                            ctx.imageLoaded = false
                            ctx.event.loadFailureHandler?.invoke(params)
                        }
                    }
                }
            }
        }
    }
}

fun ViewContainer<*, *>.StableImage(init: StableImageView.() -> Unit) {
    addChild(StableImageView(), init)
}

