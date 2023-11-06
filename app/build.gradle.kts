plugins {
    id("com.android.application")
}

android {
    namespace = "info.stefkovi.studium.mte_bakalarka"
    compileSdk = 33

    defaultConfig {
        applicationId = "info.stefkovi.studium.mte_bakalarka"
        minSdk = 28
        targetSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.auth0.android:jwtdecode:2.0.2")
}