# Fix Duplicate Class Definition Error

The error "Type ... is defined multiple times" is caused by `MaiteaViewModel` and `TaikoViewModel` being defined in both the `:app` module and the `:shared` module with the same package name (`org.arcade.atomcity.presentation.viewmodel`).

Since this is a Kotlin Multiplatform project, these ViewModels should reside in the `:shared` module to be accessible by all platforms. The versions in the `:app` module are redundant and are causing the dex merging conflict during the release build.

## Proposed Changes

### [Presentation]

The following files will be removed from the `:app` module as they are already provided by the `:shared` module.

#### [DELETE] [MaiteaViewModel.kt](file:///C:/Users/MohaHtn/StudioProjects/AtomCity/app/src/main/java/org/arcade/atomcity/presentation/viewmodel/MaiteaViewModel.kt)
#### [DELETE] [TaikoViewModel.kt](file:///C:/Users/MohaHtn/StudioProjects/AtomCity/app/src/main/java/org/arcade/atomcity/presentation/viewmodel/TaikoViewModel.kt)

#### [MODIFY] [MaiteaViewModel.kt](file:///C:/Users/MohaHtn/StudioProjects/AtomCity/shared/src/commonMain/kotlin/org/arcade/atomcity/presentation/viewmodel/MaiteaViewModel.kt)
- Enable basic logging (using `println` or similar) to replace the commented-out `Log.e` calls, ensuring that error reporting is still available on Android.

### [UI / Infrastructure]

> [!IMPORTANT]
> The version of `TaikoViewModel` in the `:app` module contained Android-specific logic for merging avatar images using `Bitmap` and `Canvas`. This logic is currently commented out or missing in the `:shared` version.
> To restore this functionality, we should eventually implement it using `expect`/`actual` or move the platform-specific parts to `shared/src/androidMain`. For now, deleting the duplicate is necessary to fix the build.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:mergeDexRelease` to verify that the duplicate class error is resolved.
- Run `./gradlew :app:assembleDebug` to ensure the project still builds correctly.

### Manual Verification
- Deploy the app and verify that the Maimai and Taiko screens still load their data correctly using the ViewModels from the `:shared` module.
