import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Release signing is driven by a gitignored keystore.properties at the repo root, so no secret
// reaches the public repo. Absent on a fresh clone → debug builds still work.
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) keystorePropsFile.inputStream().use { load(it) }
}

android {
    namespace = "com.sh4wty.downloader"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sh4wty.downloader"
        minSdk = 26
        targetSdk = 35
        versionCode = 4
        versionName = "0.2.2"

        // Personal app: build only for the target device (modern phones are arm64-v8a).
        // Each extra ABI bundles a full Python + yt-dlp copy, so we keep just one.
        ndk {
            abiFilters += "arm64-v8a"
        }
    }

    signingConfigs {
        if (keystorePropsFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Use the release signing config when the keystore is present; otherwise the build
            // stays unsigned (still produces an APK, just not distributable).
            signingConfig = signingConfigs.findByName("release")
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
    }

    packaging {
        // The yt-dlp / Python native payload ships as extracted assets; avoid clashing
        // duplicate META-INF entries from the bundled libraries.
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
        // The Python runtime must be unpacked on install so yt-dlp can dlopen it at runtime.
        jniLibs.useLegacyPackaging = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.androidx.datastore.preferences)

    // yt-dlp engine (Python + yt-dlp binary) and ffmpeg for audio extraction / merging.
    implementation(libs.youtubedl.android.library)
    implementation(libs.youtubedl.android.ffmpeg)
}
