# Compatibility and validation

[README](../README.md) · [Independent CI](https://github.com/Nobbyyinchen/KuiklyToolkit/actions/workflows/ci.yml)

## Toolchain

| Configuration | Kotlin | Kuikly core | Serialization |
| --- | --- | --- | --- |
| Standard Android / iOS / JS | 2.1.21 | 2.23.2-2.1.21 | 1.7.3 |
| Separate HarmonyOS KBA configuration | 2.0.21-KBA-010 | 2.23.2-2.0.21-ohos | 1.7.1-KBA-003 |

Gradle 8.11.1, JDK 17, Android Gradle Plugin 8.2.2, Android SDK platform 34.
The Android demo registers its pages with public Kuikly core-ksp and KSP 2.1.21-2.0.1.

## Targets versus evidence

| Target | Configured | Compilation / algorithm tests | UI runtime evidence |
| --- | --- | --- | --- |
| Android | Yes | Library/sample compilation; demo APK built by current CI | Six-page emulator smoke workflow; check the current run for success |
| JS | Yes | Libraries/sample compile; 16 JS tests | Browser and mini-program UI hosts not validated |
| iOS | Yes | Independent macOS/Xcode build pending | Device interaction and TurboDisplay replay pending |
| HarmonyOS | Separate KBA configuration | Independent KBA build pending | Device interaction and TurboDisplay replay pending |

The 16 JS tests cover height interpolation, bounded log storage, pagination state and serialization rules. They do not exercise native image loading or UI interactions.

The Android smoke workflow opens six actual Kuikly pages, requires successful page-load callbacks, captures screenshots and records pager gestures. It detects reported render exceptions; it does not exhaustively verify every interaction or establish physical-device acceptance.

The demo uses local image fixtures and a deliberately delayed asset loader. Network image failures, URL replacement races and transparent images require additional device tests.

Directory registration lists conservatively verified runtime targets. Unverified targets remain in this matrix and can be added to the catalog after acceptance. Dynamic mode is not advertised as verified by the Android JVM demo.

## Manual acceptance

- Pager: slow/fast drags, different heights, first/last boundaries and following content layout.
- Images: success/failure, source replacement, transparent content and fallback visibility.
- Lazy content: preloading threshold, mount count, repeated entry and exit.
- Waterfall: paging, retry, refreshing, delayed stale response and outer-list integration.
- Console: opening, filtering, folding, capacity, clearing and clipboard callback.
- Native platforms: device behavior and relevant TurboDisplay replay.

Record version, toolchain, device/system, steps and screenshots when reporting platform validation.
