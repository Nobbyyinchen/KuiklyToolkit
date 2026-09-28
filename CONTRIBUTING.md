# Contributing

[Quick start](docs/QUICK_START.md) · [Validation matrix](docs/COMPATIBILITY.md)

1. Keep business models, private assets, internal endpoints, routing and analytics outside the libraries.
2. Public UI APIs stay in commonMain and use the traditional Kuikly DSL. Android-only services belong to android-demo.
3. Add meaningful common tests for new state or boundary behavior.
4. Run project-boundary/documentation checks, JS tests and Android compilation before proposing changes.
5. UI changes include runtime evidence for the platforms they claim. Record unverified targets separately; screenshots and emulator smoke checks do not establish every device interaction.
6. Keep API behavior, compatibility and release status consistent across documentation and CHANGELOG.

```shell
python3 scripts/check_independence.py
python3 scripts/check_docs.py
./gradlew :kuikly-toolkit:jsNodeTest :kuikly-lenient-serialization:jsNodeTest :android-demo:assembleDebug
```

Describe the concrete use case, expected behavior, affected platforms and validation performed. Small reproducible examples help maintainers resolve integration issues.
