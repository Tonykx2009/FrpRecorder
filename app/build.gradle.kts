plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.webrtcrecorder" 
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.webrtcrecorder"
        minSdk = 24
        targetSdk = 34
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

    androidResources {
        // assets 不压缩 frpc（正确）
        noCompress += "frpc"

    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("io.github.vshmygin:webrtc-android:1.1.0")
    implementation("org.nanohttpd:nanohttpd:2.3.1")
}
