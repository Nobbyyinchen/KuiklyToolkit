/*
 * MIT License
 * Copyright (c) 2026 KuiklyToolkit contributors
 */
package io.github.nobbyyinchen.kuikly.toolkit.sample

import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

/** Optional service supplied by the independent demo host, never by the libraries. */
object DemoPlatform {
    var copyToClipboard: (String) -> Unit = {}
}

internal fun ViewContainer<*, *>.DemoText(value: String, large: Boolean = false) {
    Text {
        attr {
            text(value)
            fontSize(if (large) 22f else 15f)
            color(Color(0xFF172554))
            margin(16f)
        }
    }
}

internal fun ViewContainer<*, *>.DemoButton(label: String, action: () -> Unit) {
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
