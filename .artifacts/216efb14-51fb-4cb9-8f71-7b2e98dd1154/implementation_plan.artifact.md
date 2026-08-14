# Clean up Gradle Properties and resolve AGP warnings

This plan removes deprecated and experimental flags from `gradle.properties` that are triggering warnings in AGP 9.3.1. These flags are mostly legacy settings that are no longer recommended or have become the default.

## Proposed Changes

### Build Configuration

#### [MODIFY] [gradle.properties](file:///C:/Users/MohaHtn/StudioProjects/AtomCity/gradle.properties)
- Remove `android.dependency.excludeLibraryComponentsFromConstraints` (Deprecated)
- Remove `android.disallowKotlinSourceSets` (Experimental)
- Remove `android.defaults.buildfeatures.resvalues` (Deprecated)
- Remove `android.sdk.defaultTargetSdkToCompileSdkIfUnset` (Deprecated)
- Remove `android.enableAppCompileTimeRClass` (Deprecated)
- Remove `android.usesSdkInManifest.disallowed` (Deprecated)
- Remove `android.r8.optimizedResourceShrinking` (Deprecated)
- Remove `android.builtInKotlin` (Deprecated)
- Remove `android.newDsl` (Deprecated, and its removal enables the new Variant API defaults)
- Add `android.sync.suppressAgpWarnings=UNSUPPORTED_PROJECT_OPTION_USE,DEPRECATED_DSL` to suppress any remaining warnings from 3rd-party plugins.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to ensure the project still builds successfully without these flags.
- Check the Gradle output for remaining warnings.
