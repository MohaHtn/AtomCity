# Plan pour le build iOS

Ce plan détaille les modifications nécessaires pour permettre au projet de compiler et de s'exécuter sur iOS. Cela inclut l'implémentation des fonctions `expect` manquantes, la configuration de Room pour iOS, et l'initialisation de Koin dans l'application Swift.

## User Review Required

> [!IMPORTANT]
> L'initialisation de Koin sur iOS nécessite une clé API pour `Scorefetcher`. Pour l'instant, une chaîne vide sera utilisée comme placeholder dans `iosAppApp.swift`. Vous devrez la remplacer par votre clé réelle ou configurer un mécanisme pour la passer (ex: via un fichier de configuration).

> [!WARNING]
> Deux bases de données Room sont actuellement définies dans le projet (`org.arcade.atomcity.db.AppDatabase` et `com.atomcity.maimai.db.AppDatabase`). Des implémentations iOS seront fournies pour les deux, mais il serait préférable d'unifier ces bases de données à l'avenir.

## Proposed Changes

### [Shared Module]

#### [MODIFY] [PlatformUtils.ios.kt](file:///Users/mohahtn/StudioProjects/AtomCity/shared/src/iosMain/kotlin/org/arcade/atomcity/utils/PlatformUtils.ios.kt)
- Correction du cast `String` vers `NSString` pour éviter l'avertissement de compilation.
- Nettoyage des imports inutilisés.

#### [NEW] [AppDatabase.ios.kt](file:///Users/mohahtn/StudioProjects/AtomCity/shared/src/iosMain/kotlin/org/arcade/atomcity/db/AppDatabase.ios.kt)
- Implémentation de `getDatabaseBuilder()` pour la base de données principale.
- Utilisation du répertoire `Documents` de l'application pour stocker le fichier `.db`.

#### [NEW] [AppDatabase.ios.kt](file:///Users/mohahtn/StudioProjects/AtomCity/shared/src/com/atomcity/maimai/db/AppDatabase.ios.kt)
- Implémentation de `getDatabaseBuilder()` pour la base de données Maimai.

#### [NEW] [IosNetworkErrorHandler.kt](file:///Users/mohahtn/StudioProjects/AtomCity/shared/src/iosMain/kotlin/org/arcade/atomcity/network/IosNetworkErrorHandler.kt)
- Implémentation de `NetworkErrorHandler` pour iOS qui logue les erreurs via `PlatformUtils`.

### [iOS Application]

#### [MODIFY] [iosAppApp.swift](file:///Users/mohahtn/StudioProjects/AtomCity/iosApp/iosApp/iosAppApp.swift)
- Initialisation de Koin au démarrage de l'application en appelant `KoinIOSKt.doInitKoin`.
- Fourniture de l'implémentation de `NetworkErrorHandler`.

## Verification Plan

### Automated Tests
- Tentative de compilation du module `shared` pour la cible iOS Simulator :
  `./gradlew :shared:assembleIosSimulatorArm64`

### Manual Verification
- L'utilisateur devra ouvrir le projet dans Xcode et tenter de lancer l'application sur un simulateur.
- Vérifier que Koin s'initialise sans crash au démarrage.
