plugins {
    id("com.android.application") version "8.7.1" apply false
}

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
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    aaptOptions {
        noCompress.add("frpc")
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    implementation("org.nanohttpd:nanohttpd:2.3.1")
    // 直接引用 本地AAR
    // implementation(files("/libs/google-webrtc-1.0.32006.aar"))
}
