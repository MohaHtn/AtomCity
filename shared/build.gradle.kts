plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

compose.resources {
    publicResClass = true
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    android {
        namespace = "org.arcade.atomcity.shared"
        compileSdk = 36
        minSdk = 24
        
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
        
        androidResources {
            enable = true
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
            binaryOptions["bundleId"] = "org.arcade.atomcity.shared"
        }
    }

    sourceSets {
        val composeDeps = (project.extensions.getByName("compose") as org.jetbrains.compose.ComposeExtension).dependencies
        commonMain.dependencies {
            api(libs.kotlinx.serialization.json)
            api(libs.ktor.client.core)
            api(libs.ktor.client.content.negotiation)
            api(libs.ktor.client.logging)
            api(libs.ktor.serialization.kotlinx.json)
            api(libs.koin.core)
            implementation("io.insert-koin:koin-compose:4.0.0")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.8.0")
            api(libs.androidx.datastore.preferences.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.room.runtime)
            implementation(libs.sqlite.bundled)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.compose.navigation)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.coil.gif)
            
            implementation(composeDeps.runtime)
            implementation(composeDeps.foundation)
            implementation(composeDeps.material3)
            implementation(composeDeps.ui)
            api(composeDeps.components.resources)
            implementation(composeDeps.components.uiToolingPreview)
        }
        androidMain.dependencies {
            api(libs.ktor.client.okhttp)
            implementation(libs.androidx.work.runtime.ktx)
            implementation(libs.androidx.activity.compose)
            implementation(composeDeps.uiTooling)
            implementation(project.dependencies.platform(libs.androidx.compose.bom))
            implementation(libs.androidx.ui.tooling)
            implementation(libs.androidx.ui.tooling.preview)
            implementation(libs.coil.gif)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.coil.gif)
        }
    }
}

val copyComposeResourcesToAndroidAssets by tasks.registering(Copy::class) {
    group = "compose resources"
    from(project.file("src/commonMain/composeResources"))
    into(project.file("src/androidMain/assets/composeResources/atomcity.shared.generated.resources"))
    includeEmptyDirs = false
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

afterEvaluate {
    tasks.findByName("preBuild")?.dependsOn("copyComposeResourcesToAndroidAssets")
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}
