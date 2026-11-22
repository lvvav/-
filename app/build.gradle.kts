plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.zvonki.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.zvonki.app"
        minSdk = 24
        targetSdk = 34
        versionCode = (System.getenv("GITHUB_RUN_NUMBER") ?: "1").toInt()
        versionName = "1.0"
    }

    signingConfigs {
        create("ci") {
            storeFile = file("signing/zvonki.jks")
            storePassword = "f42d2853e66ef9203a8df0e927b0d3c8"
            keyAlias = "zvonki"
            keyPassword = "f42d2853e66ef9203a8df0e927b0d3c8"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("ci")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
}
