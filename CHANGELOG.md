# Changelog

## Unreleased

- Removed local JAR/KLIB dependency injection from all build variants.
- Use Maven Central serialization for standard targets and a separate public KBA version for HarmonyOS.
- Replaced the wrapper with the official Gradle 8.11.1 runner and verified checksums.
- Added a standalone CI build with an empty dependency cache and an import/build boundary check.
- Made sample image sources explicit parameters instead of assumed host assets.

## 1.0.0

- Added `AdaptiveHeightPager` with continuous height interpolation.
- Added stable two-layer `StableImage`.
- Added one-shot `LazyMountContainer`.
- Added injectable `DebugLogStore` and `DebugConsole`.
- Added business-neutral `PaginatedWaterfall` and a stale-response-safe pagination controller.
- Added lenient kotlinx.serialization adapters and compatibility aliases.
- Added common tests and source-only examples.
