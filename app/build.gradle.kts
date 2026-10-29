plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.jahanmusic.app"
    compileSdk = 37
    buildToolsVersion = "37.0.0"

    defaultConfig {
        applicationId = "com.jahanmusic.app"
        minSdk = 31
        targetSdk = 35
        versionCode = 12
        versionName = "1.1.0"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            freeCompilerArgs.add("-opt-in=androidx.compose.ui.ExperimentalComposeUiApi")
        }
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")

    implementation(platform("androidx.compose:compose-bom:2026.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")

    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")

    // Responsive (phones + tablets, Android 12+)
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Media3 (ExoPlayer) — real device playback + MediaSession
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-session:1.4.1")
    implementation("androidx.media3:media3-common:1.4.1")

    // Lottie — smooth loading / empty-state animations
    implementation("com.airbnb.android:lottie-compose:6.1.0")

    // ── Real libraries (Kotlin 2.4 / AGP 9 / compileSdk 37 toolchain) ──────
    // Miuix — HyperOS-style theme & components (BakaSU look)
    implementation("top.yukonga.miuix.kmp:miuix-ui-android:0.9.4")
    // AndroidLiquidGlass (Kyant0) — real backdrop refraction for the glass menu
    implementation("io.github.kyant0:backdrop:2.0.1")
    // liquidglass (shayann07) — refracting glass surfaces (Android 13+ shader,
    // tinted-fallback on Android 12 / API 31-32)
    implementation("dev.shayxo.liquidglass:liquidglass:0.1.0")
    // compose-audio-controls — ImageStripKnob for Bass / Virtualizer knobs
    implementation("org.androidaudioplugin:compose-audio-controls:0.7.3")

    implementation("io.coil-kt:coil-compose:2.5.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
