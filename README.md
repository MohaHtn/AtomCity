# AtomCity Score Companion (nom absolument pas définitif)
## Introduction
Ce dépôt contient le code source de l'application AtomCity Score Companion,
qui permet pour l'instant de consulter les scores des joueurs sur les différentes
bornes de jeux de rythme présentes à l'Atom.

C'est une application Android native, en utlisant [Kotlin](https://kotlinlang.org/) 
et [Jetpack Compose](https://developer.android.com/compose).

C'est un projet qui n'est en réalité juste un prétexte pour comprendre Kotlin et Jetpack Compose,
mais j'essaie d'appliquer le plus possible de bonnes pratiques de développement, telles que 
l'architecture MVVM, l'injection de dépendances avec [Koin](https://insert-koin.io/),
des gestions d'états avec [StateFlow](https://developer.android.com/kotlin/flow/stateflow) et 
d'autres trucs, mais en aucun cas cette application n'a été conçue pour tenir des standards de qualité
de manière professionnelle, c'est juste un projet personnel.
En ce sens, les tests unitaires et d'intégration seront très limités, et le code peut etre
très variable en qualité. 

Néanmoins, si vous souhaitez contribuer, n'hésitez pas à ouvrir une issue ou une PR ! 

## Compiler l'application
Pour compiler l'application, vous aurez besoin d'[Android Studio](https://developer.android.com/studio)
et d'un SDK Android installé. 

Vous pouvez cloner le dépôt et ouvrir le projet dans Android Studio, puis
lancer la compilation directrement depuis l'IDE.

Gradle est utilisé pour la gestion des dépendances, et les versions des dépendances sont
définies dans le fichier `build.gradle.kts` à la racine du projet.

