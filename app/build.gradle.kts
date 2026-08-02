import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

/**
 * Fitbit credentials are never committed. Put them in `local.properties`:
 *
 *   fitbit.clientId=23XXXX
 *   fitbit.redirectScheme=sparkgym
 *
 * Without them the app still runs; the Fitbit screen simply reports
 * "not configured" and Health Connect stays available as the sync source.
 */
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "com.sparkgym"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sparkgym"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val redirectScheme = localProps.getProperty("fitbit.redirectScheme") ?: "sparkgym"
        buildConfigField("String", "FITBIT_CLIENT_ID", "\"${localProps.getProperty("fitbit.clientId") ?: ""}\"")
        buildConfigField("String", "FITBIT_REDIRECT_SCHEME", "\"$redirectScheme\"")
        buildConfigField("String", "FITBIT_REDIRECT_URI", "\"$redirectScheme://fitbit-callback\"")
        manifestPlaceholders["fitbitRedirectScheme"] = redirectScheme
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
        }
        release {
            // Minification is off for the first release, deliberately.
            //
            // It was turned on originally, then turned off again while chasing
            // an install failure. proguard-rules.pro is now correct — it keeps
            // the @JavascriptInterface bridge the 3D heat map calls by name, and
            // the enum constants Room and DataStore persist by name, both of
            // which fail silently rather than crashing when R8 renames them.
            //
            // But a green build does not prove a minified app *runs*: only a
            // device does, and there are no instrumented tests yet. Shipping the
            // exact bytecode CI tested is worth more than a smaller APK. Turn
            // these on together, and test the heat map tap and the exercise
            // library on a real phone before shipping that build.
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        resources.excludes += setOf("/META-INF/{AL2.0,LGPL2.1}")
    }
}

// Room writes its schema JSON here so migrations can be diffed in review.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.health.connect)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
