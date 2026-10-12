plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val tgId = (System.getenv("API_ID") ?: "").trim().ifEmpty { "0" }
val tgHash = (System.getenv("API_HASH") ?: "").trim()

android {
    namespace = "app.nexogram.client"
    compileSdk = 34

    defaultConfig {
        applicationId = "app.nexogram.client"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "0.2"
        ndk { abiFilters += listOf("arm64-v8a") }
        buildConfigField("int", "TG_API_ID", tgId)
        buildConfigField("String", "TG_API_HASH", "\"" + tgHash + "\"")
    }

    signingConfigs {
        create("nexo") {
            storeFile = file("nexo.keystore")
            storePassword = "nexopass2026"
            keyAlias = "nexo"
            keyPassword = "nexopass2026"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("nexo")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.03"))
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("com.github.fazilus:tdlib-android:1.8.61")
}
