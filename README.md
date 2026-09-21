# KuiklyToolkit

`KuiklyToolkit` 是面向 [KuiklyUI](https://github.com/Tencent-TDS/KuiklyUI) 传统 DSL 的跨端通用组件与 KMP 工具库。目前包含：

- `AdaptiveHeightPager`：页面高度不一致时，跟随横向滑动进度连续调整容器高度。
- `StableImage`：常驻兜底图和网络图两层节点，降低加载空白和 TurboDisplay 节点重建风险。
- `LazyMountContainer`：组件接近滚动容器可视区域时只挂载一次真实内容。
- `DebugConsole`：可注入日志源的端内调试面板，支持容量限制、重复折叠、筛选、清空和复制回调。
- `PaginatedWaterfall`：请求、解析和业务 UI 全部外置的双列/多列瀑布流，内置防重复加载、分页上限和过期响应丢弃。
- `KuiklyLenientSerialization`：兼容后端用空字符串表示 List、Map、Object、Boolean 和数值的异常 JSON。

这些能力抽取自企业级 App 的实际开发模式，源工程覆盖 Android、iOS 和 HarmonyOS。独立库仍需在每次发布前按本文验收矩阵重新完成三端构建与真机测试。

库不依赖业务包名、业务模型、路由、埋点、银行 Pager 基类、私有图片或 Native Bridge。源码全部位于 `commonMain`。

## 模块

| 模块 | 计划发布的 Maven 坐标 | 内容 |
| --- | --- | --- |
| `kuikly-toolkit` | `io.github.nobbyyinchen.kuikly:kuikly-toolkit:1.0.0` | UI 组件和 DebugLogStore |
| `kuikly-lenient-serialization` | `io.github.nobbyyinchen.kuikly:kuikly-lenient-serialization:1.0.0` | kotlinx.serialization 容错适配器 |
| `sample` | 不发布 | 可嵌入现有 Pager 的源码示例 |

当前通过 GitHub 源码接入，以上 Maven 坐标尚未发布到 Maven Central。将需要的模块复制到现有 KMP 工程并通过 `implementation(project(":kuikly-toolkit"))` 或 `implementation(project(":kuikly-lenient-serialization"))` 引用；也可以将需要的 `commonMain` 源码复制到已有模块。

## 组件分类与第三方目录登记

| 分类 | 组件 | 目录条目 | componentType |
| --- | --- | --- | --- |
| 分页与列表 | `AdaptiveHeightPager`、`PaginatedWaterfall` | KuiklyToolkit | KMP |
| 图片与渲染性能 | `StableImage`、`LazyMountContainer` | KuiklyToolkit | KMP |
| 调试工具 | `DebugConsole`、`DebugLogStore` | KuiklyDebugConsole | Tools |
| JSON 容错序列化 | `KuiklyLenientSerialization` | KuiklyLenientSerialization | KMP |

[登记条目](registry/KuiklyUI-Libraries-entries.json) 按 [KuiklyUI-third-party](https://github.com/Tencent-TDS/KuiklyUI-third-party) 模板提供字段、顺序和两空格缩进。三个条目分别链接到仓库、调试工具包和序列化模块，并提供对应源码示例。

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
        fallbackSrc("assets://common/image-placeholder.png")
        src(imageUrl)
        hideFallbackOnSuccess(true)
        image { resizeCover() }
    }
    event {
        loadFailure { params -> }
    }
}
```

底层兜底图和上层网络图始终存在。网络地址改变时加载状态会重置。透明网络图需要开启 `hideFallbackOnSuccess`，否则透明区域会透出底图。

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

## 构建与测试

在仓库根目录执行：

```shell
./gradlew :kuikly-toolkit:jsNodeTest \
  :kuikly-lenient-serialization:jsNodeTest \
  :kuikly-toolkit:compileReleaseKotlinAndroid \
  :kuikly-lenient-serialization:compileReleaseKotlinAndroid \
  :sample:compileKotlinJs \
  :sample:compileReleaseKotlinAndroid
```

HarmonyOS 需要 Kuikly 定制 Kotlin 工具链：

```shell
./gradlew -c settings.ohos.gradle.kts \
  :kuikly-toolkit:compileKotlinMetadata \
  :kuikly-lenient-serialization:compileKotlinMetadata
```

## 发布验收矩阵

目前已完成 Android 库与示例编译、JS 库与示例编译，以及 16 项 JS 单元测试；测试覆盖高度插值、日志存储、分页状态和序列化规则。测试不包含 Native 图片加载或组件真机交互。iOS 目标已配置，需在 macOS/Xcode 环境构建；HarmonyOS 构建配置已提供，本次验证因 KBA 编译器依赖下载受阻而未完成。目录平台字段表示源码适配目标，真机验收仍按以下矩阵执行。示例为可嵌入已有 Pager 的源码，图片 URL 和占位资源由接入方替换。

- Android：分页高度过渡、图片成功/失败/URL 切换、懒加载一次性、瀑布流翻页/失败重试/过期响应、Console 打开/清空/复制。
- iOS：以上场景，加测 TurboDisplay 首屏回放。
- HarmonyOS：以上场景，加测 TurboDisplay 首屏回放。
- 单元测试：高度边界、日志折叠/容量/筛选、每个序列化器的空值/合法值/错误值/编码行为。
- 示例视频：必须来自真实组件运行，推荐 H.264 + AAC 的 MP4，并通过 GitHub Pages 的 `<video controls playsinline>` 页面提供点击即播预览。

## License

[MIT](LICENSE)
