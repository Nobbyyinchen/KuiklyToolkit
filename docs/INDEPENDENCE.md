# Standalone project boundaries

KuiklyToolkit is an independent Git repository and KMP build. A consuming application supplies data and optional platform services through public callbacks; its source tree, Gradle project, cached binaries and private resources are never build inputs.

## Dependency map

| Module | Required dependencies | Optional host inputs |
| --- | --- | --- |
| kuikly-toolkit | Kotlin standard library and the public Kuikly core artifact | Item/content builders, pagination results, image URIs, log entries and clipboard callback |
| kuikly-lenient-serialization | Kotlin standard library and public kotlinx.serialization JSON | Caller models and KSerializer instances |
| sample | The two modules in this repository | Image/placeholder URIs and clipboard callback |

The UI module does not depend on the serialization module. The serialization module has no Kuikly UI dependency. Sample Gradle `project()` references stay inside this repository.

## Public build sources

- Gradle 8.11.1: the official distribution and wrapper, with verified SHA256 values.
- Kotlin 2.1.21 and kotlinx.serialization 1.7.3: Maven Central and Gradle Plugin Portal.
- Android Gradle Plugin 8.2.2: Google Maven.
- Kuikly core 2.23.2-2.1.21: Tencent's public Maven repository, restricted to the Kuikly artifact group in the standard build.
- HarmonyOS: a separate settings/build variant uses the public Kotlin KBA toolchain and `OHOS_SERIALIZATION_VERSION=1.7.1-KBA-003` runtime. These are ecosystem requirements rather than application artifacts.

There is no local binary fallback, local Maven repository, included application build or absolute dependency path. Android SDK and JDK are normal developer prerequisites, configured through the standard environment or an untracked `local.properties` file.

## Runtime boundaries

All component imports belong to Kotlin, public Kuikly core or this repository's own packages. Serializers import only Kotlin and kotlinx.serialization.

- AdaptiveHeightPager receives item data and height/content providers.
- StableImage receives network and fallback image URIs; it has no preset application resource path.
- LazyMountContainer receives placeholder and content builders.
- PaginatedWaterfall emits requests and accepts results; it never selects an endpoint, parses business payloads or performs navigation.
- DebugConsole/DebugLogStore accept logs, page/time providers and copy callbacks. They never import a private logger or platform bridge.
- Serializers operate on generic Kotlin types and KSerializer instances, without application model types.

All UI calls must follow the Kuikly runtime's normal thread/lifecycle requirements. Data providers and clipboard callbacks are integration points available to any application.

## Reproducible validation

The `Standalone build` GitHub workflow checks out only this repository, uses a fresh runner and an empty Gradle dependency cache, downloads public dependencies and runs the commands documented in README. It does not use a caller's checkout, Gradle init script, local JAR/KLIB, copied Node packages or skipped dependency installation.

`scripts/check_independence.py` rejects imports outside the public/own package allowlist, local binary repositories, absolute filesystem paths, private Maven endpoints and project dependencies outside the three repository modules. This check runs before compilation in CI.

JS tests validate 16 algorithm/serialization cases. Android/JS libraries and examples are compiled independently. iOS requires a macOS/Xcode build; HarmonyOS KBA builds and all platform UI interactions require separate validation. A successful compile does not stand in for device verification.
