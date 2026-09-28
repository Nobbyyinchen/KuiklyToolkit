# KuiklyDebugConsole

在 Kuikly 页面内查看、折叠和筛选日志。DebugLogStore 提供容量限制、重复日志折叠与查询；DebugConsole 提供面板、清空和复制事件。

[运行独立 Android 示例](https://github.com/Nobbyyinchen/KuiklyToolkit/tree/main/android-demo) · [接入代码与边界](https://github.com/Nobbyyinchen/KuiklyToolkit/blob/main/docs/COMPONENTS.md#debugconsole) · [平台验证](https://github.com/Nobbyyinchen/KuiklyToolkit/blob/main/docs/COMPATIBILITY.md)

日志源、页面名、时间文本与剪贴板服务由调用方提供。只在你需要的页面安装面板，并遵循 Kuikly 的线程与生命周期要求。Demo 的剪贴板实现属于独立 Android 宿主。

当前随 kuikly-toolkit 模块通过源码接入，Maven Central 尚未发布。
