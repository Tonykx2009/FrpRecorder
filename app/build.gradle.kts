plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.webrtcrecorder"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.webrtcrecorder"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            //isMinifyEnabled = false
            //proguardFiles(
            //    getDefaultProguardFile("proguard-android-optimize.txt"),
            //    "proguard-rules.pro"
            //    signingConfig signingConfigs.release
            //)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: "keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    //implementation("com.github.amoseui:nanohttpd:2.3.9")
    // 引用本地AAR
    // implementation(files("/libs/google-webrtc-1.0.32006.aar"))
}
