/*
 * MIT License
 * Copyright (c) 2026 KuiklyToolkit contributors
 */
package io.github.nobbyyinchen.kuikly.toolkit.demo

import android.app.Application
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import com.tencent.kuikly.core.render.android.adapter.HRImageLoadOption
import com.tencent.kuikly.core.render.android.adapter.IKRImageAdapter
import com.tencent.kuikly.core.render.android.adapter.IKRThreadAdapter
import com.tencent.kuikly.core.render.android.adapter.KuiklyRenderAdapterManager
import java.util.concurrent.Executors

class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val workers = Executors.newFixedThreadPool(2)
        val main = Handler(Looper.getMainLooper())
        KuiklyRenderAdapterManager.krThreadAdapter = object : IKRThreadAdapter {
            override fun executeOnSubThread(task: () -> Unit) { workers.execute { task() } }
        }
        KuiklyRenderAdapterManager.krImageAdapter = object : IKRImageAdapter {
            override fun fetchDrawable(imageLoadOption: HRImageLoadOption, callback: (Drawable?) -> Unit) {
                workers.execute {
                    val drawable = runCatching {
                        require(imageLoadOption.src.startsWith("assets://toolkit/"))
                        assets.open(imageLoadOption.src.removePrefix("assets://")).use { stream ->
                            BitmapFactory.decodeStream(stream)?.let { BitmapDrawable(resources, it) }
                        }
                    }.getOrNull()
                    // A small, controlled delay makes the fallback transition visible.
                    main.postDelayed({ callback(drawable) }, 450L)
                }
            }
        }
    }
}
