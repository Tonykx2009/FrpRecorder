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
    signingConfigs {
        release {
            // 从 local.properties 读取签名配置
            Properties props = new Properties()
            file("local.properties").withInputStream { stream ->
                props.load(stream)
            }

            storeFile file(props.getProperty('KEYSTORE_FILE'))
            storePassword props.getProperty('KEYSTORE_PASSWORD')
            keyAlias props.getProperty('KEY_ALIAS')
            keyPassword props.getProperty('KEY_PASSWORD')
        }
    }

    buildTypes {
        release {
            signingConfig signingConfigs.release // 使用签名
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}

dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'org.webrtc:google-webrtc:1.0.32006'
    implementation 'org.nanohttpd:nanohttpd:2.3.1'
}
