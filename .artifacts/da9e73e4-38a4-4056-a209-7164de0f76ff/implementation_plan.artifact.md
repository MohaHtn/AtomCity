# Global Rename and UseCase Refactoring: Maitea to Scorefetcher

This plan covers two major tasks:
1.  Renaming all occurrences of "maitea" (and its variants) to "scorefetcher" throughout the project.
2.  Splitting the monolithic `GetScorefetcherDataUseCase` (formerly `GetMaiteaDataUseCase`) into specialized, thematic UseCases.

## Proposed Changes

### 1. Global Rename (Maitea -> Scorefetcher)

#### [MODIFY] [ScorefetcherRepository.kt](file:///C:/Users/MohaHtn/StudioProjects/AtomCity/shared/src/commonMain/kotlin/org/arcade/atomcity/data/ScorefetcherRepository.kt) (Renamed from MaiteaRepository.kt)
- Rename class and all internal references.
- Rename methods containing "MaiTea" (e.g., `startMaiTeaImport` -> `startScorefetcherImport`).

#### [MODIFY] [ScorefetcherViewModel.kt](file:///C:/Users/MohaHtn/StudioProjects/AtomCity/shared/src/commonMain/kotlin/org/arcade/atomcity/presentation/viewmodel/ScorefetcherViewModel.kt) (Renamed from MaiteaViewModel.kt)
- Rename class and all internal references.

#### [MODIFY] Models and Packages
- Rename packages `org.arcade.atomcity.model.maitea` to `org.arcade.atomcity.model.scorefetcher`.
- Rename classes like `MaiteaPlaysResponse`, `MaiteaPlayerDetailsResponse`, etc.

#### [MODIFY] UI and DI
- Update all Composable functions and DI modules that use the renamed classes/variables.

### 2. UseCase Refactoring

Instead of one `GetScorefetcherDataUseCase`, I will create:

- **`GetScorefetcherScoresUseCase`**: Handles fetching paginated scores, searching, and score-specific data.
- **`GetScorefetcherProfileUseCase`**: Handles player profile, ratings, and Best 30 charts.
- **`ScorefetcherImportUseCase`**: Handles the import process and worker status.
- **`GetScorefetcherAnalyticsUseCase`**: Handles chart history, "Best per player", and "Most played" analytics.
- **`GetScorefetcherJacketUseCase`**: Handles jacket image URL lookup.

## Verification Plan

### Automated Tests
- Run `gradle :shared:assembleDebug` and `gradle :app:assembleDebug` to ensure all references are correctly updated.

### Manual Verification
- Verify that the Maimai feature still works as expected with the new names.
