plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.playlistmaker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.playlistmaker"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}



dependencies {
    implementation (libs.androidx.browser)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.material)
    implementation (libs.glide)
    annotationProcessor (libs.compiler)
    // Gson
    implementation(libs.gson)
    // Retrofit
    implementation (libs.retrofit)
    implementation (libs.retrofit2.converter.gson)
    // Logging interceptor (для удобства отладки)
    implementation (libs.logging.interceptor)
    implementation (libs.androidx.lifecycle.runtime.ktx)
    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx.v251)
    implementation(libs.androidx.activity.ktx)
    // Koin
    implementation(libs.koin.android)
    //Fragment
    implementation (libs.androidx.fragment.ktx.v155)
    implementation(libs.androidx.fragment.ktx.v162)
    implementation(libs.androidx.viewpager2)

    // Navigation
    implementation (libs.androidx.navigation.fragment.ktx)
    implementation (libs.androidx.navigation.ui.ktx)
}