/*
 * MIT License
 * Copyright (c) 2026 KuiklyToolkit contributors
 */
package io.github.nobbyyinchen.kuikly.toolkit.demo

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.tencent.kuikly.core.render.android.context.KuiklyRenderCoreExecuteModeBase
import com.tencent.kuikly.core.render.android.exception.ErrorReason
import com.tencent.kuikly.core.render.android.expand.KuiklyBaseView
import com.tencent.kuikly.core.render.android.expand.KuiklyRenderViewBaseDelegatorDelegate
import io.github.nobbyyinchen.kuikly.toolkit.sample.DemoPlatform

class MainActivity : Activity() {
    private var pageView: KuiklyBaseView? = null
    private val pages = linkedMapOf(
        "toolkit_pager" to "AdaptiveHeightPager",
        "toolkit_image" to "StableImage",
        "toolkit_lazy" to "LazyMountContainer",
        "toolkit_waterfall" to "PaginatedWaterfall",
        "toolkit_console" to "DebugConsole",
        "toolkit_serialization" to "KMP JSON serialization",
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DemoPlatform.copyToClipboard = { text ->
            Handler(Looper.getMainLooper()).post {
                (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager)
                    .setPrimaryClip(ClipData.newPlainText("KuiklyToolkit log", text))
            }
        }
        val selected = intent.getStringExtra("toolkit_page")
        if (selected != null && pages.containsKey(selected)) openPage(selected) else showMenu()
    }

    private fun showMenu() {
        pageView?.onDetach()
        pageView = null
        val menu = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(16))
            setBackgroundColor(Color.WHITE)
        }
        menu.addView(TextView(this).apply {
            text = "KuiklyToolkit\nSix independent examples"
            textSize = 24f
            setTextColor(Color.rgb(23, 37, 84))
            setPadding(0, 0, 0, dp(20))
        })
        pages.forEach { (page, label) ->
            menu.addView(Button(this).apply {
                text = label
                isAllCaps = false
                setOnClickListener { openPage(page) }
            })
        }
        setContentView(menu)
    }

    private fun openPage(page: String) {
        pageView?.onDetach()
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(Button(this).apply {
            text = "‹  KuiklyToolkit / ${pages[page]}"
            isAllCaps = false
            setOnClickListener { showMenu() }
        })
        val delegate = object : KuiklyRenderViewBaseDelegatorDelegate {
            override fun onPageLoadComplete(isSucceed: Boolean, errorReason: ErrorReason?,
                executeMode: KuiklyRenderCoreExecuteModeBase) {
                Log.i("ToolkitDemo", "loaded=$isSucceed page=$page")
            }
            override fun onUnhandledException(throwable: Throwable, errorReason: ErrorReason,
                executeMode: KuiklyRenderCoreExecuteModeBase) {
                Log.e("ToolkitDemo", "render error page=$page", throwable)
            }
        }
        val view = KuiklyBaseView(this, delegate)
        pageView = view
        root.addView(view, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root)
        view.onAttach("", page, emptyMap())
    }

    override fun onResume() { super.onResume(); pageView?.onResume() }
    override fun onPause() { pageView?.onPause(); super.onPause() }
    override fun onDestroy() {
        pageView?.onDetach()
        DemoPlatform.copyToClipboard = {}
        super.onDestroy()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
