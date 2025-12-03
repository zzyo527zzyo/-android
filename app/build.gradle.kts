plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.secupay_jni"
    compileSdk = 34
    // 1. 定义签名配置
    signingConfigs {
        create("release") {
            storeFile = file("E:/AS/key_store.jks")
            storePassword = "527527"
            keyAlias = "key0"
            keyPassword = "527527"

            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }

        maybeCreate("debug").apply {
            storeFile = file("E:/AS/key_store.jks")
            storePassword = "527527"
            keyAlias = "key0"
            keyPassword = "527527"
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
    }


    defaultConfig {
        applicationId = "com.example.secupay_jni"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "1.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        //发布版本
        release {
//            isMinifyEnabled = true        // ✅ 正确：启用代码压缩和混淆
//
//            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            // ✅ 关键：必须显式引用签名配置
            signingConfig = signingConfigs.getByName("release")
//            //设置debuggable模式
//            isDebuggable = false


        }
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }

    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

}