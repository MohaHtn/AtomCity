# Architecture AtomCity

Cette application suit les principes de Clean Architecture avec une architecture MVVM (Model-View-ViewModel).

## Structure des packages

```
org.arcade.atomcity/
├── data/                  # Couche de données
│   ├── remote/           # Sources de données distantes (API)
│   ├── local/            # Sources de données locales (DataStore, Room)
│   ├── repository/       # Implémentations des repositories
│   └── model/            # Modèles de données pour la couche data
│
├── domain/               # Couche domaine
│   ├── model/           # Modèles de domaine
│   ├── repository/      # Interfaces des repositories
│   └── usecase/         # Cas d'utilisation
│
├── presentation/         # Couche présentation
│   ├── screens/         # Écrans de l'application
│   │   ├── home/       # Écran d'accueil
│   │   └── detail/     # Écran de détail
│   ├── components/      # Composants UI réutilisables
│   └── theme/           # Thème de l'application
│
├── di/                   # Injection de dépendances
│   ├── DataModule.kt
│   ├── DomainModule.kt
│   └── PresentationModule.kt
│
└── util/                # Utilitaires et extensions
```

## Principes architecturaux

1. **Séparation des préoccupations** : Chaque couche a une responsabilité unique et bien définie.

2. **Dépendances unidirectionnelles** : Les dépendances ne vont que dans une direction :
   - La couche présentation dépend du domaine
   - Le domaine ne dépend de rien
   - La couche data implémente les interfaces du domaine

3. **Modèles séparés** :
   - `data.model` : DTOs pour la communication avec les API
   - `domain.model` : Modèles métier
   - `presentation.model` : Modèles UI

4. **Injection de dépendances** : Utilisation de Koin pour la gestion des dépendances

5. **MVVM** : 
   - `ViewModel` : Gestion de l'état et de la logique de présentation
   - `View` : Composables Jetpack Compose
   - `Model` : Couches domain et data

## Flow de données

1. UI (Composables) → ViewModel
2. ViewModel → UseCase
3. UseCase → Repository
4. Repository → DataSource (Remote/Local)

## Tests

La structure facilite les tests à chaque niveau :
- Tests unitaires pour les UseCases et ViewModels
- Tests d'intégration pour les Repositories
- Tests UI pour les Composables 