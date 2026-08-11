# Migration to Compose Multiplatform

This plan outlines the steps to migrate the iOS UI from SwiftUI to Compose Multiplatform, matching the Android implementation.

## Proposed Changes

### [Build System]

#### [MODIFY] [libs.versions.toml](file:///Users/mohahtn/StudioProjects/AtomCity/gradle/libs.versions.toml)
- Add Jetbrains Compose Multiplatform plugin and library versions.

#### [MODIFY] [build.gradle.kts (root)](file:///Users/mohahtn/StudioProjects/AtomCity/build.gradle.kts)
- Apply Jetbrains Compose plugin.

#### [MODIFY] [shared/build.gradle.kts](file:///Users/mohahtn/StudioProjects/AtomCity/shared/build.gradle.kts)
- Apply `jetbrains-compose` and `kotlin-compose` plugins.
- Add Compose dependencies to `commonMain`.

### [Shared Module UI]

#### [NEW] [shared/src/commonMain/kotlin/org/arcade/atomcity/ui](file:///Users/mohahtn/StudioProjects/AtomCity/shared/src/commonMain/kotlin/org/arcade/atomcity/ui)
- Move/Adapt UI components from Android `:app` module to `:shared` module.
- Implement the requested Search Bar and missing screens (Taiko, Users) in the shared module.

#### [NEW] [MainViewController.kt](file:///Users/mohahtn/StudioProjects/AtomCity/shared/src/iosMain/kotlin/org/arcade/atomcity/MainViewController.kt)
- Expose the shared UI to iOS via `ComposeUIViewController`.

### [Platform Adapters]

#### [MODIFY] [MainActivity.kt](file:///Users/mohahtn/StudioProjects/AtomCity/app/src/main/java/org/arcade/atomcity/MainActivity.kt)
- Update to use the shared UI.

#### [MODIFY] [iosApp/iosApp/iosAppApp.swift](file:///Users/mohahtn/StudioProjects/AtomCity/iosApp/iosApp/iosAppApp.swift)
- Update to host the Compose UI.

## Verification Plan

### Manual Verification
- Deploy to Android and verify the UI remains functional.
- Deploy to iOS and verify the Compose UI is rendered correctly.
- Verify the search bar and new screens (Taiko, Users) work on both platforms.
