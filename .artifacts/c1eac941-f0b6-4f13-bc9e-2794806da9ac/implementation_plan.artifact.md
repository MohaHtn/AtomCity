# Fix Koin NoDefinitionFoundException for ScorefetcherRepository

The app is crashing because it's trying to inject the concrete class `ScorefetcherRepository` instead of its interface `IScorefetcherRepository`. The Koin module provides the interface, but some parts of the code (like `MainActivity` and `ApiItem`) depend on the implementation.

## Proposed Changes

### [shared] Component

#### [MODIFY] [IScorefetcherRepository.kt](file:///C:/Users/MohaHtn/StudioProjects/AtomCity/shared/src/commonMain/kotlin/org/arcade/atomcity/domain/repository/IScorefetcherRepository.kt)
- Add `removeApiKey(apiKey: String): Flow<DeleteApiKeyResponse>` to the interface so it can be used via the interface.

#### [MODIFY] [ScorefetcherRepository.kt](file:///C:/Users/MohaHtn/StudioProjects/AtomCity/shared/src/commonMain/kotlin/org/arcade/atomcity/data/repository/ScorefetcherRepository.kt)
- Mark `removeApiKey` as `override`.

#### [MODIFY] [ApiItem.kt](file:///C:/Users/MohaHtn/StudioProjects/AtomCity/shared/src/commonMain/kotlin/org/arcade/atomcity/ui/guide/apistatus/ApiItem.kt)
- Change injection from `ScorefetcherRepository` to `IScorefetcherRepository`.

### [app] Component

#### [MODIFY] [MainActivity.kt](file:///C:/Users/MohaHtn/StudioProjects/AtomCity/app/src/main/java/org/arcade/atomcity/MainActivity.kt)
- Change Koin `get()` call to use `IScorefetcherRepository`.

## Verification Plan

### Automated Tests
- Build the project to ensure no compilation errors.
- Run the app and verify it no longer crashes on startup.

### Manual Verification
- Check if "Maimai Import State" is preloaded correctly on startup (this was where it crashed).
- Verify that deleting an API key in the settings still works.
