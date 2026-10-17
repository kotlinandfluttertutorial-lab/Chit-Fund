import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktlint)
}

if (providers.gradleProperty("skipGoogleServices").orNull != "true") {
    apply(plugin = "com.google.gms.google-services")
}

// ---------------------------------------------------------------------------
// Read local.properties so developers can override URLs without touching
// build.gradle.  Keys used:
//   local.api.base.url   — dev machine API (default: Android emulator loopback)
//   cloud.api.base.url   — staging / prod API (must be set for cloud builds)
// ---------------------------------------------------------------------------
val localProps =
    Properties().also { props ->
        val file = rootProject.file("local.properties")
        if (file.exists()) props.load(file.inputStream())
    }

fun localProp(
    key: String,
    fallback: String,
): String = (localProps.getProperty(key) ?: fallback).let { "\"$it\"" }

android {
    namespace = "com.chitsmart"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.chitsmart"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // ---------------------------------------------------------------------------
    // Flavor dimensions
    //   environment — which backend this build talks to
    // ---------------------------------------------------------------------------
    flavorDimensions += "environment"

    productFlavors {
        // ── local ───────────────────────────────────────────────────────────
        // Talks to a developer's local Docker API (emulator → host loopback).
        // URL is read from local.properties so each machine can differ.
        create("local") {
            dimension = "environment"
            applicationIdSuffix = ".local"
            versionNameSuffix = "-local"

            buildConfigField(
                "String",
                "API_BASE_URL",
                localProp("local.api.base.url", "http://10.0.2.2:8000/api/v1/"),
            )
            buildConfigField("String", "BUILD_VARIANT", "\"local\"")
            buildConfigField("Boolean", "IS_LOGGING_ENABLED", "true")

            // Convenience: override the app name so both variants can coexist on a device
            resValue("string", "app_name", "ChitSmart Dev")
        }

        // ── cloud ───────────────────────────────────────────────────────────
        // Talks to the deployed cloud API (staging or production).
        // URL is read from local.properties (cloud.api.base.url) so it is
        // never hard-coded in git.  CI must set this property before building.
        create("cloud") {
            dimension = "environment"
            // No applicationIdSuffix → same ID as production for Play Store

            buildConfigField(
                "String",
                "API_BASE_URL",
                localProp("cloud.api.base.url", "https://api.chitsmart.app/api/v1/"),
            )
            buildConfigField("String", "BUILD_VARIANT", "\"cloud\"")
            buildConfigField("Boolean", "IS_LOGGING_ENABLED", "false")

            resValue("string", "app_name", "ChitSmart")
        }
    }

    // ---------------------------------------------------------------------------
    // Build types
    // ---------------------------------------------------------------------------
    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            // Logging always on in debug regardless of flavor
            buildConfigField("Boolean", "IS_LOGGING_ENABLED", "true")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            isDebuggable = false
            // IS_LOGGING_ENABLED is inherited from the flavor (false for cloud, true for local)
        }
    }

    // ---------------------------------------------------------------------------
    // Source sets — variant-specific code lives here.
    // Add Kotlin files under these directories for behavior that must differ
    // per environment.  AGP automatically merges them at compile time.
    //
    //  src/local/java/       — code compiled into ALL local-flavor builds
    //  src/cloud/java/       — code compiled into ALL cloud-flavor builds
    //  src/debug/java/       — code compiled into ALL debug build-type builds
    //  src/release/java/     — code compiled into ALL release build-type builds
    //
    // Note: combined variant source sets (e.g. "localDebug") are NOT declared
    // here — AGP resolves them automatically from the flavor + buildType pair.
    // Just create the directory and put your files in it:
    //   src/localDebug/java/…   or   src/cloudRelease/java/…
    // ---------------------------------------------------------------------------
    sourceSets {
        getByName("local") {
            java.srcDirs("src/local/java")
            res.srcDirs("src/local/res")
        }
        getByName("cloud") {
            java.srcDirs("src/cloud/java")
            res.srcDirs("src/cloud/res")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // ── Compose BOM ──────────────────────────────────────────────────────────
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Firebase BOM + Phone Auth
    val firebaseBom = platform(libs.firebase.bom)
    implementation(firebaseBom)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.kotlinx.coroutines.play.services)

    // Core Compose
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Activity + Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Retrofit + OkHttp (API client)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // DataStore (preferences — onboarding flag, language preference)
    implementation(libs.androidx.datastore.preferences)

    // Encrypted SharedPreferences (JWT token storage)
    implementation(libs.androidx.security.crypto)

    // Coil (image loading — avatars, receipts)
    implementation(libs.coil.compose)

    // Lottie (animations — payment success/failure, confetti)
    implementation(libs.lottie.compose)

    // Accompanist (HorizontalPager, permissions)
    implementation(libs.accompanist.permissions)

    // Room (local cache — optional for offline support)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Biometric
    implementation(libs.androidx.biometric)

    // ── Testing ──────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

tasks.register("testDebugUnitTest") {
    group = "verification"
    description = "Runs the cloud debug unit tests."
    dependsOn("testCloudDebugUnitTest")
}
