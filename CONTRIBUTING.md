# Contributing

Thanks for contributing to Soltra.

## Prerequisites

Before opening a pull request, ensure you have:

- Android SDK installed
- Java 17 available on your machine
- a local `local.properties` file (not committed)

Create local config:

```powershell
Copy-Item local.properties.example local.properties
```

Then edit `local.properties` and set:

```properties
sdk.dir=/absolute/path/to/Android/Sdk
```

## Local Validation

Run these commands before submitting changes.

Windows PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

macOS/Linux:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

## Contribution Guidelines

- Keep changes focused and atomic.
- Do not commit generated/local artifacts (`build/`, `.gradle/`, `.idea/`, `local.properties`, etc.).
- Update docs when behavior or setup changes.
- Preserve existing project style and naming conventions.

## Pull Request Checklist

Before opening a PR, verify:

- app builds locally
- unit tests pass
- no secrets or machine-specific paths are introduced
- relevant documentation is updated
