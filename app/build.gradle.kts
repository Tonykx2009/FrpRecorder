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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    //aaptOptions {
    //    noCompress.add("frpc")
   // }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    implementation("com.github.amoseui:nanohttpd:2.3.9")
    // 直接引用 本地AAR
    // implementation(files("/libs/google-webrtc-1.0.32006.aar"))
}
