# Component examples / 组件示例

[Quick start](QUICK_START.md) · [Android demo](DEMO.md) · [Validation](COMPATIBILITY.md)

## AdaptiveHeightPager

```kotlin
AdaptiveHeightPager {
    attr {
        pageWidth = 375f
        initPageItems(items, height = { it.height }) { item ->
            View {
                attr { size(375f, item.height) }
            }
        }
    }
    event {
        heightDidChange { height -> }
        pageIndexDidChanged { params -> }
    }
}
```

它是有边界分页器，不增加首尾循环节点。`pageWidth` 必须大于 `0`；负高度按 `0` 处理，越界滚动位置会夹在首尾页面之间。

## StableImage

```kotlin
StableImage {
    attr {
        size(120f, 80f)
        fallbackSrc(placeholderUri)
        src(imageUrl)
        hideFallbackOnSuccess(true)
        image { resizeCover() }
    }
    event {
        loadFailure { params -> }
    }
}
```

底层兜底图和上层网络图始终存在。网络地址改变时加载状态会重置。透明网络图需要开启 `hideFallbackOnSuccess`，否则透明区域会透出底图。`imageUrl` 与 `placeholderUri` 由调用方提供；源码示例通过 `ToolkitExamples(imageUrl, fallbackImageUri)` 接收这两个参数，没有预设宿主 assets 路径或网络地址。

## LazyMountContainer

```kotlin
LazyMountContainer {
    attr {
        lazy = true
        placeholderHeight = 240f
        preloadOffset = 300f
        placeholder { View { attr { height(240f) } } }
        content { ExpensiveCard { } }
    }
    event {
        mounted { }
    }
}
```

`placeholderHeight <= 0` 时立即渲染内容，避免零高度节点永远无法进入可见区域。首次挂载后不因滚出屏幕而销毁。

## DebugConsole

```kotlin
val logs = DebugLogStore(
    pageNameProvider = { currentPageName },
    timeTextProvider = { currentTimeText() },
)

logs.append("network", "request started", DebugLogLevel.INFO)

DebugConsole {
    attr { store(logs) }
    event {
        copyRequested { entry -> clipboard.setText(entry.displayText) }
    }
}
```

剪贴板、时间、页面名和持久化均由接入方提供，库不会调用平台 Bridge。组件应放在页面根容器的最后，以免被其他内容遮挡。

## PaginatedWaterfall

```kotlin
PaginatedWaterfall<Product> {
    val waterfall = this
    attr {
        width = 375f
        height = 600f
        columnCount = 2
        pageSize = 20
        preloadDistance = 200f
        items { item, index -> ProductCard(item) }
        state { state, hasItems, hasMore ->
            // 按需渲染 loading / error / empty / no-more UI。
        }
    }
    event {
        requestPage { request ->
            repository.load(request.page, request.pageSize, request.pageToken) { result ->
                result.onSuccess {
                    waterfall.submitPage(request, it.items, it.nextToken, it.hasMore)
                }.onFailure {
                    waterfall.failPage(request)
                }
            }
        }
    }
}
```

组件不发网络请求、不解析 JSON、不弹 Toast，也不持有路由和埋点。若瀑布流嵌套在外层不可滚动列表中，请设置 `scrollEnable = false`、`autoHeight = true`，并把外层滚动参数传给 `onOuterListScrolled`。每次请求携带 `generation`；旧刷新请求晚返回时会被拒绝，避免覆盖新数据。

## 容错序列化

```kotlin
@Serializable
data class Payload(
    @Serializable(with = EmptyStringAsZeroIntSerializer::class)
    val count: Int = 0,
    @Serializable(with = EmptyStringAsFalseSerializer::class)
    val enabled: Boolean = false,
)
```

容器泛型需要声明一个委托对象：

```kotlin
object ItemListSerializer : KSerializer<List<Item>> by
    EmptyStringAsListSerializer(Item.serializer())
```

数值适配器会把空字符串、`null` 及无法解析的 primitive 转成配置的默认值；Boolean 仅接受空值、`0/1` 和 `true/false`；List、Map、Object 对非空错误类型仍保持严格失败。
