plugins {
    id 'com.android.application'
}

android {
    namespace 'com.example.webrtcrecorder'
    compileSdk 34

    defaultConfig {
        applicationId "com.example.webrtcrecorder"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
                targetCompatibility JavaVersion.VERSION_1_8
    }

    // 允许打包 assets 中的可执行文件
    aaptOptions {
        noCompress "frpc"
    }
}

dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'org.webrtc:google-webrtc:1.0.32006'
    implementation 'org.nanohttpd:nanohttpd:2.3.1'
}