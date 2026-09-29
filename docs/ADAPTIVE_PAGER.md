# 异高页面切换：让容器高度跟随手势

[运行示例](DEMO.md) · [组件 API](COMPONENTS.md#adaptiveheightpager) · [源码](../kuikly-toolkit/src/commonMain/kotlin/io/github/nobbyyinchen/kuikly/toolkit/pager/AdaptiveHeightPager.kt)

## 场景

首页的“推荐”“服务”“生活”分类分别包含 8、16、10 个菜单项，使用四列宫格后自然形成 2、4、3 行内容。若分页容器始终取最大高度，较短分类会留下空白；如果只在切页结束后更改高度，紧随菜单的业务内容会突然跳动。

AdaptiveHeightPager 使用调用方提供的高度，在横向滚动过程中连续调整视口高度。它不自动测量任意内容，也不添加循环页面。

## 插值

将横向偏移除以页宽，得到当前位置：

```text
position = offsetX / pageWidth
from = floor(position)
to = ceil(position)
progress = position - from
height = heights[from] + (heights[to] - heights[from]) * progress
```

Demo 每行菜单高 60，三页高度为 120、240、180。从“推荐”向“服务”拖动半页时，高度是 120 + (240 - 120) × 0.5 = 180。偏移被夹在首尾范围内，避免越界下标。空列表或非正页宽返回 0，负高度按 0 处理。

纯函数 interpolatePageHeight 的边界行为由 commonTest 覆盖，独立 CI 在 JS 上执行这些测试。

## 固定分页帧与自然内容高度

Kuikly PageList 控制直接子节点的分页帧，分页尺寸不能简单地作为每个内容的自然高度。组件用最大高度作为分页帧，将调用方的内容放进内层 View，再按每项自然高度布局。

外层视口随手势变化，并使用 overflow(true) 裁剪子节点。这里的 Boolean 参数叫 clipChild；false 会允许子节点越过视口。真实 Android 截图检查帮助发现并修正了这一点。

这样，分页滚动范围保持稳定，内容高度和跟随区域由变化的视口决定。调用方的 item creator 是通用 ViewContainer 构建器，可以直接使用 View、Text 或自己的组件。

## 验证与使用边界

独立 Android 示例提供三组不同行数的分类菜单，菜单下方紧跟“精选内容”区域。CI 打开实际 Kuikly 页面、执行滑动、记录截图和视频；模拟器记录验证具体场景，不能代替所有设备和平台验收。

接入时提供正的页宽与每项高度，使用有边界的分页数据。当前版本应在初始化时提供列表；运行中替换数据和其他兼容需求请以实际 API 与验证结果为准。
