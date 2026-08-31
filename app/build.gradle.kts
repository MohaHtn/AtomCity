import com.android.build.api.dsl.ApplicationExtension
import com.github.triplet.gradle.androidpublisher.ResolutionStrategy
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.ksp)
    id("com.github.triplet.play") version "4.1.1"
}

compose.resources {
    publicResClass = true
}

val isGitHubActions = providers
    .environmentVariable("GITHUB_ACTIONS")
    .map { it.equals("true", ignoreCase = true) }
    .getOrElse(false)

extensions.configure<ApplicationExtension> {
    namespace = "org.arcade.atomcity"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.arcade.atomcity"
        minSdk = 24
        targetSdk = 36
        versionCode = 3
        versionName = "1.1"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        val keystoreFile = rootProject.file("apikeys.properties")
        val properties = Properties()

        if (keystoreFile.exists()) {
            keystoreFile.inputStream().use(properties::load)
        }

        val scorefetcherApiKey =
            properties.getProperty("SCOREFETCHER_API_KEY")
                ?: System.getenv("SCOREFETCHER_API_KEY")
                ?: ""

        buildConfigField(
            "String",
            "SCOREFETCHER_API_KEY",
            "\"$scorefetcherApiKey\""
        )

        versionNameSuffix = "pre-alpha-3"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            optimization {
                enable = true
            }

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        disable += "NullSafeMutableLiveData"
    }
}

play {
    enabled.set(!isGitHubActions)
    track.set("beta")
    resolutionStrategy.set(ResolutionStrategy.AUTO)

    if (!isGitHubActions) {
        serviceAccountCredentials.set(
            file("atomcity-autopublish.json")
        )
    }
}

dependencies {
    implementation(project(":shared"))
    
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.vico.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.palette)
    implementation(libs.insert.koin.koin.android)
    implementation(libs.androidx.datastore.preferences)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.coil)
    implementation(libs.coil.compose)
}
