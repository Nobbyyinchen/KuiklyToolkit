# 独立 Android Demo / Android demo

[Quick start](QUICK_START.md) · [Validation status](COMPATIBILITY.md)

The demo is part of this repository. It uses public Kuikly APIs, its own menu, six sample pages and three local PNG fixtures. No consuming application's project or assets are needed.

## Build and run

```shell
./gradlew :android-demo:assembleDebug
adb install -r android-demo/build/outputs/apk/debug/android-demo-debug.apk
```

Use gradlew.bat on Windows, or run android-demo from Android Studio.
The APK is also uploaded by the standalone GitHub workflow when its build succeeds.

[Download v0.1.0 preview APK](https://github.com/Nobbyyinchen/KuiklyToolkit/releases/tag/v0.1.0) · [Watch the recorded pager](media/adaptive-height-pager.mp4)

## Menu / 操作

| Page | What to try |
| --- | --- |
| AdaptiveHeightPager | Swipe between 2-row, 4-row and 3-row category menus; watch the featured section move continuously with the pager height |
| StableImage | Select blue, missing and green asset sources; observe the delayed load and fallback |
| LazyMountContainer | Scroll through eight cards and return; inspect the mounted counter |
| PaginatedWaterfall | Scroll to page 2, retry its simulated first failure, then refresh |
| DebugConsole | Trigger simulated API requests, open the console, then inspect methods, parameters, responses, errors, keyword search, repeated-log folding, filtering and copying |
| KMP JSON serialization | Decode empty-string and valid fields; reject an invalid non-empty list type |

All text and test data are generic. API operations in DebugConsole are local simulations: business code explicitly appends every log to `DebugLogStore`; the console does not hook requests or methods. The image loader supports the demo's local toolkit assets only; it is not a production network loader.

To open one page directly:

```shell
adb shell am start -n io.github.nobbyyinchen.kuikly.toolkit.demo/.MainActivity --es toolkit_page toolkit_pager
```

Other page names: toolkit_image, toolkit_lazy, toolkit_waterfall, toolkit_console, toolkit_serialization.

## CI evidence

The standalone workflow assembles the APK and runs scripts/android-smoke.sh on an Android API 29 emulator. Its evidence artifact contains page-load logs, screenshots and a real pager recording. Read the actual workflow result before treating a run as passed.

## Public API references

The host follows the official [Android integration guide](https://github.com/Tencent-TDS/KuiklyUI/blob/main/docs/QuickStart/android.md), including KuiklyBaseView attach/pause/resume/detach and public render adapters. Page registration uses the official core-ksp processor.

Validated source: f7e53971555af9d76ec8e6039fd2a4bc53589a7f. [Successful run](https://github.com/Nobbyyinchen/KuiklyToolkit/actions/runs/36396310590).
